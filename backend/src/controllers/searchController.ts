import { Router } from 'express';
import { youtubeApiService } from '../services/YouTubeApiService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { searchQuerySchema } from '../utils/validation';
import { optionalAuthenticate } from '../middleware/authenticate';
import { QuotaExceededError } from '../types';
import { prisma } from '../config/database';
import { getCached, setCached } from '../config/redis';
import { config } from '../config/env';

const router = Router();

// GET /api/search
router.get(
  '/',
  optionalAuthenticate,
  asyncHandler(async (req, res) => {
    const query = validate(searchQuerySchema, req.query);
    const userId = req.user?.userId;

    try {
      // Try to get from cache first
      const cacheKey = `search:${userId || 'guest'}:${query.q}:${query.type || 'video'}:${query.page}`;
      const cached = await getCached(cacheKey);
      if (cached) {
        return res.json({ ...cached, isCached: true });
      }

      // Search YouTube API
      const response = await youtubeApiService.search(
        'snippet',
        query.q,
        query.type,
        query.pageSize,
        undefined,
        query.order,
        query.duration,
        'US',
        'moderate'
      );

      // Transform results to domain model
      const items = await Promise.all(
        response.items.map(async (item) => {
          if (item.id.videoId) {
            // It's a video
            const videoResponse = await youtubeApiService.getVideosById(
              'snippet,contentDetails,statistics',
              item.id.videoId
            );
            const video = videoResponse.items[0];

            if (!video) {
              return null;
            }

            const channelResponse = await youtubeApiService.getChannelDetails(
              'snippet,statistics',
              video.snippet.channelId
            );
            const channel = channelResponse.items[0];

            let isLiked = false;
            let isDisliked = false;
            let isSubscribed = false;

            if (userId) {
              const like = await prisma.like.findUnique({
                where: {
                  userId_targetType_targetId: {
                    userId,
                    targetType: 'video',
                    targetId: video.id,
                  },
                },
              });

              isLiked = like?.value === 'like';
              isDisliked = like?.value === 'dislike';

              const subscription = await prisma.subscription.findUnique({
                where: {
                  userId_channelId: {
                    userId,
                    channelId: channel?.id || video.snippet.channelId,
                  },
                },
              });

              isSubscribed = !!subscription;
            }

            return {
              id: video.id,
              youtubeVideoId: video.id,
              channelId: video.snippet.channelId,
              title: video.snippet.title,
              description: video.snippet.description,
              thumbnailUrl: video.snippet.thumbnails?.maxres?.url ||
                video.snippet.thumbnails?.high?.url ||
                video.snippet.thumbnails?.medium?.url,
              duration: video.contentDetails?.duration,
              viewCount: parseInt(video.statistics?.viewCount || '0'),
              likeCount: parseInt(video.statistics?.likeCount || '0'),
              dislikeCount: parseInt(video.statistics?.dislikeCount || '0'),
              commentCount: parseInt(video.statistics?.commentCount || '0'),
              publishedAt: video.snippet.publishedAt ? new Date(video.snippet.publishedAt) : null,
              isShort: video.contentDetails?.duration?.startsWith('PT') &&
                parseInt(video.contentDetails.duration.replace(/[^0-9]/g, '')) < 60,
              categoryId: video.snippet.categoryId,
              tags: video.snippet.tags || [],
              channel: channel ? {
                id: channel.id,
                name: channel.snippet.title,
                avatarUrl: channel.snippet.thumbnails?.high?.url ||
                  channel.snippet.thumbnails?.medium?.url,
                subscriberCount: parseInt(channel.statistics?.subscriberCount || '0'),
              } : null,
              isLiked,
              isDisliked,
              isSubscribed,
            };
          } else if (item.id.channelId) {
            // It's a channel
            const channelResponse = await youtubeApiService.getChannelDetails(
              'snippet,statistics',
              item.id.channelId
            );
            const channel = channelResponse.items[0];

            if (!channel) {
              return null;
            }

            let isSubscribed = false;

            if (userId) {
              const subscription = await prisma.subscription.findUnique({
                where: {
                  userId_channelId: {
                    userId,
                    channelId: channel.id,
                  },
                },
              });

              isSubscribed = !!subscription;
            }

            return {
              id: channel.id,
              youtubeChannelId: channel.id,
              name: channel.snippet.title,
              description: channel.snippet.description,
              avatarUrl: channel.snippet.thumbnails?.high?.url ||
                channel.snippet.thumbnails?.medium?.url,
              bannerUrl: undefined,
              subscriberCount: parseInt(channel.statistics?.subscriberCount || '0'),
              viewCount: parseInt(channel.statistics?.viewCount || '0'),
              videoCount: parseInt(channel.statistics?.videoCount || '0'),
              isSubscribed,
              subscriberCountText: formatSubscriberCount(
                parseInt(channel.statistics?.subscriberCount || '0')
              ),
              type: 'channel',
            };
          } else if (item.id.playlistId) {
            // It's a playlist
            return {
              id: item.id.playlistId,
              title: item.snippet.title,
              description: item.snippet.description,
              thumbnailUrl: item.snippet.thumbnails?.high?.url ||
                item.snippet.thumbnails?.medium?.url,
              channelTitle: item.snippet.channelTitle,
              channelId: item.snippet.channelId,
              type: 'playlist',
            };
          }

          return null;
        })
      );

      const result = {
        items: items.filter(Boolean),
        nextPageToken: response.nextPageToken,
        totalResults: response.pageInfo.totalResults,
        isCached: false,
      };

      // Cache for 10 minutes
      await setCached(cacheKey, result, config.cache.searchResults);

      // Save search history
      if (userId) {
        await prisma.searchHistory.upsert({
          where: {
            userId_query: {
              userId,
              query: query.q,
            },
          },
          update: {
            searchedAt: new Date(),
          },
          create: {
            userId,
            query: query.q,
            searchedAt: new Date(),
          },
        });
      }

      res.json(result);
    } catch (error) {
      if (error instanceof QuotaExceededError) {
        // Fallback to cached data if quota exceeded
        const cacheKey = `search:${userId || 'guest'}:${query.q}:${query.type || 'video'}:${query.page}`;
        const cached = await getCached(cacheKey);
        if (cached) {
          return res.json({ ...cached, isCached: true, quotaExceeded: true });
        }
      }
      throw error;
    }
  })
);

// Helper function
function formatSubscriberCount(count: number): string {
  if (count >= 1000000) {
    return `${(count / 1000000).toFixed(1)}M subscribers`;
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}K subscribers`;
  }
  return `${count} subscribers`;
}

export { router as searchRouter };

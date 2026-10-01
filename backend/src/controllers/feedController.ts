import { Router } from 'express';
import { youtubeApiService } from '../services/YouTubeApiService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { feedQuerySchema } from '../utils/validation';
import { optionalAuthenticate } from '../middleware/authenticate';
import { QuotaExceededError } from '../types';
import { prisma } from '../config/database';
import { getCached, setCached } from '../config/redis';
import { config } from '../config/env';

const router = Router();

// GET /api/feed
router.get(
  '/',
  optionalAuthenticate,
  asyncHandler(async (req, res) => {
    const query = validate(feedQuerySchema, req.query);
    const userId = req.user?.userId;

    try {
      // Try to get from cache first
      const cacheKey = `feed:${userId || 'guest'}:${query.categoryId || 'all'}:${query.page}`;
      const cached = await getCached(cacheKey);
      if (cached) {
        return res.json({ ...cached, isCached: true });
      }

      // Get trending videos from YouTube API
      const response = await youtubeApiService.getTrendingVideos(
        'snippet,contentDetails,statistics',
        'mostPopular',
        'US',
        query.pageSize,
        undefined,
        query.categoryId
      );

      // Transform videos to domain model
      const videos = await Promise.all(
        response.items.map(async (item) => {
          const channelResponse = await youtubeApiService.getChannelDetails(
            'snippet,statistics',
            item.snippet.channelId
          );
          const channel = channelResponse.items[0];

          // Check if user has interactions with this video
          let isLiked = false;
          let isDisliked = false;
          let isSubscribed = false;

          if (userId) {
            const like = await prisma.like.findUnique({
              where: {
                userId_targetType_targetId: {
                  userId,
                  targetType: 'video',
                  targetId: item.id,
                },
              },
            });

            isLiked = like?.value === 'like';
            isDisliked = like?.value === 'dislike';

            const subscription = await prisma.subscription.findUnique({
              where: {
                userId_channelId: {
                  userId,
                  channelId: channel?.id || item.snippet.channelId,
                },
              },
            });

            isSubscribed = !!subscription;
          }

          return {
            id: item.id,
            youtubeVideoId: item.id,
            channelId: item.snippet.channelId,
            title: item.snippet.title,
            description: item.snippet.description,
            thumbnailUrl: item.snippet.thumbnails?.maxres?.url ||
              item.snippet.thumbnails?.high?.url ||
              item.snippet.thumbnails?.medium?.url,
            duration: item.contentDetails?.duration,
            viewCount: parseInt(item.statistics?.viewCount || '0'),
            likeCount: parseInt(item.statistics?.likeCount || '0'),
            dislikeCount: parseInt(item.statistics?.dislikeCount || '0'),
            commentCount: parseInt(item.statistics?.commentCount || '0'),
            publishedAt: item.snippet.publishedAt ? new Date(item.snippet.publishedAt) : null,
            isShort: item.contentDetails?.duration?.startsWith('PT') &&
              parseInt(item.contentDetails.duration.replace(/[^0-9]/g, '')) < 60,
            categoryId: item.snippet.categoryId,
            tags: item.snippet.tags || [],
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
        })
      );

      const result = {
        items: videos,
        nextPageToken: response.nextPageToken,
        totalResults: response.pageInfo.totalResults,
        isCached: false,
      };

      // Cache for 5 minutes
      await setCached(cacheKey, result, config.cache.trending);

      res.json(result);
    } catch (error) {
      if (error instanceof QuotaExceededError) {
        // Fallback to cached data if quota exceeded
        const cacheKey = `feed:${userId || 'guest'}:${query.categoryId || 'all'}:${query.page}`;
        const cached = await getCached(cacheKey);
        if (cached) {
          return res.json({ ...cached, isCached: true, quotaExceeded: true });
        }
      }
      throw error;
    }
  })
);

export { router as feedRouter };

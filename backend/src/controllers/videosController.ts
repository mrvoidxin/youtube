import { Router } from 'express';
import { youtubeApiService } from '../services/YouTubeApiService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { videoIdParamSchema } from '../utils/validation';
import { optionalAuthenticate } from '../middleware/authenticate';
import { QuotaExceededError } from '../types';
import { prisma } from '../config/database';
import { getCached, setCached } from '../config/redis';
import { config } from '../config/env';

const router = Router();

// GET /api/videos/:id
router.get(
  '/:id',
  optionalAuthenticate,
  asyncHandler(async (req, res) => {
    const params = validate(videoIdParamSchema, req.params);
    const userId = req.user?.userId;

    try {
      // Try to get from cache first
      const cacheKey = `video:${params.id}:${userId || 'guest'}`;
      const cached = await getCached(cacheKey);
      if (cached) {
        return res.json({ ...cached, isCached: true });
      }

      // Get video details from YouTube API
      const videoResponse = await youtubeApiService.getVideosById(
        'snippet,contentDetails,statistics,liveStreamingDetails',
        params.id
      );

      if (!videoResponse.items.length) {
        return res.status(404).json({ error: 'Video not found' });
      }

      const video = videoResponse.items[0];

      // Get channel details
      const channelResponse = await youtubeApiService.getChannelDetails(
        'snippet,statistics',
        video.snippet.channelId
      );

      const channel = channelResponse.items[0];

      // Get related videos
      const relatedResponse = await youtubeApiService.getRelatedVideos(
        'snippet',
        params.id,
        'video',
        15
      );

      // Transform to domain model
      const relatedVideos = await Promise.all(
        relatedResponse.items.map(async (item) => {
          const videoResponse = await youtubeApiService.getVideosById(
            'snippet,contentDetails,statistics',
            item.id.videoId || item.id
          );
          const relatedVideo = videoResponse.items[0];

          if (!relatedVideo) {
            return null;
          }

          let isLiked = false;
          let isDisliked = false;

          if (userId) {
            const like = await prisma.like.findUnique({
              where: {
                userId_targetType_targetId: {
                  userId,
                  targetType: 'video',
                  targetId: relatedVideo.id,
                },
              },
            });

            isLiked = like?.value === 'like';
            isDisliked = like?.value === 'dislike';
          }

          return {
            id: relatedVideo.id,
            youtubeVideoId: relatedVideo.id,
            channelId: relatedVideo.snippet.channelId,
            title: relatedVideo.snippet.title,
            description: relatedVideo.snippet.description,
            thumbnailUrl: relatedVideo.snippet.thumbnails?.maxres?.url ||
              relatedVideo.snippet.thumbnails?.high?.url ||
              relatedVideo.snippet.thumbnails?.medium?.url,
            duration: relatedVideo.contentDetails?.duration,
            viewCount: parseInt(relatedVideo.statistics?.viewCount || '0'),
            likeCount: parseInt(relatedVideo.statistics?.likeCount || '0'),
            dislikeCount: parseInt(relatedVideo.statistics?.dislikeCount || '0'),
            commentCount: parseInt(relatedVideo.statistics?.commentCount || '0'),
            publishedAt: relatedVideo.snippet.publishedAt ? new Date(relatedVideo.snippet.publishedAt) : null,
            isShort: relatedVideo.contentDetails?.duration?.startsWith('PT') &&
              parseInt(relatedVideo.contentDetails.duration.replace(/[^0-9]/g, '')) < 60,
            categoryId: relatedVideo.snippet.categoryId,
            tags: relatedVideo.snippet.tags || [],
            isLiked,
            isDisliked,
          };
        })
      );

      // Check user interactions
      let isLiked = false;
      let isDisliked = false;
      let isSubscribed = false;
      let isInWatchLater = false;
      let isInHistory = false;

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

        const watchLater = await prisma.watchLater.findUnique({
          where: {
            userId_videoId: {
              userId,
              videoId: video.id,
            },
          },
        });

        isInWatchLater = !!watchLater;

        const history = await prisma.watchHistory.findUnique({
          where: {
            userId_videoId: {
              userId,
              videoId: video.id,
            },
          },
        });

        isInHistory = !!history;
      }

      // Get comments count
      const commentsResponse = await youtubeApiService.getVideoComments(
        'snippet',
        params.id,
        1
      );

      const result = {
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
        commentCount: commentsResponse.pageInfo.totalResults,
        publishedAt: video.snippet.publishedAt ? new Date(video.snippet.publishedAt) : null,
        isShort: video.contentDetails?.duration?.startsWith('PT') &&
          parseInt(video.contentDetails.duration.replace(/[^0-9]/g, '')) < 60,
        categoryId: video.snippet.categoryId,
        tags: video.snippet.tags || [],
        channel: channel ? {
          id: channel.id,
          youtubeChannelId: channel.id,
          name: channel.snippet.title,
          avatarUrl: channel.snippet.thumbnails?.high?.url ||
            channel.snippet.thumbnails?.medium?.url,
          bannerUrl: channel.brandingSettings?.image?.bannerExternalUrl,
          description: channel.snippet.description,
          subscriberCount: parseInt(channel.statistics?.subscriberCount || '0'),
          viewCount: parseInt(channel.statistics?.viewCount || '0'),
          videoCount: parseInt(channel.statistics?.videoCount || '0'),
          isSubscribed,
          subscriberCountText: formatSubscriberCount(
            parseInt(channel.statistics?.subscriberCount || '0')
          ),
        } : null,
        relatedVideos: relatedVideos.filter(Boolean),
        isLiked,
        isDisliked,
        isSubscribed,
        isInWatchLater,
        isInHistory,
        isCached: false,
      };

      // Cache for 5 minutes
      await setCached(cacheKey, result, config.cache.videoDetails);

      // Save to watch history
      if (userId) {
        await prisma.watchHistory.upsert({
          where: {
            userId_videoId: {
              userId,
              videoId: video.id,
            },
          },
          update: {
            watchedAt: new Date(),
          },
          create: {
            userId,
            videoId: video.id,
            watchedAt: new Date(),
            progressSeconds: 0,
          },
        });
      }

      res.json(result);
    } catch (error) {
      if (error instanceof QuotaExceededError) {
        // Fallback to cached data if quota exceeded
        const cacheKey = `video:${params.id}:${userId || 'guest'}`;
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
    return `${(count / 1000000).toFixed(1)}M`;
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}K`;
  }
  return count.toString();
}

export { router as videosRouter };

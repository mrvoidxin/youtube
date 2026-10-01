import { Router } from 'express';
import { youtubeApiService } from '../services/YouTubeApiService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { channelIdParamSchema } from '../utils/validation';
import { authenticate, optionalAuthenticate } from '../middleware/authenticate';
import { QuotaExceededError } from '../types';
import { prisma } from '../config/database';
import { getCached, setCached } from '../config/redis';
import { config } from '../config/env';

const router = Router();

// GET /api/channels/:id
router.get(
  '/:id',
  optionalAuthenticate,
  asyncHandler(async (req, res) => {
    const params = validate(channelIdParamSchema, req.params);
    const userId = req.user?.userId;

    try {
      // Try to get from cache first
      const cacheKey = `channel:${params.id}:${userId || 'guest'}`;
      const cached = await getCached(cacheKey);
      if (cached) {
        return res.json({ ...cached, isCached: true });
      }

      // Get channel details from YouTube API
      const channelResponse = await youtubeApiService.getChannelDetails(
        'snippet,statistics,brandingSettings',
        params.id
      );

      if (!channelResponse.items.length) {
        return res.status(404).json({ error: 'Channel not found' });
      }

      const channel = channelResponse.items[0];

      // Get channel videos
      const videosResponse = await youtubeApiService.search(
        'snippet',
        '',
        'video',
        20,
        undefined,
        'date',
        undefined,
        'US',
        'moderate'
      );

      // Filter videos by channel
      const channelVideos = videosResponse.items.filter(
        (item) => item.snippet.channelId === params.id
      );

      // Get video details for channel videos
      const videoIds = channelVideos.map((item) => item.id.videoId).filter(Boolean);
      const videosDetails = videoIds.length > 0
        ? await youtubeApiService.getVideosById(
            'snippet,contentDetails,statistics',
            videoIds.join(',')
          )
        : { items: [] };

      // Transform to domain model
      const videos = await Promise.all(
        videosDetails.items.map(async (video) => {
          let isLiked = false;
          let isDisliked = false;

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
            isLiked,
            isDisliked,
          };
        })
      );

      // Check if user is subscribed
      let isSubscribed = false;
      let subscriberCount = parseInt(channel.statistics?.subscriberCount || '0');

      if (userId) {
        // Check if user has a channel in our database
        const userChannel = await prisma.channel.findFirst({
          where: { ownerUserId: userId },
        });

        if (userChannel) {
          const subscription = await prisma.subscription.findUnique({
            where: {
              userId_channelId: {
                userId,
                channelId: userChannel.id,
              },
            },
          });

          isSubscribed = !!subscription;
        }

        // Check if user is subscribed to this channel
        const dbChannel = await prisma.channel.findUnique({
          where: { youtubeChannelId: params.id },
        });

        if (dbChannel) {
          const subscription = await prisma.subscription.findUnique({
            where: {
              userId_channelId: {
                userId,
                channelId: dbChannel.id,
              },
            },
          });

          isSubscribed = !!subscription;

          // Update subscriber count from our database if available
          const dbChannelData = await prisma.channel.findUnique({
            where: { id: dbChannel.id },
            include: { _count: { select: { subscriptions: true } } },
          });

          if (dbChannelData) {
            subscriberCount = dbChannelData._count.subscriptions;
          }
        }
      }

      const result = {
        id: channel.id,
        youtubeChannelId: channel.id,
        name: channel.snippet.title,
        bannerUrl: channel.brandingSettings?.image?.bannerExternalUrl,
        avatarUrl: channel.snippet.thumbnails?.high?.url ||
          channel.snippet.thumbnails?.medium?.url,
        description: channel.snippet.description,
        subscriberCount,
        viewCount: parseInt(channel.statistics?.viewCount || '0'),
        videoCount: parseInt(channel.statistics?.videoCount || '0'),
        isSubscribed,
        subscriberCountText: formatSubscriberCount(subscriberCount),
        videos,
        isCached: false,
      };

      // Cache for 60 minutes
      await setCached(cacheKey, result, config.cache.channelInfo);

      res.json(result);
    } catch (error) {
      if (error instanceof QuotaExceededError) {
        // Fallback to cached data if quota exceeded
        const cacheKey = `channel:${params.id}:${userId || 'guest'}`;
        const cached = await getCached(cacheKey);
        if (cached) {
          return res.json({ ...cached, isCached: true, quotaExceeded: true });
        }
      }
      throw error;
    }
  })
);

// POST /api/channels/:id/subscribe
router.post(
  '/:id/subscribe',
  authenticate,
  asyncHandler(async (req, res) => {
    const params = validate(channelIdParamSchema, req.params);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Find or create channel in our database
    let dbChannel = await prisma.channel.findUnique({
      where: { youtubeChannelId: params.id },
    });

    if (!dbChannel) {
      // Get channel details from YouTube API
      const channelResponse = await youtubeApiService.getChannelDetails(
        'snippet,statistics',
        params.id
      );

      if (!channelResponse.items.length) {
        return res.status(404).json({ error: 'Channel not found' });
      }

      const channel = channelResponse.items[0];

      dbChannel = await prisma.channel.create({
        data: {
          id: uuidv4(),
          youtubeChannelId: channel.id,
          name: channel.snippet.title,
          avatarUrl: channel.snippet.thumbnails?.high?.url ||
            channel.snippet.thumbnails?.medium?.url,
          description: channel.snippet.description,
          subscriberCount: parseInt(channel.statistics?.subscriberCount || '0'),
          viewCount: parseInt(channel.statistics?.viewCount || '0'),
          videoCount: parseInt(channel.statistics?.videoCount || '0'),
          ownerUserId: userId, // For now, set the current user as owner
        },
      });
    }

    // Create subscription
    const subscription = await prisma.subscription.upsert({
      where: {
        userId_channelId: {
          userId,
          channelId: dbChannel.id,
        },
      },
      update: {},
      create: {
        userId,
        channelId: dbChannel.id,
      },
    });

    // Invalidate cache
    await deleteCached(`channel:${params.id}:${userId}`);

    res.json({
      message: 'Subscribed successfully',
      subscription,
    });
  })
);

// DELETE /api/channels/:id/subscribe
router.delete(
  '/:id/subscribe',
  authenticate,
  asyncHandler(async (req, res) => {
    const params = validate(channelIdParamSchema, req.params);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Find channel in our database
    const dbChannel = await prisma.channel.findUnique({
      where: { youtubeChannelId: params.id },
    });

    if (!dbChannel) {
      return res.status(404).json({ error: 'Channel not found' });
    }

    // Delete subscription
    await prisma.subscription.delete({
      where: {
        userId_channelId: {
          userId,
          channelId: dbChannel.id,
        },
      },
    });

    // Invalidate cache
    await deleteCached(`channel:${params.id}:${userId}`);

    res.json({ message: 'Unsubscribed successfully' });
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

// Helper import
import { v4 as uuidv4 } from 'uuid';
import { deleteCached } from '../config/redis';

export { router as channelsRouter };

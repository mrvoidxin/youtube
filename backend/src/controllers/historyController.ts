import { Router } from 'express';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { historyQuerySchema, createHistorySchema } from '../utils/validation';
import { authenticate } from '../middleware/authenticate';
import { prisma } from '../config/database';
import { v4 as uuidv4 } from 'uuid';

const router = Router();

// GET /api/history
router.get(
  '/',
  authenticate,
  asyncHandler(async (req, res) => {
    const query = validate(historyQuerySchema, req.query);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Get watch history with video details
    const history = await prisma.watchHistory.findMany({
      where: { userId },
      include: {
        video: true,
      },
      orderBy: { watchedAt: 'desc' },
      take: query.pageSize,
      skip: (query.page - 1) * query.pageSize,
    });

    // Get total count
    const total = await prisma.watchHistory.count({
      where: { userId },
    });

    const items = await Promise.all(
      history.map(async (item) => {
        // Check if video exists in our database
        let video = item.video;

        if (!video) {
          // Try to get video from YouTube API
          // For now, we'll just return the history item without video details
          return {
            id: item.id,
            videoId: item.videoId,
            watchedAt: item.watchedAt,
            progressSeconds: item.progressSeconds,
            duration: item.duration,
            video: null,
          };
        }

        return {
          id: item.id,
          videoId: video.id,
          video: {
            id: video.id,
            youtubeVideoId: video.youtubeVideoId,
            channelId: video.channelId,
            title: video.title,
            description: video.description || undefined,
            thumbnailUrl: video.thumbnailUrl || undefined,
            duration: video.duration || undefined,
            viewCount: video.viewCount || 0,
            likeCount: video.likeCount || 0,
            dislikeCount: video.dislikeCount || 0,
            commentCount: video.commentCount || 0,
            publishedAt: video.publishedAt || null,
            isShort: video.isShort,
            categoryId: video.categoryId || undefined,
            tags: video.tags,
          },
          watchedAt: item.watchedAt,
          progressSeconds: item.progressSeconds,
          duration: item.duration,
        };
      })
    );

    res.json({
      items,
      totalResults: total,
      page: query.page,
      pageSize: query.pageSize,
    });
  })
);

// POST /api/history
router.post(
  '/',
  authenticate,
  asyncHandler(async (req, res) => {
    const data = validate(createHistorySchema, req.body);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Find or create video in our database
    let video = await prisma.video.findUnique({
      where: { youtubeVideoId: data.videoId },
    });

    if (!video) {
      // Create a placeholder video
      video = await prisma.video.create({
        data: {
          id: uuidv4(),
          youtubeVideoId: data.videoId,
          channelId: 'unknown',
          title: 'Unknown Video',
          description: null,
          thumbnailUrl: null,
          duration: data.duration?.toString() || null,
          viewCount: 0,
          likeCount: 0,
          dislikeCount: 0,
          commentCount: 0,
          publishedAt: null,
          isShort: false,
          categoryId: null,
          tags: [],
        },
      });
    }

    // Create or update watch history
    const history = await prisma.watchHistory.upsert({
      where: {
        userId_videoId: {
          userId,
          videoId: video.id,
        },
      },
      update: {
        watchedAt: new Date(),
        progressSeconds: data.progressSeconds,
        duration: data.duration,
      },
      create: {
        userId,
        videoId: video.id,
        watchedAt: new Date(),
        progressSeconds: data.progressSeconds,
        duration: data.duration,
      },
      include: {
        video: true,
      },
    });

    res.status(201).json({
      id: history.id,
      videoId: history.videoId,
      video: {
        id: history.video.id,
        youtubeVideoId: history.video.youtubeVideoId,
        channelId: history.video.channelId,
        title: history.video.title,
        description: history.video.description || undefined,
        thumbnailUrl: history.video.thumbnailUrl || undefined,
        duration: history.video.duration || undefined,
        viewCount: history.video.viewCount || 0,
        likeCount: history.video.likeCount || 0,
        dislikeCount: history.video.dislikeCount || 0,
        commentCount: history.video.commentCount || 0,
        publishedAt: history.video.publishedAt || null,
        isShort: history.video.isShort,
        categoryId: history.video.categoryId || undefined,
        tags: history.video.tags,
      },
      watchedAt: history.watchedAt,
      progressSeconds: history.progressSeconds,
      duration: history.duration,
    });
  })
);

// DELETE /api/history
router.delete(
  '/',
  authenticate,
  asyncHandler(async (req, res) => {
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Delete all watch history for user
    await prisma.watchHistory.deleteMany({
      where: { userId },
    });

    res.json({ message: 'Watch history cleared' });
  })
);

// DELETE /api/history/:id
router.delete(
  '/:id',
  authenticate,
  asyncHandler(async (req, res) => {
    const userId = req.user?.userId;
    const { id } = req.params;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Delete specific history item
    await prisma.watchHistory.delete({
      where: {
        id,
        userId,
      },
    });

    res.json({ message: 'History item removed' });
  })
);

export { router as historyRouter };

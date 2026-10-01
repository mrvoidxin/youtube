import { Router } from 'express';
import { youtubeApiService } from '../services/YouTubeApiService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import { videoIdParamSchema, commentsQuerySchema, createCommentSchema } from '../utils/validation';
import { authenticate, optionalAuthenticate } from '../middleware/authenticate';
import { QuotaExceededError } from '../types';
import { prisma } from '../config/database';
import { getCached, setCached } from '../config/redis';
import { config } from '../config/env';
import { v4 as uuidv4 } from 'uuid';

const router = Router();

// GET /api/videos/:id/comments
router.get(
  '/videos/:id',
  optionalAuthenticate,
  asyncHandler(async (req, res) => {
    const params = validate(videoIdParamSchema, req.params);
    const query = validate(commentsQuerySchema, req.query);
    const userId = req.user?.userId;

    try {
      // Try to get from cache first
      const cacheKey = `comments:${params.id}:${query.page || '1'}:${userId || 'guest'}`;
      const cached = await getCached(cacheKey);
      if (cached) {
        return res.json({ ...cached, isCached: true });
      }

      // Get comments from YouTube API
      const commentsResponse = await youtubeApiService.getVideoComments(
        'snippet,replies',
        params.id,
        query.pageSize,
        undefined,
        query.order
      );

      // Transform comments to domain model
      const comments = await Promise.all(
        commentsResponse.items.map(async (item) => {
          const comment = item.snippet.topLevelComment.snippet;

          // Get user info from our database or create a placeholder
          let user = await prisma.user.findFirst({
            where: {
              OR: [
                { email: comment.authorChannelId?.value },
                { googleId: comment.authorChannelId?.value },
              ],
            },
          });

          if (!user) {
            // Create a placeholder user for external YouTube users
            user = await prisma.user.upsert({
              where: { email: `youtube_${comment.authorChannelId?.value}@placeholder.com` },
              update: {},
              create: {
                email: `youtube_${comment.authorChannelId?.value}@placeholder.com`,
                displayName: comment.authorDisplayName,
                avatarUrl: comment.authorProfileImageUrl,
              },
            });
          }

          // Check if current user liked/disliked this comment
          let isLiked = false;
          let isDisliked = false;

          if (userId) {
            const like = await prisma.like.findUnique({
              where: {
                userId_targetType_targetId: {
                  userId,
                  targetType: 'comment',
                  targetId: item.id,
                },
              },
            });

            isLiked = like?.value === 'like';
            isDisliked = like?.value === 'dislike';
          }

          // Get replies
          const replies = item.replies?.comments || [];
          const replyComments = await Promise.all(
            replies.map(async (reply) => {
              const replySnippet = reply.snippet;

              let replyUser = await prisma.user.findFirst({
                where: {
                  OR: [
                    { email: replySnippet.authorChannelId?.value },
                    { googleId: replySnippet.authorChannelId?.value },
                  ],
                },
              });

              if (!replyUser) {
                replyUser = await prisma.user.upsert({
                  where: { email: `youtube_${replySnippet.authorChannelId?.value}@placeholder.com` },
                  update: {},
                  create: {
                    email: `youtube_${replySnippet.authorChannelId?.value}@placeholder.com`,
                    displayName: replySnippet.authorDisplayName,
                    avatarUrl: replySnippet.authorProfileImageUrl,
                  },
                });
              }

              return {
                id: reply.id,
                youtubeCommentId: reply.id,
                videoId: params.id,
                user: {
                  id: replyUser.id,
                  displayName: replyUser.displayName,
                  avatarUrl: replyUser.avatarUrl || undefined,
                },
                body: replySnippet.textDisplay || replySnippet.textOriginal,
                likeCount: parseInt(replySnippet.likeCount.toString()) || 0,
                createdAt: new Date(replySnippet.publishedAt),
                isLiked: false,
                isDisliked: false,
              };
            })
          );

          return {
            id: item.id,
            youtubeCommentId: item.id,
            videoId: params.id,
            user: {
              id: user.id,
              displayName: user.displayName,
              avatarUrl: user.avatarUrl || undefined,
            },
            body: comment.textDisplay || comment.textOriginal,
            likeCount: parseInt(comment.likeCount.toString()) || 0,
            createdAt: new Date(comment.publishedAt),
            replies: replyComments,
            isLiked,
            isDisliked,
          };
        })
      );

      const result = {
        items: comments,
        nextPageToken: commentsResponse.nextPageToken,
        totalResults: commentsResponse.pageInfo.totalResults,
        isCached: false,
      };

      // Cache for 10 minutes
      await setCached(cacheKey, result, config.cache.comments);

      res.json(result);
    } catch (error) {
      if (error instanceof QuotaExceededError) {
        // Fallback to our database comments
        const dbComments = await prisma.comment.findMany({
          where: { videoId: params.id },
          include: {
            user: {
              select: { id: true, displayName: true, avatarUrl: true },
            },
            replies: {
              include: {
                user: {
                  select: { id: true, displayName: true, avatarUrl: true },
                },
              },
            },
          },
          orderBy: { createdAt: 'desc' },
          take: query.pageSize,
        });

        const comments = await Promise.all(
          dbComments.map(async (comment) => {
            let isLiked = false;
            let isDisliked = false;

            if (userId) {
              const like = await prisma.like.findUnique({
                where: {
                  userId_targetType_targetId: {
                    userId,
                    targetType: 'comment',
                    targetId: comment.id,
                  },
                },
              });

              isLiked = like?.value === 'like';
              isDisliked = like?.value === 'dislike';
            }

            return {
              id: comment.id,
              youtubeCommentId: comment.youtubeCommentId || undefined,
              videoId: comment.videoId,
              user: comment.user,
              body: comment.body,
              likeCount: comment.likeCount,
              createdAt: comment.createdAt,
              replies: comment.replies.map((reply) => ({
                id: reply.id,
                youtubeCommentId: reply.youtubeCommentId || undefined,
                videoId: reply.videoId,
                user: reply.user,
                body: reply.body,
                likeCount: reply.likeCount,
                createdAt: reply.createdAt,
                isLiked: false,
                isDisliked: false,
              })),
              isLiked,
              isDisliked,
            };
          })
        );

        return res.json({
          items: comments,
          isCached: false,
          quotaExceeded: true,
        });
      }
      throw error;
    }
  })
);

// POST /api/videos/:id/comments
router.post(
  '/videos/:id',
  authenticate,
  asyncHandler(async (req, res) => {
    const params = validate(videoIdParamSchema, req.params);
    const data = validate(createCommentSchema, req.body);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Create comment in our database
    const comment = await prisma.comment.create({
      data: {
        id: uuidv4(),
        userId,
        videoId: params.id,
        body: data.body,
        likeCount: 0,
        parentCommentId: data.parentCommentId,
      },
      include: {
        user: {
          select: { id: true, displayName: true, avatarUrl: true },
        },
      },
    });

    // Invalidate comments cache
    await deleteCached(`comments:${params.id}:*`);

    res.status(201).json({
      id: comment.id,
      youtubeCommentId: undefined,
      videoId: comment.videoId,
      user: comment.user,
      body: comment.body,
      likeCount: comment.likeCount,
      createdAt: comment.createdAt,
      replies: [],
      isLiked: false,
      isDisliked: false,
    });
  })
);

// POST /api/comments/:id/like
router.post(
  '/:id/like',
  authenticate,
  asyncHandler(async (req, res) => {
    const params = validate({ id: z.string().min(1) }, req.params);
    const userId = req.user?.userId;

    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    // Find comment
    const comment = await prisma.comment.findUnique({
      where: { id: params.id },
    });

    if (!comment) {
      return res.status(404).json({ error: 'Comment not found' });
    }

    // Toggle like
    const existingLike = await prisma.like.findUnique({
      where: {
        userId_targetType_targetId: {
          userId,
          targetType: 'comment',
          targetId: params.id,
        },
      },
    });

    let like;

    if (existingLike) {
      // Remove like if it exists
      await prisma.like.delete({
        where: {
          userId_targetType_targetId: {
            userId,
            targetType: 'comment',
            targetId: params.id,
          },
        },
      });

      // Decrement like count
      await prisma.comment.update({
        where: { id: params.id },
        data: { likeCount: { decrement: 1 } },
      });

      like = null;
    } else {
      // Create like
      like = await prisma.like.create({
        data: {
          userId,
          targetType: 'comment',
          targetId: params.id,
          value: 'like',
        },
      });

      // Increment like count
      await prisma.comment.update({
        where: { id: params.id },
        data: { likeCount: { increment: 1 } },
      });
    }

    // Invalidate cache
    await deleteCached(`comments:${comment.videoId}:*`);

    res.json({
      message: like ? 'Comment liked' : 'Comment unliked',
      like,
    });
  })
);

// Helper import
import { z } from 'zod';
import { deleteCached } from '../config/redis';

export { router as commentsRouter };

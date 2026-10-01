import { z } from 'zod';

// ============================================
// AUTHENTICATION SCHEMAS
// ============================================

export const registerSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string()
    .min(8, 'Password must be at least 8 characters')
    .max(100, 'Password must be less than 100 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  displayName: z.string()
    .min(2, 'Display name must be at least 2 characters')
    .max(50, 'Display name must be less than 50 characters'),
});

export const loginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

export const googleAuthSchema = z.object({
  credential: z.string().min(1, 'Credential is required'),
});

export const refreshTokenSchema = z.object({
  refreshToken: z.string().min(1, 'Refresh token is required'),
});

// ============================================
// FEED SCHEMAS
// ============================================

export const feedQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
  categoryId: z.string().optional(),
});

// ============================================
// SEARCH SCHEMAS
// ============================================

export const searchQuerySchema = z.object({
  q: z.string().min(1, 'Search query is required'),
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(50).default(20),
  type: z.enum(['video', 'channel', 'playlist']).optional(),
  duration: z.enum(['short', 'medium', 'long']).optional(),
  order: z.enum(['date', 'rating', 'relevance', 'title', 'videoCount', 'viewCount']).optional(),
  categoryId: z.string().optional(),
});

// ============================================
// VIDEO SCHEMAS
// ============================================

export const videoIdParamSchema = z.object({
  id: z.string().min(1, 'Video ID is required'),
});

// ============================================
// CHANNEL SCHEMAS
// ============================================

export const channelIdParamSchema = z.object({
  id: z.string().min(1, 'Channel ID is required'),
});

// ============================================
// COMMENT SCHEMAS
// ============================================

export const createCommentSchema = z.object({
  videoId: z.string().min(1, 'Video ID is required'),
  body: z.string()
    .min(1, 'Comment body is required')
    .max(5000, 'Comment must be less than 5000 characters'),
  parentCommentId: z.string().optional(),
});

export const commentsQuerySchema = z.object({
  videoId: z.string().min(1, 'Video ID is required'),
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(50),
  order: z.enum(['time', 'relevance']).optional(),
});

// ============================================
// LIKE SCHEMAS
// ============================================

export const likeParamSchema = z.object({
  id: z.string().min(1, 'ID is required'),
});

export const createLikeSchema = z.object({
  targetType: z.enum(['video', 'comment']),
  targetId: z.string().min(1, 'Target ID is required'),
  value: z.enum(['like', 'dislike']),
});

// ============================================
// SUBSCRIPTION SCHEMAS
// ============================================

export const subscriptionParamSchema = z.object({
  id: z.string().min(1, 'Channel ID is required'),
});

// ============================================
// HISTORY SCHEMAS
// ============================================

export const historyQuerySchema = z.object({
  page: z.coerce.number().int().min(1).default(1),
  pageSize: z.coerce.number().int().min(1).max(100).default(20),
});

export const createHistorySchema = z.object({
  videoId: z.string().min(1, 'Video ID is required'),
  progressSeconds: z.number().int().min(0).default(0),
  duration: z.number().int().min(0).optional(),
});

// ============================================
// VALIDATION UTILITY
// ============================================

export function validate<T>(schema: z.ZodSchema<T>, data: unknown): T {
  const result = schema.safeParse(data);
  if (!result.success) {
    const errors = result.error.errors.map((err) => ({
      field: err.path.join('.'),
      message: err.message,
    }));
    throw new Error(JSON.stringify(errors));
  }
  return result.data;
}

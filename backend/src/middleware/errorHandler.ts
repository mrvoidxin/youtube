import { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';
import {
  AuthenticationError,
  AuthorizationError,
  ValidationError,
  NotFoundError,
  RateLimitError,
  QuotaExceededError,
  ApiError,
} from '../types';

export function errorHandler(
  error: Error,
  req: Request,
  res: Response,
  next: NextFunction
) {
  console.error('Error:', error);

  // Handle Zod validation errors
  if (error instanceof ZodError) {
    const errors = error.errors.map((err) => ({
      field: err.path.join('.'),
      message: err.message,
    }));

    return res.status(400).json({
      error: 'ValidationError',
      message: 'Validation failed',
      details: errors,
    });
  }

  // Handle custom errors
  if (error instanceof AuthenticationError) {
    return res.status(401).json({
      error: 'AuthenticationError',
      message: error.message,
    });
  }

  if (error instanceof AuthorizationError) {
    return res.status(403).json({
      error: 'AuthorizationError',
      message: error.message,
    });
  }

  if (error instanceof ValidationError) {
    return res.status(400).json({
      error: 'ValidationError',
      message: error.message,
    });
  }

  if (error instanceof NotFoundError) {
    return res.status(404).json({
      error: 'NotFoundError',
      message: error.message,
    });
  }

  if (error instanceof RateLimitError) {
    return res.status(429).json({
      error: 'RateLimitError',
      message: error.message,
    });
  }

  if (error instanceof QuotaExceededError) {
    return res.status(429).json({
      error: 'QuotaExceededError',
      message: error.message,
    });
  }

  // Handle Axios errors (from YouTube API)
  if ('isAxiosError' in error && error.isAxiosError) {
    const axiosError = error as unknown as { response?: { status: number; data?: { error?: { message?: string } } } };

    if (axiosError.response?.status === 403) {
      return res.status(429).json({
        error: 'QuotaExceededError',
        message: 'YouTube API quota exceeded',
      });
    }

    if (axiosError.response?.status === 404) {
      return res.status(404).json({
        error: 'NotFoundError',
        message: axiosError.response.data?.error?.message || 'Resource not found',
      });
    }

    return res.status(500).json({
      error: 'YouTubeApiError',
      message: axiosError.response?.data?.error?.message || 'YouTube API error',
    });
  }

  // Handle generic errors
  return res.status(500).json({
    error: 'InternalServerError',
    message: process.env.NODE_ENV === 'development' ? error.message : 'Something went wrong',
  });
}

// Async handler wrapper to catch errors
export function asyncHandler(
  fn: (req: Request, res: Response, next: NextFunction) => Promise<void>
): (req: Request, res: Response, next: NextFunction) => void {
  return async (req, res, next) => {
    try {
      await fn(req, res, next);
    } catch (error) {
      next(error);
    }
  };
}

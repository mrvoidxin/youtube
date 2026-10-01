import { Request, Response, NextFunction } from 'express';
import { verifyAccessToken } from '../utils/jwt';
import { AuthorizationError } from '../types';
import { prisma } from '../config/database';

interface AuthenticatedRequest extends Request {
  user?: {
    userId: string;
    email: string;
    displayName: string;
  };
}

export async function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  try {
    // Get token from header
    const authHeader = req.headers.authorization;
    const token = authHeader?.split('Bearer ')[1];

    if (!token) {
      throw new AuthorizationError('No authorization token provided');
    }

    // Verify token
    const payload = verifyAccessToken(token);

    // Check if user exists
    const user = await prisma.user.findUnique({
      where: { id: payload.userId },
    });

    if (!user) {
      throw new AuthorizationError('User not found');
    }

    // Attach user to request
    req.user = {
      userId: user.id,
      email: user.email,
      displayName: user.displayName,
    };

    next();
  } catch (error) {
    if (error instanceof AuthorizationError) {
      return res.status(401).json({ error: error.name, message: error.message });
    }
    return res.status(401).json({ error: 'Unauthorized', message: 'Invalid or expired token' });
  }
}

export async function optionalAuthenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  try {
    // Get token from header
    const authHeader = req.headers.authorization;
    const token = authHeader?.split('Bearer ')[1];

    if (!token) {
      return next();
    }

    // Verify token
    const payload = verifyAccessToken(token);

    // Check if user exists
    const user = await prisma.user.findUnique({
      where: { id: payload.userId },
    });

    if (!user) {
      return next();
    }

    // Attach user to request
    req.user = {
      userId: user.id,
      email: user.email,
      displayName: user.displayName,
    };

    next();
  } catch (error) {
    // If authentication fails, just continue without user
    next();
  }
}

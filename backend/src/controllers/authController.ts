import { Router } from 'express';
import { authService } from '../services/AuthService';
import { asyncHandler } from '../middleware/errorHandler';
import { validate } from '../utils/validation';
import {
  registerSchema,
  loginSchema,
  googleAuthSchema,
  refreshTokenSchema,
} from '../utils/validation';
import { authenticate } from '../middleware/authenticate';

const router = Router();

// POST /api/auth/register
router.post(
  '/register',
  asyncHandler(async (req, res) => {
    const data = validate(registerSchema, req.body);
    const result = await authService.register(data);
    res.json(result);
  })
);

// POST /api/auth/login
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const data = validate(loginSchema, req.body);
    const result = await authService.login(data);
    res.json(result);
  })
);

// POST /api/auth/google
router.post(
  '/google',
  asyncHandler(async (req, res) => {
    const data = validate(googleAuthSchema, req.body);
    const result = await authService.googleAuth(data.credential);
    res.json(result);
  })
);

// POST /api/auth/refresh
router.post(
  '/refresh',
  asyncHandler(async (req, res) => {
    const data = validate(refreshTokenSchema, req.body);
    const result = await authService.refreshToken(data.refreshToken);
    res.json(result);
  })
);

// POST /api/auth/logout
router.post(
  '/logout',
  authenticate,
  asyncHandler(async (req, res) => {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    const authHeader = req.headers.authorization;
    const token = authHeader?.split('Bearer ')[1];

    await authService.logout(userId, token);
    res.json({ message: 'Logged out successfully' });
  })
);

// GET /api/auth/me
router.get(
  '/me',
  authenticate,
  asyncHandler(async (req, res) => {
    const userId = req.user?.userId;
    if (!userId) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    const profile = await authService.getProfile(userId);
    res.json(profile);
  })
);

export { router as authRouter };

import bcrypt from 'bcrypt';
import { v4 as uuidv4 } from 'uuid';
import { prisma } from '../config/database';
import { generateAccessToken, generateRefreshToken, verifyRefreshToken } from '../utils/jwt';
import { AuthenticationError, AuthorizationError, ValidationError } from '../types';
import { RegisterRequest, LoginRequest, AuthResponse, UserProfile } from '../types';

class AuthService {
  // ============================================
  // REGISTRATION
  // ============================================

  async register(data: RegisterRequest): Promise<AuthResponse> {
    // Check if email already exists
    const existingUser = await prisma.user.findUnique({
      where: { email: data.email },
    });

    if (existingUser) {
      throw new ValidationError('Email already registered');
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(data.password, 10);

    // Create user
    const user = await prisma.user.create({
      data: {
        email: data.email,
        passwordHash: hashedPassword,
        displayName: data.displayName,
      },
    });

    // Generate tokens
    const tokens = this.generateTokens(user);

    // Store refresh token in database
    await prisma.refreshToken.create({
      data: {
        token: tokens.refreshToken,
        userId: user.id,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
      },
    });

    return {
      user: this.sanitizeUser(user),
      ...tokens,
      expiresIn: 15 * 60, // 15 minutes
    };
  }

  // ============================================
  // LOGIN
  // ============================================

  async login(data: LoginRequest): Promise<AuthResponse> {
    // Find user by email
    const user = await prisma.user.findUnique({
      where: { email: data.email },
    });

    if (!user || !user.passwordHash) {
      throw new AuthenticationError('Invalid email or password');
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(data.password, user.passwordHash);

    if (!isPasswordValid) {
      throw new AuthenticationError('Invalid email or password');
    }

    // Generate tokens
    const tokens = this.generateTokens(user);

    // Invalidate old refresh tokens and create new one
    await prisma.refreshToken.deleteMany({
      where: { userId: user.id },
    });

    await prisma.refreshToken.create({
      data: {
        token: tokens.refreshToken,
        userId: user.id,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
      },
    });

    return {
      user: this.sanitizeUser(user),
      ...tokens,
      expiresIn: 15 * 60, // 15 minutes
    };
  }

  // ============================================
  // GOOGLE SIGN-IN
  // ============================================

  async googleAuth(credential: string): Promise<AuthResponse> {
    // In a real implementation, we would verify the Google ID token here
    // For now, we'll parse the JWT and extract user info
    // Note: In production, you should use google-auth-library to verify the token

    try {
      // Parse the JWT (this is a simplified approach)
      // In production, use: const ticket = await client.verifyIdToken({ idToken: credential, audience: CLIENT_ID });
      const payload = Buffer.from(credential.split('.')[1], 'base64').toString('utf-8');
      const tokenData = JSON.parse(payload);

      const email = tokenData.email;
      const displayName = tokenData.name || tokenData.email;
      const avatarUrl = tokenData.picture;
      const googleId = tokenData.sub;

      if (!email || !googleId) {
        throw new AuthenticationError('Invalid Google credential');
      }

      // Find or create user
      let user = await prisma.user.findUnique({
        where: { googleId },
      });

      if (!user) {
        // Check if email already exists
        const existingUser = await prisma.user.findUnique({
          where: { email },
        });

        if (existingUser) {
          // Link Google account to existing user
          user = await prisma.user.update({
            where: { email },
            data: { googleId },
          });
        } else {
          // Create new user
          user = await prisma.user.create({
            data: {
              email,
              googleId,
              displayName,
              avatarUrl,
            },
          });
        }
      }

      // Generate tokens
      const tokens = this.generateTokens(user);

      // Invalidate old refresh tokens and create new one
      await prisma.refreshToken.deleteMany({
        where: { userId: user.id },
      });

      await prisma.refreshToken.create({
        data: {
          token: tokens.refreshToken,
          userId: user.id,
          expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
        },
      });

      return {
        user: this.sanitizeUser(user),
        ...tokens,
        expiresIn: 15 * 60, // 15 minutes
      };
    } catch (error) {
      console.error('Google auth error:', error);
      throw new AuthenticationError('Google authentication failed');
    }
  }

  // ============================================
  // REFRESH TOKEN
  // ============================================

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    // Verify refresh token
    let payload;
    try {
      payload = verifyRefreshToken(refreshToken);
    } catch (error) {
      throw new AuthorizationError('Invalid or expired refresh token');
    }

    // Find user by ID from token
    const user = await prisma.user.findUnique({
      where: { id: payload.userId },
    });

    if (!user) {
      throw new AuthorizationError('User not found');
    }

    // Check if refresh token exists in database
    const storedToken = await prisma.refreshToken.findUnique({
      where: { token: refreshToken },
    });

    if (!storedToken) {
      throw new AuthorizationError('Refresh token not found or revoked');
    }

    // Check if refresh token is expired
    if (storedToken.expiresAt < new Date()) {
      await prisma.refreshToken.delete({
        where: { id: storedToken.id },
      });
      throw new AuthorizationError('Refresh token expired');
    }

    // Generate new tokens
    const tokens = this.generateTokens(user);

    // Invalidate old refresh token and create new one
    await prisma.refreshToken.delete({
      where: { id: storedToken.id },
    });

    await prisma.refreshToken.create({
      data: {
        token: tokens.refreshToken,
        userId: user.id,
        expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days
      },
    });

    return {
      user: this.sanitizeUser(user),
      ...tokens,
      expiresIn: 15 * 60, // 15 minutes
    };
  }

  // ============================================
  // LOGOUT
  // ============================================

  async logout(userId: string, refreshToken?: string): Promise<void> {
    if (refreshToken) {
      // Delete specific refresh token
      await prisma.refreshToken.deleteMany({
        where: { token: refreshToken, userId },
      });
    } else {
      // Delete all refresh tokens for user
      await prisma.refreshToken.deleteMany({
        where: { userId },
      });
    }
  }

  // ============================================
  // GET USER PROFILE
  // ============================================

  async getProfile(userId: string): Promise<UserProfile> {
    const user = await prisma.user.findUnique({
      where: { id: userId },
    });

    if (!user) {
      throw new AuthorizationError('User not found');
    }

    return this.sanitizeUser(user);
  }

  // ============================================
  // HELPER METHODS
  // ============================================

  private generateTokens(user: { id: string; email: string; displayName: string }) {
    const payload = {
      userId: user.id,
      email: user.email,
      displayName: user.displayName,
    };

    const accessToken = generateAccessToken(payload);
    const refreshToken = generateRefreshToken(payload);

    return { accessToken, refreshToken };
  }

  private sanitizeUser(user: {
    id: string;
    email: string;
    displayName: string;
    avatarUrl?: string | null;
    createdAt: Date;
    passwordHash?: string | null;
    googleId?: string | null;
  }): UserProfile {
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      avatarUrl: user.avatarUrl || undefined,
      createdAt: user.createdAt,
    };
  }
}

// Singleton instance
export const authService = new AuthService();

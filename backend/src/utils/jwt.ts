import jwt from 'jsonwebtoken';
import { config } from '../config/env';

interface JwtPayload {
  userId: string;
  email: string;
  [key: string]: unknown;
}

export function generateAccessToken(payload: JwtPayload): string {
  return jwt.sign(payload, config.jwt.accessSecret, {
    expiresIn: config.jwt.accessExpiry,
  });
}

export function generateRefreshToken(payload: JwtPayload): string {
  return jwt.sign(payload, config.jwt.refreshSecret, {
    expiresIn: config.jwt.refreshExpiry,
  });
}

export function verifyAccessToken(token: string): JwtPayload {
  try {
    const decoded = jwt.verify(token, config.jwt.accessSecret) as JwtPayload;
    return decoded;
  } catch (error) {
    throw new Error('Invalid or expired access token');
  }
}

export function verifyRefreshToken(token: string): JwtPayload {
  try {
    const decoded = jwt.verify(token, config.jwt.refreshSecret) as JwtPayload;
    return decoded;
  } catch (error) {
    throw new Error('Invalid or expired refresh token');
  }
}

export function decodeToken(token: string): JwtPayload | null {
  try {
    const decoded = jwt.decode(token) as JwtPayload | null;
    return decoded || null;
  } catch (error) {
    return null;
  }
}

export function getTokenExpiry(token: string): Date | null {
  try {
    const decoded = jwt.decode(token) as jwt.JwtPayload | null;
    if (decoded && typeof decoded.exp === 'number') {
      return new Date(decoded.exp * 1000);
    }
    return null;
  } catch (error) {
    return null;
  }
}

import dotenv from 'dotenv';
import path from 'path';

// Load .env file
dotenv.config({ path: path.resolve(__dirname, '../../.env') });

// Validate required environment variables
const requiredEnvVars = [
  'YOUTUBE_API_KEY',
  'DATABASE_URL',
  'REDIS_URL',
  'JWT_ACCESS_SECRET',
  'JWT_REFRESH_SECRET',
];

for (const envVar of requiredEnvVars) {
  if (!process.env[envVar]) {
    console.warn(`Warning: Missing required environment variable: ${envVar}`);
  }
}

export const config = {
  // Server
  nodeEnv: process.env.NODE_ENV || 'development',
  port: process.env.PORT || '3000',
  
  // CORS
  corsOrigin: process.env.CORS_ORIGIN || 'http://localhost:3000,http://localhost:8080',
  
  // Rate limiting
  rateLimitWindowMs: process.env.RATE_LIMIT_WINDOW_MS || '900000', // 15 minutes
  rateLimitMaxRequests: process.env.RATE_LIMIT_MAX_REQUESTS || '100',
  
  // YouTube API
  youtubeApiKey: process.env.YOUTUBE_API_KEY || '',
  youtubeApiBase: process.env.YOUTUBE_API_BASE || 'https://www.googleapis.com/youtube/v3',
  
  // Database
  databaseUrl: process.env.DATABASE_URL || '',
  
  // Redis
  redisUrl: process.env.REDIS_URL || '',
  
  // JWT
  jwt: {
    accessSecret: process.env.JWT_ACCESS_SECRET || '',
    refreshSecret: process.env.JWT_REFRESH_SECRET || '',
    accessExpiry: process.env.JWT_ACCESS_EXPIRY || '15m',
    refreshExpiry: process.env.JWT_REFRESH_EXPIRY || '30d',
  },
  
  // Google OAuth
  google: {
    clientId: process.env.GOOGLE_OAUTH_CLIENT_ID || '',
    clientSecret: process.env.GOOGLE_OAUTH_CLIENT_SECRET || '',
  },
  
  // Cache TTLs (in seconds)
  cache: {
    searchResults: 600,      // 10 minutes
    videoStats: 300,        // 5 minutes
    channelInfo: 3600,      // 60 minutes
    videoDetails: 300,      // 5 minutes
    trending: 300,          // 5 minutes
    comments: 600,          // 10 minutes
  },
  
  // YouTube API quota limits
  youtubeQuota: {
    dailyLimit: 10000,
    perRequestCost: {
      search: 100,
      videos: 1,
      channels: 1,
      comments: 1,
    },
  },
};

export type Config = typeof config;

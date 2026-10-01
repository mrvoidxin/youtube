import { createClient, RedisClientType } from 'redis';
import { config } from './env';

let redisClient: RedisClientType | null = null;

export async function getRedisClient(): Promise<RedisClientType> {
  if (!redisClient) {
    redisClient = createClient({
      url: config.redisUrl,
      socket: {
        reconnectStrategy: (retries) => Math.min(retries * 100, 5000),
      },
    });

    redisClient.on('error', (err) => {
      console.error('Redis error:', err);
    });

    redisClient.on('connect', () => {
      console.log('Connected to Redis');
    });

    await redisClient.connect();
  }

  return redisClient;
}

export async function disconnectRedis(): Promise<void> {
  if (redisClient) {
    await redisClient.quit();
    redisClient = null;
  }
}

// Helper functions for caching
export async function getCached<T>(key: string): Promise<T | null> {
  try {
    const client = await getRedisClient();
    const value = await client.get(key);
    return value ? JSON.parse(value) : null;
  } catch (error) {
    console.error('Redis get error:', error);
    return null;
  }
}

export async function setCached<T>(key: string, value: T, ttl?: number): Promise<void> {
  try {
    const client = await getRedisClient();
    const serialized = JSON.stringify(value);
    if (ttl) {
      await client.setEx(key, ttl, serialized);
    } else {
      await client.set(key, serialized);
    }
  } catch (error) {
    console.error('Redis set error:', error);
  }
}

export async function deleteCached(key: string): Promise<void> {
  try {
    const client = await getRedisClient();
    await client.del(key);
  } catch (error) {
    console.error('Redis delete error:', error);
  }
}

export async function incrementQuota(endpoint: string): Promise<number> {
  try {
    const client = await getRedisClient();
    const key = `youtube:quota:${new Date().toISOString().split('T')[0]}:${endpoint}`;
    return await client.incr(key);
  } catch (error) {
    console.error('Redis increment error:', error);
    return 0;
  }
}

export async function getQuotaUsage(endpoint: string): Promise<number> {
  try {
    const client = await getRedisClient();
    const key = `youtube:quota:${new Date().toISOString().split('T')[0]}:${endpoint}`;
    const value = await client.get(key);
    return value ? parseInt(value) : 0;
  } catch (error) {
    console.error('Redis get quota error:', error);
    return 0;
  }
}

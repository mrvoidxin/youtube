import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { config } from '../config/env';
import { getCached, setCached, incrementQuota, getQuotaUsage } from '../config/redis';
import {
  YouTubeVideo,
  YouTubeChannel,
  YouTubeSearchResult,
  YouTubeComment,
  YouTubeApiResponse,
} from '../types';
import { QuotaExceededError } from '../types';

class YouTubeApiService {
  private client: AxiosInstance;
  private readonly QUOTA_LIMIT = config.youtubeQuota.dailyLimit;

  constructor() {
    this.client = axios.create({
      baseURL: config.youtubeApiBase,
      params: {
        key: config.youtubeApiKey,
      },
      timeout: 30000,
    });

    // Add request interceptor for logging
    this.client.interceptors.request.use((config) => {
      console.log(`YouTube API request: ${config.method?.toUpperCase()} ${config.url}`);
      return config;
    });

    // Add response interceptor for quota tracking
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 403) {
          const quotaUsage = await this.getTotalQuotaUsage();
          if (quotaUsage >= this.QUOTA_LIMIT) {
            throw new QuotaExceededError('YouTube API quota exceeded for today');
          }
        }
        throw error;
      }
    );
  }

  // ============================================
  // QUOTA MANAGEMENT
  // ============================================

  private async getTotalQuotaUsage(): Promise<number> {
    try {
      const endpoints = [
        'search.list',
        'videos.list',
        'channels.list',
        'commentThreads.list',
        'playlistItems.list',
      ];

      const total = await Promise.all(
        endpoints.map((endpoint) => getQuotaUsage(endpoint))
      );

      return total.reduce((sum, count) => sum + count, 0);
    } catch (error) {
      console.error('Error getting quota usage:', error);
      return 0;
    }
  }

  private async checkQuota(endpoint: string): Promise<void> {
    const usage = await getQuotaUsage(endpoint);
    const totalUsage = await this.getTotalQuotaUsage();

    if (totalUsage >= this.QUOTA_LIMIT) {
      throw new QuotaExceededError('YouTube API quota exceeded for today');
    }

    // Increment quota for this endpoint
    await incrementQuota(endpoint);
  }

  // ============================================
  // VIDEOS
  // ============================================

  async getTrendingVideos(
    part: string = 'snippet,contentDetails,statistics',
    chart: string = 'mostPopular',
    regionCode: string = 'US',
    maxResults: number = 20,
    pageToken?: string,
    videoCategoryId?: string
  ): Promise<YouTubeApiResponse<YouTubeVideo>> {
    const endpoint = 'videos.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:trending:${regionCode}:${videoCategoryId || 'all'}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeVideo>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeVideo>>(
        '/videos',
        {
          params: {
            part,
            chart,
            regionCode,
            maxResults,
            pageToken,
            videoCategoryId,
          },
        }
      );

      // Cache for 5 minutes
      await setCached(cacheKey, response.data, config.cache.trending);

      return response.data;
    } catch (error) {
      console.error('Error fetching trending videos:', error);
      throw error;
    }
  }

  async getVideosById(
    part: string = 'snippet,contentDetails,statistics,liveStreamingDetails',
    id: string | string[],
    maxResults: number = 1
  ): Promise<YouTubeApiResponse<YouTubeVideo>> {
    const endpoint = 'videos.list';
    await this.checkQuota(endpoint);

    const videoIds = Array.isArray(id) ? id.join(',') : id;
    const cacheKey = `youtube:videos:${videoIds}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeVideo>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeVideo>>(
        '/videos',
        {
          params: {
            part,
            id: videoIds,
            maxResults,
          },
        }
      );

      // Cache for 5 minutes
      await setCached(cacheKey, response.data, config.cache.videoDetails);

      return response.data;
    } catch (error) {
      console.error('Error fetching videos by ID:', error);
      throw error;
    }
  }

  async getRelatedVideos(
    part: string = 'snippet',
    relatedToVideoId: string,
    type: string = 'video',
    maxResults: number = 15,
    pageToken?: string
  ): Promise<YouTubeApiResponse<YouTubeSearchResult>> {
    const endpoint = 'search.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:related:${relatedToVideoId}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeSearchResult>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeSearchResult>>(
        '/search',
        {
          params: {
            part,
            relatedToVideoId,
            type,
            maxResults,
            pageToken,
          },
        }
      );

      // Cache for 10 minutes
      await setCached(cacheKey, response.data, config.cache.searchResults);

      return response.data;
    } catch (error) {
      console.error('Error fetching related videos:', error);
      throw error;
    }
  }

  // ============================================
  // SEARCH
  // ============================================

  async search(
    part: string = 'snippet',
    q: string,
    type: string = 'video',
    maxResults: number = 20,
    pageToken?: string,
    order: string = 'relevance',
    videoDuration?: string,
    regionCode: string = 'US',
    safeSearch: string = 'moderate'
  ): Promise<YouTubeApiResponse<YouTubeSearchResult>> {
    const endpoint = 'search.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:search:${q}:${type}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeSearchResult>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeSearchResult>>(
        '/search',
        {
          params: {
            part,
            q,
            type,
            maxResults,
            pageToken,
            order,
            videoDuration,
            regionCode,
            safeSearch,
          },
        }
      );

      // Cache for 10 minutes
      await setCached(cacheKey, response.data, config.cache.searchResults);

      return response.data;
    } catch (error) {
      console.error('Error searching:', error);
      throw error;
    }
  }

  async searchShorts(
    part: string = 'snippet',
    q: string,
    maxResults: number = 10,
    pageToken?: string,
    regionCode: string = 'US'
  ): Promise<YouTubeApiResponse<YouTubeSearchResult>> {
    const endpoint = 'search.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:search:shorts:${q}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeSearchResult>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeSearchResult>>(
        '/search',
        {
          params: {
            part,
            q,
            type: 'video',
            videoDuration: 'short',
            maxResults,
            pageToken,
            regionCode,
          },
        }
      );

      // Cache for 10 minutes
      await setCached(cacheKey, response.data, config.cache.searchResults);

      return response.data;
    } catch (error) {
      console.error('Error searching shorts:', error);
      throw error;
    }
  }

  // ============================================
  // CHANNELS
  // ============================================

  async getChannelDetails(
    part: string = 'snippet,statistics,brandingSettings',
    id: string | string[]
  ): Promise<YouTubeApiResponse<YouTubeChannel>> {
    const endpoint = 'channels.list';
    await this.checkQuota(endpoint);

    const channelIds = Array.isArray(id) ? id.join(',') : id;
    const cacheKey = `youtube:channel:${channelIds}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeChannel>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeChannel>>(
        '/channels',
        {
          params: {
            part,
            id: channelIds,
          },
        }
      );

      // Cache for 60 minutes
      await setCached(cacheKey, response.data, config.cache.channelInfo);

      return response.data;
    } catch (error) {
      console.error('Error fetching channel details:', error);
      throw error;
    }
  }

  // ============================================
  // COMMENTS
  // ============================================

  async getVideoComments(
    part: string = 'snippet,replies',
    videoId: string,
    maxResults: number = 50,
    pageToken?: string,
    order: string = 'relevance'
  ): Promise<YouTubeApiResponse<YouTubeComment>> {
    const endpoint = 'commentThreads.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:comments:${videoId}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeComment>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeComment>>(
        '/commentThreads',
        {
          params: {
            part,
            videoId,
            maxResults,
            pageToken,
            order,
          },
        }
      );

      // Cache for 10 minutes
      await setCached(cacheKey, response.data, config.cache.comments);

      return response.data;
    } catch (error) {
      console.error('Error fetching comments:', error);
      throw error;
    }
  }

  // ============================================
  // PLAYLISTS
  // ============================================

  async getPlaylistVideos(
    part: string = 'snippet,contentDetails',
    playlistId: string,
    maxResults: number = 20,
    pageToken?: string
  ): Promise<YouTubeApiResponse<YouTubeSearchResult>> {
    const endpoint = 'playlistItems.list';
    await this.checkQuota(endpoint);

    const cacheKey = `youtube:playlist:${playlistId}:${pageToken || '1'}`;
    const cached = await getCached<YouTubeApiResponse<YouTubeSearchResult>>(cacheKey);
    if (cached) {
      return cached;
    }

    try {
      const response = await this.client.get<YouTubeApiResponse<YouTubeSearchResult>>(
        '/playlistItems',
        {
          params: {
            part,
            playlistId,
            maxResults,
            pageToken,
          },
        }
      );

      // Cache for 10 minutes
      await setCached(cacheKey, response.data, config.cache.searchResults);

      return response.data;
    } catch (error) {
      console.error('Error fetching playlist videos:', error);
      throw error;
    }
  }
}

// Singleton instance
export const youtubeApiService = new YouTubeApiService();

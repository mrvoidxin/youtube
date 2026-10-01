// ============================================
// AUTHENTICATION TYPES
// ============================================

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface GoogleAuthRequest {
  credential: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  user: {
    id: string;
    email: string;
    displayName: string;
    avatarUrl?: string;
  };
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  createdAt: Date;
}

// ============================================
// YOUTUBE API TYPES
// ============================================

export interface YouTubeApiResponse<T> {
  kind: string;
  etag: string;
  nextPageToken?: string;
  prevPageToken?: string;
  pageInfo: {
    totalResults: number;
    resultsPerPage: number;
  };
  items: T[];
}

export interface YouTubeVideoSnippet {
  publishedAt: string;
  channelId: string;
  title: string;
  description: string;
  thumbnails: {
    default?: { url: string; width: number; height: number };
    medium?: { url: string; width: number; height: number };
    high?: { url: string; width: number; height: number };
    standard?: { url: string; width: number; height: number };
    maxres?: { url: string; width: number; height: number };
  };
  channelTitle: string;
  tags?: string[];
  categoryId: string;
  liveBroadcastContent: string;
  defaultLanguage?: string;
  localized?: {
    title?: string;
    description?: string;
  };
  defaultAudioLanguage?: string;
}

export interface YouTubeVideoContentDetails {
  duration: string;
  dimension: string;
  definition: string;
  caption: string;
  licensedContent: boolean;
  regionRestriction?: {
    allowed: string[];
    blocked: string[];
  };
  contentRating?: {
    acbRating?: string;
    agcomRating?: string;
    anacRating?: string;
    bbfcRating?: string;
    bfvcRating?: string;
    bmukkRating?: string;
    catvRating?: string;
    catvfrRating?: string;
    cbbfcRating?: string;
    cbfcRating?: string;
    cncRating?: string;
    csaRating?: string;
    cscfRating?: string;
    czfilmRating?: string;
    djctqRating?: string;
    djctqRatingReasons?: string[];
    ecbmctRating?: string;
    eirinRating?: string;
    fcacRating?: string;
    fccRating?: string;
    fmocRating?: string;
    fpbRating?: string;
    fskRating?: string;
    grfilmRating?: string;
    icaaRating?: string;
    ifcoRating?: string;
    ilfilmRating?: string;
    incaaRating?: string;
    kfcbRating?: string;
    kijkwijzerRating?: {
      label?: string;
      age?: number;
    };
    kzkmRating?: string;
    lsfRating?: string;
    mccaaRating?: string;
    mccypRating?: string;
    mecuRating?: string;
    mibacRating?: string;
    mocRating?: string;
    moctwRating?: string;
    mpaaRating?: string;
    mpaatRating?: string;
    oflcRating?: string;
    pefilmRating?: string;
    rcnRating?: string;
    russiaRating?: {
      rating?: string;
      advisoryCouncilRating?: string;
    };
    skfilmRating?: string;
    smsaRating?: string;
    tvpgRating?: string;
    ytRating?: string;
  };
  projection: string;
  hasCustomThumbnail: boolean;
}

export interface YouTubeVideoStatistics {
  viewCount: string;
  likeCount: string;
  dislikeCount?: string;
  favoriteCount: string;
  commentCount: string;
}

export interface YouTubeVideoLiveStreamingDetails {
  nextLiveStreamId?: string;
  activeLiveChatId?: string;
  scheduledStartTime?: string;
  scheduledEndTime?: string;
  concurrentViewers?: string;
  activeLiveStreamId?: string;
}

export interface YouTubeVideo {
  kind: string;
  etag: string;
  id: string;
  snippet: YouTubeVideoSnippet;
  contentDetails?: YouTubeVideoContentDetails;
  statistics?: YouTubeVideoStatistics;
  liveStreamingDetails?: YouTubeVideoLiveStreamingDetails;
}

export interface YouTubeChannelSnippet {
  title: string;
  description: string;
  customUrl?: string;
  publishedAt: string;
  thumbnails: {
    default?: { url: string; width: number; height: number };
    medium?: { url: string; width: number; height: number };
    high?: { url: string; width: number; height: number };
  };
  defaultLanguage?: string;
  localized?: {
    title?: string;
    description?: string;
  };
  country?: string;
}

export interface YouTubeChannelStatistics {
  viewCount: string;
  commentCount?: string;
  subscriberCount: string;
  hiddenSubscriberCount?: boolean;
  videoCount: string;
}

export interface YouTubeChannelBrandingSettings {
  channel?: {
    title: string;
    description: string;
    keywords: string;
    trackingAnalyticsAccountId: string;
    unlinkedChannels: string[];
    country: string;
  };
  image?: {
    bannerExternalUrl: string;
  };
}

export interface YouTubeChannel {
  kind: string;
  etag: string;
  id: string;
  snippet: YouTubeChannelSnippet;
  statistics?: YouTubeChannelStatistics;
  brandingSettings?: YouTubeChannelBrandingSettings;
}

export interface YouTubeSearchResultSnippet {
  publishedAt: string;
  channelId: string;
  title: string;
  description: string;
  thumbnails: {
    default?: { url: string; width: number; height: number };
    medium?: { url: string; width: number; height: number };
    high?: { url: string; width: number; height: number };
  };
  channelTitle: string;
  liveBroadcastContent: string;
  publishTime: string;
}

export interface YouTubeSearchResult {
  kind: string;
  etag: string;
  id: {
    kind: string;
    videoId?: string;
    channelId?: string;
    playlistId?: string;
  };
  snippet: YouTubeSearchResultSnippet;
}

export interface YouTubeCommentSnippet {
  videoId: string;
  topLevelComment: {
    kind: string;
    etag: string;
    id: string;
    snippet: {
      videoId: string;
      textDisplay: string;
      textOriginal: string;
      authorDisplayName: string;
      authorProfileImageUrl: string;
      authorChannelUrl: string;
      authorChannelId: {
        value: string;
      };
      canRate: boolean;
      viewerRating: string;
      likeCount: number;
      publishedAt: string;
      updatedAt: string;
    };
  };
  canReply: boolean;
  totalReplyCount: number;
  isPublic: boolean;
}

export interface YouTubeComment {
  kind: string;
  etag: string;
  id: string;
  snippet: YouTubeCommentSnippet;
  replies?: {
    comments: YouTubeComment[];
  };
}

// ============================================
// API REQUEST/RESPONSE TYPES
// ============================================

export interface PaginatedRequest {
  page?: number;
  pageSize?: number;
  pageToken?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  nextPageToken?: string;
  prevPageToken?: string;
  totalResults: number;
}

export interface VideoFilter {
  type?: 'video' | 'channel' | 'playlist';
  duration?: 'short' | 'medium' | 'long';
  order?: 'date' | 'rating' | 'relevance' | 'title' | 'videoCount' | 'viewCount';
  categoryId?: string;
}

export interface CommentFilter {
  order?: 'time' | 'relevance';
}

// ============================================
// DOMAIN MODELS
// ============================================

export interface VideoDomain {
  id: string;
  youtubeVideoId: string;
  channelId: string;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  duration: string | null;
  viewCount: number | null;
  likeCount: number | null;
  dislikeCount: number | null;
  commentCount: number | null;
  publishedAt: Date | null;
  isShort: boolean;
  categoryId: string | null;
  tags: string[];
  channel: {
    id: string;
    name: string;
    avatarUrl: string | null;
    subscriberCount: number | null;
  } | null;
  isLiked: boolean;
  isDisliked: boolean;
  isSubscribed: boolean;
}

export interface ChannelDomain {
  id: string;
  youtubeChannelId: string | null;
  name: string;
  bannerUrl: string | null;
  avatarUrl: string | null;
  description: string | null;
  subscriberCount: number | null;
  viewCount: number | null;
  videoCount: number | null;
  isSubscribed: boolean;
  subscriberCountText: string;
}

export interface CommentDomain {
  id: string;
  youtubeCommentId: string | null;
  videoId: string;
  user: {
    id: string;
    displayName: string;
    avatarUrl: string | null;
  };
  body: string;
  likeCount: number;
  createdAt: Date;
  replies: CommentDomain[];
  isLiked: boolean;
  isDisliked: boolean;
}

export interface SubscriptionDomain {
  id: string;
  channelId: string;
  channel: ChannelDomain;
  createdAt: Date;
}

export interface LikeDomain {
  id: string;
  targetType: string;
  targetId: string;
  value: string;
  createdAt: Date;
}

export interface WatchHistoryDomain {
  id: string;
  videoId: string;
  video: VideoDomain;
  watchedAt: Date;
  progressSeconds: number;
  duration: number | null;
}

export interface SearchHistoryDomain {
  id: string;
  query: string;
  searchedAt: Date;
}

// ============================================
// ERROR TYPES
// ============================================

export interface ApiError {
  error: string;
  message?: string;
  code?: string;
  details?: Record<string, unknown>;
}

export class AuthenticationError extends Error {
  constructor(message: string = 'Authentication failed') {
    super(message);
    this.name = 'AuthenticationError';
  }
}

export class AuthorizationError extends Error {
  constructor(message: string = 'Unauthorized') {
    super(message);
    this.name = 'AuthorizationError';
  }
}

export class ValidationError extends Error {
  constructor(message: string = 'Validation failed') {
    super(message);
    this.name = 'ValidationError';
  }
}

export class NotFoundError extends Error {
  constructor(message: string = 'Not found') {
    super(message);
    this.name = 'NotFoundError';
  }
}

export class RateLimitError extends Error {
  constructor(message: string = 'Rate limit exceeded') {
    super(message);
    this.name = 'RateLimitError';
  }
}

export class QuotaExceededError extends Error {
  constructor(message: string = 'YouTube API quota exceeded') {
    super(message);
    this.name = 'QuotaExceededError';
  }
}

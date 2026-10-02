export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  if (!process.env.YOUTUBE_API_KEY) return res.status(500).json({ error: 'YouTube API is not configured' });

  const maxResults = Math.min(Math.max(Number(req.query.pageSize) || 20, 1), 50);
  const params = new URLSearchParams({
    part: 'snippet,contentDetails,statistics',
    chart: 'mostPopular',
    regionCode: String(req.query.regionCode || 'US'),
    maxResults: String(maxResults),
    key: process.env.YOUTUBE_API_KEY,
  });
  if (req.query.categoryId) params.set('videoCategoryId', String(req.query.categoryId));
  if (req.query.pageToken) params.set('pageToken', String(req.query.pageToken));

  try {
    const response = await fetch(`${process.env.YOUTUBE_API_BASE || 'https://www.googleapis.com/youtube/v3'}/videos?${params}`);
    const data = await response.json();
    if (!response.ok) return res.status(response.status).json({ error: data.error?.message || 'YouTube feed failed' });
    return res.status(200).json({
      nextPageToken: data.nextPageToken || '',
      items: (data.items || []).map((item) => ({
        id: item.id,
        youtubeVideoId: item.id,
        title: item.snippet?.title,
        description: item.snippet?.description,
        channelTitle: item.snippet?.channelTitle,
        publishedAt: item.snippet?.publishedAt,
        duration: item.contentDetails?.duration,
        viewCount: item.statistics?.viewCount,
        thumbnailUrl: item.snippet?.thumbnails?.maxres?.url || item.snippet?.thumbnails?.high?.url || item.snippet?.thumbnails?.medium?.url,
      })),
    });
  } catch (error) {
    console.error('[v0] Feed proxy failed:', error);
    return res.status(502).json({ error: 'Unable to reach YouTube right now' });
  }
}

export const config = { runtime: 'nodejs20.x' };

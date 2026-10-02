export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  if (!process.env.YOUTUBE_API_KEY) return res.status(500).json({ error: 'YouTube API is not configured' });
  const q = String(req.query.q || '').trim().slice(0, 200);
  if (!q) return res.status(400).json({ error: 'Search query is required' });

  const params = new URLSearchParams({
    part: 'snippet', type: 'video', order: String(req.query.order || 'relevance'),
    maxResults: String(Math.min(Math.max(Number(req.query.pageSize) || 20, 1), 50)), q,
    key: process.env.YOUTUBE_API_KEY,
  });
  for (const key of ['pageToken', 'publishedAfter', 'publishedBefore', 'videoDuration', 'videoDefinition']) {
    if (req.query[key]) params.set(key, String(req.query[key]));
  }

  try {
    const response = await fetch(`${process.env.YOUTUBE_API_BASE || 'https://www.googleapis.com/youtube/v3'}/search?${params}`);
    const data = await response.json();
    if (!response.ok) return res.status(response.status).json({ error: data.error?.message || 'YouTube search failed' });
    return res.status(200).json({
      nextPageToken: data.nextPageToken || '',
      items: (data.items || []).filter((item) => item.id?.videoId).map((item) => ({
        id: item.id.videoId, youtubeVideoId: item.id.videoId, title: item.snippet?.title,
        description: item.snippet?.description, channelTitle: item.snippet?.channelTitle,
        publishedAt: item.snippet?.publishedAt,
        thumbnailUrl: item.snippet?.thumbnails?.high?.url || item.snippet?.thumbnails?.medium?.url || item.snippet?.thumbnails?.default?.url,
      })),
    });
  } catch (error) {
    console.error('[v0] Search proxy failed:', error);
    return res.status(502).json({ error: 'Unable to reach YouTube right now' });
  }
}

export const config = { runtime: 'nodejs20.x' };

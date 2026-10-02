export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  const params = new URLSearchParams({ part: 'snippet', type: 'video', maxResults: String(Math.min(Number(req.query.pageSize) || 20, 50)), q: req.query.categoryId ? '' : 'trending videos', key: process.env.YOUTUBE_API_KEY });
  if (req.query.categoryId) params.set('videoCategoryId', String(req.query.categoryId));
  if (req.query.pageToken) params.set('pageToken', String(req.query.pageToken));
  const response = await fetch(`${process.env.YOUTUBE_API_BASE || 'https://www.googleapis.com/youtube/v3'}/search?${params}`);
  if (!response.ok) return res.status(response.status).json({ error: 'YouTube feed failed' });
  const data = await response.json();
  return res.status(200).json({ nextPageToken: data.nextPageToken || '', items: (data.items || []).map((item) => ({ id: item.id.videoId, youtubeVideoId: item.id.videoId, title: item.snippet.title, description: item.snippet.description, channelTitle: item.snippet.channelTitle, publishedAt: item.snippet.publishedAt, thumbnailUrl: item.snippet.thumbnails?.high?.url || item.snippet.thumbnails?.medium?.url || item.snippet.thumbnails?.default?.url })) });
}

export const config = { runtime: 'nodejs20.x' };

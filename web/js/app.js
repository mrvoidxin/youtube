const videos = [
  { id: 'dQw4w9WgXcQ', title: 'The art of starting before you feel ready', channel: 'The Slow Down', creator: 'Maya Chen', views: '2.4M views', date: '3 days ago', duration: '12:48', category: 'Design', description: 'A thoughtful field guide to making space for better work, better ideas, and a little more courage.', avatar: 'https://i.pravatar.cc/80?img=47', thumb: 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=900&q=80' },
  { id: '9bZkp7q19s4', title: 'I built a tiny house in the mountains', channel: 'Field Notes', creator: 'Noah Williams', views: '891K views', date: '1 week ago', duration: '18:21', category: 'Travel', description: 'One year, one empty plot, and a lot of lessons about building a life with less noise.', avatar: 'https://i.pravatar.cc/80?img=12', thumb: 'https://images.unsplash.com/photo-1449158743715-0a90ebb6d2d8?auto=format&fit=crop&w=900&q=80' },
  { id: '5JnMutdy6Yw', title: 'The future of AI is more human than you think', channel: 'Signal / Noise', creator: 'Ari Patel', views: '1.1M views', date: '5 days ago', duration: '24:05', category: 'Technology', description: 'A clear, optimistic look at the tools changing how we create, work, and connect.', avatar: 'https://i.pravatar.cc/80?img=32', thumb: 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=900&q=80' },
  { id: 'kJQP7kiw5Fk', title: 'How I plan my week without burning out', channel: 'Sunday Studio', creator: 'Lena Ortiz', views: '430K views', date: '2 weeks ago', duration: '09:42', category: 'Wellness', description: 'A gentle weekly reset for people who want to do meaningful work without running on empty.', avatar: 'https://i.pravatar.cc/80?img=49', thumb: 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80' },
  { id: '3PHXvlpOkf4', title: 'Tokyo at 5am: a quiet city guide', channel: 'After Hours', creator: 'Kenji Watanabe', views: '678K views', date: '4 days ago', duration: '15:16', category: 'Travel', description: 'The city before the crowds: coffee, train lines, and the best light in Tokyo.', avatar: 'https://i.pravatar.cc/80?img=11', thumb: 'https://images.unsplash.com/photo-1519501025264-65ba15a82390?auto=format&fit=crop&w=900&q=80' },
  { id: 'F9UG9XX9FIA', title: 'The playlist that changed my year', channel: 'Good Company', creator: 'Nia Rivers', views: '2.8M views', date: '1 month ago', duration: '31:08', category: 'Music', description: 'Twenty songs, five memories, and one very good year in review.', avatar: 'https://i.pravatar.cc/80?img=44', thumb: 'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?auto=format&fit=crop&w=900&q=80' },
  { id: 'mFQP7N2pE4g', title: 'A practical guide to making things beautiful', channel: 'Studio Forma', creator: 'Studio Forma', views: '944K views', date: '6 days ago', duration: '20:10', category: 'Design', description: 'The small decisions that make a big difference in product, space, and visual design.', avatar: 'https://i.pravatar.cc/80?img=5', thumb: 'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=900&q=80' },
  { id: 'Hzevd8l3x7M', title: 'Why everyone is talking about indie games', channel: 'Pixel Common', creator: 'Pixel Common', views: '1.7M views', date: '3 weeks ago', duration: '14:33', category: 'Gaming', description: 'The tiny teams, big ideas, and strange new worlds behind the indie game renaissance.', avatar: 'https://i.pravatar.cc/80?img=68', thumb: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=900&q=80' }
];
const categories = ['All', 'Music', 'Mixes', 'Live', 'Gaming', 'News', 'Technology', 'Design', 'Travel', 'Recently uploaded'];
const state = { section: 'home', category: 'All', query: '', liked: false, subscribed: false, visible: 8, loading: false, apiItems: [], nextPageToken: '', requestId: 0, apiError: '' };
const $ = (id) => document.getElementById(id);
const categoryAliases = { News: ['Technology'], Live: ['Music'], Mixes: ['Music', 'Design', 'Technology'] };
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[char]));

function toast(message) { const element = $('toast'); if (!element) return; element.textContent = message; element.classList.add('show'); clearTimeout(window.toastTimer); window.toastTimer = setTimeout(() => element.classList.remove('show'), 2200); }
function filtered() {
  return state.apiItems;
}
function normalizeApiVideo(item) {
  const duration = item.duration ? formatDuration(item.duration) : '—';
  return { id: item.youtubeVideoId || item.id, title: item.title || 'Untitled video', channel: item.channel?.name || item.channelTitle || 'YouTube creator', creator: item.channel?.name || item.channelTitle || 'YouTube creator', views: `${Number(item.viewCount || 0).toLocaleString()} views`, date: item.publishedAt ? new Date(item.publishedAt).toLocaleDateString() : 'Recently uploaded', duration, category: 'YouTube', description: item.description || '', avatar: item.channel?.avatarUrl || 'https://www.gstatic.com/youtube/img/branding/youtubelogo/svg/youtubelogo.svg', thumb: item.thumbnailUrl };
}
function formatDuration(value) {
  const match = String(value).match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
  if (!match) return value;
  const parts = [Number(match[1] || 0), Number(match[2] || 0), Number(match[3] || 0)];
  return parts[0] ? `${parts[0]}:${String(parts[1]).padStart(2, '0')}:${String(parts[2]).padStart(2, '0')}` : `${parts[1]}:${String(parts[2]).padStart(2, '0')}`;
}
async function fetchCatalog({ reset = false } = {}) {
  const requestId = ++state.requestId;
  if (reset) { state.apiItems = []; state.nextPageToken = ''; state.apiError = ''; state.visible = 8; renderVideos(); }
  state.loading = true;
  try {
    const params = new URLSearchParams({ page: String(Math.floor(state.apiItems.length / 20) + 1), pageSize: '20' });
    if (state.query) params.set('q', state.query); else { const categoryIds = { Music: '10', Gaming: '20', News: '25', Technology: '28' }; if (categoryIds[state.category]) params.set('categoryId', categoryIds[state.category]); }
    if (state.nextPageToken) params.set('pageToken', state.nextPageToken);
    const endpoint = state.query ? `/api/search?${params}` : `/api/feed?${params}`;
    const response = await fetch(endpoint, { headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error(`API request failed (${response.status})`);
    const payload = await response.json();
    if (requestId !== state.requestId) return;
    const incoming = (payload.items || []).map(normalizeApiVideo).filter((item) => item.id && item.thumb);
    const existingIds = new Set(state.apiItems.map((item) => item.id));
    state.apiItems = [...state.apiItems, ...incoming.filter((item) => !existingIds.has(item.id))];
    state.nextPageToken = payload.nextPageToken || '';
    state.apiError = '';
    if (payload.isCached) toast('Showing cached YouTube results');
  } catch (error) {
    console.error('[v0] YouTube API request failed:', error);
    state.apiError = 'Live search is temporarily unavailable. Please try again.';
  } finally {
    if (requestId === state.requestId) { state.loading = false; renderVideos(); }
  }
}
function renderCategories() { $('categoryRow').innerHTML = categories.map((category) => `<button class="category-btn ${state.category === category ? 'active' : ''}" data-category="${category}">${category}</button>`).join(''); document.querySelectorAll('[data-category]').forEach((button) => button.addEventListener('click', () => { state.category = button.dataset.category; state.visible = 8; renderCategories(); renderVideos(); })); }
function renderVideos() {
  const matches = filtered();
  const list = matches.slice(0, state.visible);
  $('videoGrid').innerHTML = list.length ? list.map((video) => `<article class="video-card" data-video="${video.id}" tabindex="0"><div class="thumbnail"><img loading="lazy" src="${video.thumb}" alt="${escapeHtml(video.title)}"><span class="duration">${video.duration}</span></div><div class="video-details"><img class="creator-avatar" src="${video.avatar}" alt="${escapeHtml(video.channel)} avatar"><div><h3 class="video-title">${escapeHtml(video.title)}</h3><span class="channel-name">${escapeHtml(video.channel)} <i class="fas fa-check-circle" aria-label="Verified"></i></span><span class="video-meta">${video.views} · ${video.date}</span></div></div></article>`).join('') : `<div class="empty-state"><h3>${escapeHtml(state.apiError || 'No videos found')}</h3><p>${state.apiError ? 'Check your connection and try again.' : 'Try another search or topic.'}</p></div>`;
  document.querySelectorAll('.video-card').forEach((card) => { const open = () => openPlayer(matches.find((video) => video.id === card.dataset.video)); card.addEventListener('click', open); card.addEventListener('keydown', (event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); open(); } }); });
  $('loadMore').style.display = matches.length > state.visible ? 'block' : 'none';
}
function loadMore() { if (state.loading || (!state.nextPageToken && state.apiItems.length)) return; $('loadMore').classList.add('is-loading'); if (state.apiItems.length || state.query || state.category !== 'All') fetchCatalog(); else { state.visible += 8; $('loadMore').classList.remove('is-loading'); renderVideos(); } }
function openPlayer(video) { if (!video) return; $('videoFrame').src = `https://www.youtube.com/embed/${video.id}?autoplay=1&rel=0`; $('playerTitle').textContent = video.title; $('playerStats').textContent = `${video.channel} · ${video.views} · ${video.date}`; $('playerDescription').textContent = video.description; $('playerModal').classList.add('active'); $('playerModal').setAttribute('aria-hidden', 'false'); document.body.style.overflow = 'hidden'; }
function closePlayer() { $('videoFrame').src = ''; $('playerModal').classList.remove('active'); $('playerModal').setAttribute('aria-hidden', 'true'); document.body.style.overflow = ''; }
function setSection(section) { state.section = section; state.query = ''; $('searchInput').value = ''; state.category = { music: 'Music', gaming: 'Gaming', live: 'Live' }[section] || 'All'; state.visible = 8; document.querySelectorAll('.nav-item,.mobile-nav-item').forEach((item) => item.classList.toggle('active', item.dataset.section === section)); const titles = { home: ['Recommended', 'Home'], shorts: ['Quick hits', 'Shorts'], subscriptions: ['From your channels', 'Subscriptions'], history: ['Keep watching', 'History'], liked: ['Your favorites', 'Liked videos'], 'watch-later': ['Saved for later', 'Watch later'], music: ['Listen now', 'Music'], gaming: ['Press play', 'Gaming'], live: ['Happening now', 'Live'], library: ['Your collection', 'Library'] }; const [kicker, title] = titles[section] || titles.home; $('sectionKicker').textContent = kicker; $('sectionTitle').textContent = title; $('heroBanner').style.display = section === 'home' ? 'flex' : 'none'; renderCategories(); renderVideos(); fetchCatalog({ reset: true }); $('sidebar').classList.remove('open'); $('overlay').classList.remove('open'); }
function search() { state.query = $('searchInput').value.trim(); state.section = 'home'; state.category = 'All'; state.apiItems = []; state.nextPageToken = ''; state.visible = 8; document.querySelectorAll('.nav-item,.mobile-nav-item').forEach((item) => item.classList.toggle('active', item.dataset.section === 'home'));   $('suggestions').hidden = true; $('sectionKicker').textContent = state.query ? 'Search results' : 'Recommended'; $('sectionTitle').textContent = state.query ? `Results for “${state.query}”` : 'Home'; $('heroBanner').style.display = state.query ? 'none' : 'flex'; renderCategories(); renderVideos(); fetchCatalog({ reset: true }); }

$('searchForm').addEventListener('submit', (event) => { event.preventDefault(); search(); });
$('searchInput').addEventListener('input', (event) => { const query = event.target.value.trim().toLowerCase(); if (!query) { $('suggestions').hidden = true; return; } const matches = videos.filter((video) => `${video.title} ${video.creator} ${video.channel}`.toLowerCase().includes(query)).slice(0, 5); $('suggestions').innerHTML = matches.map((video) => `<button class="suggestion" data-suggest="${escapeHtml(video.title)}"><i class="fas fa-magnifying-glass"></i> ${escapeHtml(video.title)}</button>`).join(''); $('suggestions').hidden = !matches.length; document.querySelectorAll('[data-suggest]').forEach((button) => button.addEventListener('click', () => { $('searchInput').value = button.dataset.suggest; search(); })); });
$('menuBtn').addEventListener('click', () => { $('sidebar').classList.toggle('open'); $('overlay').classList.toggle('open'); });
$('overlay').addEventListener('click', () => { $('sidebar').classList.remove('open'); $('overlay').classList.remove('open'); });
$('closePlayer').addEventListener('click', closePlayer); $('playerModal').addEventListener('click', (event) => { if (event.target === $('playerModal')) closePlayer(); });
document.addEventListener('keydown', (event) => { if (event.key === 'Escape') { closePlayer(); $('suggestions').hidden = true; } });
document.querySelectorAll('.nav-item,.mobile-nav-item').forEach((item) => item.addEventListener('click', () => setSection(item.dataset.section)));
$('brand').addEventListener('click', () => setSection('home')); $('heroBtn').addEventListener('click', () => $('sectionTitle').scrollIntoView({ behavior: 'smooth' })); $('viewAllBtn').addEventListener('click', () => $('videoGrid').scrollIntoView({ behavior: 'smooth' })); $('loadMore').addEventListener('click', loadMore);
const infiniteObserver = new IntersectionObserver((entries) => { if (entries[0].isIntersecting) loadMore(); }, { rootMargin: '500px' }); infiniteObserver.observe($('loadMore'));
$('playerSubscribe').addEventListener('click', () => { state.subscribed = !state.subscribed; $('playerSubscribe').textContent = state.subscribed ? 'Subscribed' : 'Subscribe'; toast(state.subscribed ? 'You are now subscribed' : 'Subscription removed'); });
$('likeBtn').addEventListener('click', () => { state.liked = !state.liked; $('likeBtn').classList.toggle('liked', state.liked); $('likeBtn').innerHTML = state.liked ? '<i class="fas fa-thumbs-up"></i><span>Liked</span>' : '<i class="far fa-thumbs-up"></i><span>Like</span>'; toast(state.liked ? 'Added to liked videos' : 'Removed from liked videos'); });
$('saveBtn').addEventListener('click', () => toast('Saved to Watch later'));
$('shareBtn').addEventListener('click', async () => { try { await navigator.clipboard.writeText(window.location.href); toast('Link copied'); } catch (error) { toast('Share link ready'); } });
$('createBtn').addEventListener('click', () => toast('Create menu opened')); $('notificationBtn').addEventListener('click', () => toast('No new notifications')); $('profileBtn').addEventListener('click', () => toast('Account menu opened')); $('voiceBtn').addEventListener('click', () => toast('Voice search is not available in this preview'));
renderCategories(); renderVideos();
fetchCatalog({ reset: true });
window.addEventListener('error', (event) => { console.error('[v0] UI error:', event.error || event.message); });
window.addEventListener('unhandledrejection', (event) => { console.error('[v0] Async UI error:', event.reason); });

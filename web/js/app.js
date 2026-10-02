// Sample video data
const sampleVideos = [
    {
        id: 'dQw4w9WgXcQ',
        title: 'Rick Astley - Never Gonna Give You Up (Official Music Video)',
        thumbnail: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg',
        duration: '3:33',
        views: '1.2B',
        date: 'Oct 25, 2009',
        channel: {
            name: 'RickAstleyVEVO',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '1.5M'
        },
        description: 'The official music video for Rick Astley's 1987 hit single "Never Gonna Give You Up".'
    },
    {
        id: '9bZkp7q19s4',
        title: 'Python for Beginners - Full Course',
        thumbnail: 'https://i.ytimg.com/vi/9bZkp7q19s4/hqdefault.jpg',
        duration: '4:15:30',
        views: '25M',
        date: 'Nov 10, 2022',
        channel: {
            name: 'freeCodeCamp.org',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '10M'
        },
        description: 'Learn Python for beginners in this full course. Python is one of the most popular programming languages.'
    },
    {
        id: '5JnMutdy6Yw',
        title: 'Machine Learning Tutorial for Beginners',
        thumbnail: 'https://i.ytimg.com/vi/5JnMutdy6Yw/hqdefault.jpg',
        duration: '15:30',
        views: '10M',
        date: 'Jan 15, 2023',
        channel: {
            name: 'Sentdex',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '1.2M'
        },
        description: 'Learn the basics of machine learning in this comprehensive tutorial.'
    },
    {
        id: 'kJQP7kiw5Fk',
        title: 'Learn CSS in 20 Minutes',
        thumbnail: 'https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg',
        duration: '20:15',
        views: '5M',
        date: 'Mar 15, 2023',
        channel: {
            name: 'Web Dev Simplified',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '1M'
        },
        description: 'Learn CSS basics in just 20 minutes with this crash course.'
    },
    {
        id: '3PHXvlpOkf4',
        title: 'JavaScript Crash Course For Beginners',
        thumbnail: 'https://i.ytimg.com/vi/3PHXvlpOkf4/hqdefault.jpg',
        duration: '1:30:00',
        views: '15M',
        date: 'Feb 20, 2023',
        channel: {
            name: 'Traversy Media',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '2M'
        },
        description: 'JavaScript crash course for absolute beginners. Learn JavaScript in one hour.'
    },
    {
        id: 'F9UG9XX9FIA',
        title: 'React JS Full Course for Beginners',
        thumbnail: 'https://i.ytimg.com/vi/F9UG9XX9FIA/hqdefault.jpg',
        duration: '8:00:00',
        views: '8M',
        date: 'Apr 1, 2023',
        channel: {
            name: 'Clever Programmer',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '800K'
        },
        description: 'Complete React JS course for beginners. Learn React from scratch.'
    },
    {
        id: 'mFQP7N2pE4g',
        title: 'HTML and CSS Tutorial for Beginners',
        thumbnail: 'https://i.ytimg.com/vi/mFQP7N2pE4g/hqdefault.jpg',
        duration: '1:10:00',
        views: '3M',
        date: 'May 1, 2023',
        channel: {
            name: 'Programming with Mosh',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '1.5M'
        },
        description: 'Complete HTML and CSS tutorial for beginners. Build real projects.'
    },
    {
        id: 'Hzevd8l3x7M',
        title: 'How to Make a Website - Full HTML & CSS Course',
        thumbnail: 'https://i.ytimg.com/vi/Hzevd8l3x7M/hqdefault.jpg',
        duration: '2:30:00',
        views: '12M',
        date: 'Jun 1, 2023',
        channel: {
            name: 'freeCodeCamp.org',
            avatar: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
            subscribers: '10M'
        },
        description: 'Learn how to make a website from scratch using HTML and CSS.'
    }
];

// DOM Elements
const menuBtn = document.getElementById('menuBtn');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('overlay');
const closePlayer = document.getElementById('closePlayer');
const playerModal = document.getElementById('playerModal');
const videosGrid = document.getElementById('videosGrid');
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');

// Initialize the app
function init() {
    renderVideos(sampleVideos);
    setupEventListeners();
}

// Render videos to the grid
function renderVideos(videos) {
    videosGrid.innerHTML = '';
    
    if (videos.length === 0) {
        videosGrid.innerHTML = '<div class="loading"><p>No videos found</p></div>';
        return;
    }
    
    videos.forEach(video => {
        const videoCard = document.createElement('div');
        videoCard.className = 'video-card';
        videoCard.innerHTML = `
            <div class="video-thumbnail">
                <img src="${video.thumbnail}" alt="${video.title}">
                <span class="video-duration">${video.duration}</span>
            </div>
            <div class="video-info">
                <h3 class="video-title">${video.title}</h3>
                <div class="video-stats">
                    <span>${video.views} views</span>
                    <span>${video.date}</span>
                </div>
                <div class="channel-info">
                    <div class="channel-avatar">
                        <img src="${video.channel.avatar}" alt="${video.channel.name}">
                    </div>
                    <span class="channel-name">${video.channel.name}</span>
                </div>
            </div>
        `;
        
        videoCard.addEventListener('click', () => playVideo(video));
        videosGrid.appendChild(videoCard);
    });
}

// Play video in modal
function playVideo(video) {
    const videoFrame = document.getElementById('videoFrame');
    const videoTitle = document.getElementById('videoTitle');
    const videoViews = document.getElementById('videoViews');
    const videoDate = document.getElementById('videoDate');
    const channelName = document.getElementById('channelName');
    const channelSubs = document.getElementById('channelSubs');
    const videoDescription = document.getElementById('videoDescription');
    
    // Update video player
    videoFrame.src = `https://www.youtube.com/embed/${video.id}?autoplay=1`;
    videoTitle.textContent = video.title;
    videoViews.textContent = `${video.views} views`;
    videoDate.textContent = video.date;
    channelName.textContent = video.channel.name;
    channelSubs.textContent = `${video.channel.subscribers} subscribers`;
    videoDescription.innerHTML = `<p>${video.description}</p>`;
    
    // Show modal
    playerModal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// Setup event listeners
function setupEventListeners() {
    // Menu toggle
    menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('active');
        overlay.classList.toggle('active');
    });
    
    overlay.addEventListener('click', () => {
        sidebar.classList.remove('active');
        overlay.classList.remove('active');
    });
    
    // Close player modal
    closePlayer.addEventListener('click', () => {
        playerModal.classList.remove('active');
        document.body.style.overflow = '';
        const videoFrame = document.getElementById('videoFrame');
        videoFrame.src = '';
    });
    
    // Close modal on overlay click
    playerModal.addEventListener('click', (e) => {
        if (e.target === playerModal) {
            playerModal.classList.remove('active');
            document.body.style.overflow = '';
            const videoFrame = document.getElementById('videoFrame');
            videoFrame.src = '';
        }
    });
    
    // Close modal on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            playerModal.classList.remove('active');
            document.body.style.overflow = '';
            const videoFrame = document.getElementById('videoFrame');
            videoFrame.src = '';
        }
    });
    
    // Search functionality
    searchBtn.addEventListener('click', () => {
        const query = searchInput.value.trim();
        if (query) {
            searchVideos(query);
        }
    });
    
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const query = searchInput.value.trim();
            if (query) {
                searchVideos(query);
            }
        }
    });
    
    // Category filter
    const categoryBtns = document.querySelectorAll('.category-btn');
    categoryBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            categoryBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const category = btn.dataset.category;
            filterVideos(category);
        });
    });
    
    // Navigation items
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            navItems.forEach(n => n.classList.remove('active'));
            item.classList.add('active');
            const section = item.dataset.section;
            loadSection(section);
            
            // Close sidebar on mobile
            if (window.innerWidth <= 768) {
                sidebar.classList.remove('active');
                overlay.classList.remove('active');
            }
        });
    });
}

// Search videos
function searchVideos(query) {
    // In a real app, this would call your backend API
    console.log('Searching for:', query);
    
    // For demo, filter sample videos
    const results = sampleVideos.filter(video => 
        video.title.toLowerCase().includes(query.toLowerCase()) ||
        video.channel.name.toLowerCase().includes(query.toLowerCase())
    );
    
    renderVideos(results);
}

// Filter videos by category
function filterVideos(category) {
    if (category === 'all') {
        renderVideos(sampleVideos);
        return;
    }
    
    // In a real app, this would call your backend API
    const filtered = sampleVideos.filter(video => {
        const title = video.title.toLowerCase();
        return title.includes(category);
    });
    
    renderVideos(filtered);
}

// Load section
function loadSection(section) {
    console.log('Loading section:', section);
    
    // For demo, just show all videos
    if (section === 'home') {
        renderVideos(sampleVideos);
    } else {
        // Show loading state
        videosGrid.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading...</p></div>';
        
        // Simulate loading
        setTimeout(() => {
            renderVideos(sampleVideos.slice(0, 4));
        }, 1000);
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', init);

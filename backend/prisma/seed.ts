import { PrismaClient, User, Channel, Video } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  console.log('Seeding database...');

  // Create test users
  const hashedPassword = await bcrypt.hash('password123', 10);
  
  const users: User[] = await Promise.all([
    prisma.user.upsert({
      where: { email: 'user1@example.com' },
      update: {},
      create: {
        email: 'user1@example.com',
        passwordHash: hashedPassword,
        displayName: 'Test User 1',
        avatarUrl: 'https://i.pravatar.cc/150?img=1',
      },
    }),
    prisma.user.upsert({
      where: { email: 'user2@example.com' },
      update: {},
      create: {
        email: 'user2@example.com',
        passwordHash: hashedPassword,
        displayName: 'Test User 2',
        avatarUrl: 'https://i.pravatar.cc/150?img=2',
      },
    }),
  ]);

  console.log(`Created ${users.length} users`);

  // Create test channels
  const channels: Channel[] = await Promise.all([
    prisma.channel.upsert({
      where: { youtubeChannelId: 'UC-9-kyTW8ZkZNDHQJ6FgpwQ' },
      update: {},
      create: {
        ownerUserId: users[0].id,
        youtubeChannelId: 'UC-9-kyTW8ZkZNDHQJ6FgpwQ',
        name: 'Sentdex',
        bannerUrl: 'https://i.ytimg.com/vi/5JnMutdy6Yw/maxresdefault.jpg',
        avatarUrl: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
        description: 'Tutorials on Python, Machine Learning, and more',
        subscriberCount: 1200000,
        viewCount: 500000000,
        videoCount: 1500,
      },
    }),
    prisma.channel.upsert({
      where: { youtubeChannelId: 'UCsXVk37bltHxD1pJxrfXy9A' },
      update: {},
      create: {
        ownerUserId: users[1].id,
        youtubeChannelId: 'UCsXVk37bltHxD1pJxrfXy9A',
        name: 'Kurtis Conner',
        bannerUrl: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg',
        avatarUrl: 'https://yt3.ggpht.com/ytc/AKedOLQn9n4G2X20sXQJ6FgpwQ',
        description: 'Educational content about computers and technology',
        subscriberCount: 500000,
        viewCount: 200000000,
        videoCount: 800,
      },
    }),
  ]);

  console.log(`Created ${channels.length} channels`);

  // Create test videos
  const videos: Video[] = await Promise.all([
    prisma.video.upsert({
      where: { youtubeVideoId: '5JnMutdy6Yw' },
      update: {},
      create: {
        youtubeVideoId: '5JnMutdy6Yw',
        channelId: channels[0].id,
        title: 'Machine Learning Tutorial for Beginners',
        description: 'Learn the basics of machine learning in this comprehensive tutorial',
        thumbnailUrl: 'https://i.ytimg.com/vi/5JnMutdy6Yw/maxresdefault.jpg',
        duration: 'PT15M30S',
        viewCount: 1000000,
        likeCount: 50000,
        dislikeCount: 500,
        commentCount: 2000,
        publishedAt: new Date('2023-01-15'),
        isShort: false,
        categoryId: '27',
        tags: ['machine learning', 'python', 'tutorial', 'AI'],
        statsJson: { engagement: 0.85 },
      },
    }),
    prisma.video.upsert({
      where: { youtubeVideoId: 'dQw4w9WgXcQ' },
      update: {},
      create: {
        youtubeVideoId: 'dQw4w9WgXcQ',
        channelId: channels[1].id,
        title: 'How Computers Work',
        description: 'A deep dive into how computers actually work',
        thumbnailUrl: 'https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg',
        duration: 'PT20M',
        viewCount: 5000000,
        likeCount: 250000,
        dislikeCount: 2000,
        commentCount: 10000,
        publishedAt: new Date('2023-02-20'),
        isShort: false,
        categoryId: '28',
        tags: ['computers', 'technology', 'education'],
        statsJson: { engagement: 0.92 },
      },
    }),
    prisma.video.upsert({
      where: { youtubeVideoId: '9bZkp7q19s4' },
      update: {},
      create: {
        youtubeVideoId: '9bZkp7q19s4',
        channelId: channels[0].id,
        title: 'Python for Beginners - Full Course',
        description: 'Complete Python course for absolute beginners',
        thumbnailUrl: 'https://i.ytimg.com/vi/9bZkp7q19s4/maxresdefault.jpg',
        duration: 'PT4H15M',
        viewCount: 2000000,
        likeCount: 100000,
        dislikeCount: 1000,
        commentCount: 5000,
        publishedAt: new Date('2022-11-10'),
        isShort: false,
        categoryId: '27',
        tags: ['python', 'programming', 'course', 'beginner'],
        statsJson: { engagement: 0.78 },
      },
    }),
  ]);

  console.log(`Created ${videos.length} videos`);

  // Create subscriptions
  await prisma.subscription.upsert({
    where: { userId_channelId: { userId: users[0].id, channelId: channels[1].id } },
    update: {},
    create: {
      userId: users[0].id,
      channelId: channels[1].id,
    },
  });

  await prisma.subscription.upsert({
    where: { userId_channelId: { userId: users[1].id, channelId: channels[0].id } },
    update: {},
    create: {
      userId: users[1].id,
      channelId: channels[0].id,
    },
  });

  console.log('Created subscriptions');

  // Create comments
  await prisma.comment.createMany({
    data: [
      {
        userId: users[0].id,
        videoId: videos[0].id,
        body: 'Great tutorial! Very helpful for beginners.',
        likeCount: 100,
      },
      {
        userId: users[1].id,
        videoId: videos[0].id,
        body: 'Thanks for explaining machine learning so clearly!',
        likeCount: 50,
      },
      {
        userId: users[0].id,
        videoId: videos[1].id,
        body: 'This is exactly what I needed to understand computers better.',
        likeCount: 200,
      },
    ],
  });

  console.log('Created comments');

  // Create likes
  await prisma.like.createMany({
    data: [
      { userId: users[0].id, targetType: 'video', targetId: videos[0].id, value: 'like' },
      { userId: users[1].id, targetType: 'video', targetId: videos[1].id, value: 'like' },
      { userId: users[0].id, targetType: 'video', targetId: videos[2].id, value: 'like' },
    ],
  });

  console.log('Created likes');

  // Create watch history
  await prisma.watchHistory.createMany({
    data: [
      { userId: users[0].id, videoId: videos[0].id, progressSeconds: 300, duration: 930 },
      { userId: users[0].id, videoId: videos[1].id, progressSeconds: 600, duration: 1200 },
      { userId: users[1].id, videoId: videos[2].id, progressSeconds: 150, duration: 15300 },
    ],
  });

  console.log('Created watch history');

  console.log('Seeding completed!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });

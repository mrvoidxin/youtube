import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import rateLimit from 'express-rate-limit';
import { createServer } from 'http';
import { config } from './config/env';
import { errorHandler } from './middleware/errorHandler';
import { authRouter } from './controllers/authController';
import { feedRouter } from './controllers/feedController';
import { searchRouter } from './controllers/searchController';
import { videosRouter } from './controllers/videosController';
import { channelsRouter } from './controllers/channelsController';
import { commentsRouter } from './controllers/commentsController';
import { historyRouter } from './controllers/historyController';

const app = express();
const httpServer = createServer(app);

// ============================================
// MIDDLEWARE
// ============================================

// Security
app.use(helmet());

// CORS
app.use(cors({
  origin: config.corsOrigin.split(','),
  credentials: true,
}));

// Logging
app.use(morgan(config.nodeEnv === 'production' ? 'combined' : 'dev'));

// Body parsing
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(config.rateLimitWindowMs),
  max: parseInt(config.rateLimitMaxRequests),
  message: { error: 'Too many requests, please try again later.' },
  standardHeaders: true,
  legacyHeaders: false,
});

app.use('/api/', limiter);

// ============================================
// ROUTES
// ============================================

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API routes
app.use('/api/auth', authRouter);
app.use('/api/feed', feedRouter);
app.use('/api/search', searchRouter);
app.use('/api/videos', videosRouter);
app.use('/api/channels', channelsRouter);
app.use('/api/comments', commentsRouter);
app.use('/api/history', historyRouter);

// ============================================
// ERROR HANDLING
// ============================================

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Global error handler
app.use(errorHandler);

// ============================================
// SERVER
// ============================================

const PORT = config.port || 3000;

httpServer.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Environment: ${config.nodeEnv}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully...');
  httpServer.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT received, shutting down gracefully...');
  httpServer.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

export { app, httpServer };

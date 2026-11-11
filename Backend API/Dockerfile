# Stage 1: Build TypeScript
FROM node:22-slim AS builder

WORKDIR /app

COPY package*.json tsconfig.json ./
RUN npm ci

COPY src ./src
RUN npm run build

# Stage 2: Production Runner
FROM node:22-slim

# Install required system dependencies for headless Chrome execution + unzip
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
      unzip \
      curl \
      ca-certificates \
      chromium \
      xvfb \
      fonts-liberation \
      fonts-noto-color-emoji \
      libasound2 \
      libatk-bridge2.0-0 \
      libatk1.0-0 \
      libc6 \
      libcairo2 \
      libcups2 \
      libdbus-1-3 \
      libexpat1 \
      libfontconfig1 \
      libgbm1 \
      libgcc1 \
      libglib2.0-0 \
      libgtk-3-0 \
      libnspr4 \
      libnss3 \
      libpango-1.0-0 \
      libpangocairo-1.0-0 \
      libstdc++6 \
      libx11-6 \
      libx11-xcb1 \
      libxcb1 \
      libxcomposite1 \
      libxcursor1 \
      libxdamage1 \
      libxext6 \
      libxfixes3 \
      libxi6 \
      libxrandr2 \
      libxrender1 \
      libxss1 \
      libxtst6 \
    && rm -rf /var/lib/apt/lists/*

ENV NODE_ENV=production \
    PORT=5376 \
    HOST=0.0.0.0 \
    PUPPETEER_CACHE_DIR=/home/pptruser/.cache/puppeteer

WORKDIR /app

# Copy package descriptors & install production dependencies only
COPY package*.json ./
RUN npm ci --omit=dev

# Download Google Chrome resmi via Puppeteer (unzip sekarang sudah tersedia)
RUN npx puppeteer browsers install chrome

# Copy built files from builder stage
COPY --from=builder /app/dist ./dist

# Create a non-root user for security & assign permissions to Chrome cache
RUN groupadd -r pptruser && useradd -r -g pptruser -G audio,video -m pptruser \
    && mkdir -p /home/pptruser/Downloads /home/pptruser/.cache \
    && chown -R pptruser:pptruser /home/pptruser \
    && chown -R pptruser:pptruser /app

USER pptruser

EXPOSE 5376

CMD ["node", "dist/server.js"]

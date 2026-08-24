# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first for layer caching
COPY package.json package-lock.json ./
RUN npm ci --production=false

# Copy source and build
COPY . .

# Build argument for API URL (baked into the static bundle)
ARG VITE_API_BASE_URL=https://api.autodeploy.space
ARG VITE_WS_URL=wss://api.autodeploy.space
ARG VITE_GITHUB_AUTH_URL=https://api.autodeploy.space/oauth2/authorization/github

RUN npm run build

# ─── Stage 2: Serve ──────────────────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

LABEL maintainer="AutoDeploy <admin@autodeploy.space>"
LABEL service="frontend"

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/autodeploy.conf

# Copy built static files
COPY --from=build /app/dist /usr/share/nginx/html

# Add health check endpoint
RUN echo "OK" > /usr/share/nginx/html/health

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]

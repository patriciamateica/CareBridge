# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Serve stage
FROM caddy:2-alpine
COPY --from=build /app/dist/frontend/browser /srv
COPY Caddyfile /etc/caddy/Caddyfile

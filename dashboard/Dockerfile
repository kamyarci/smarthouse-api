FROM node:22-slim AS build
WORKDIR /app

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY . .

# O Vite embute as variáveis VITE_ no bundle durante o build
ARG VITE_API_URL
ARG VITE_ADMIN_KEY
ARG VITE_DEVICE_ID=abrigo-01
ARG VITE_POLL_INTERVAL_MS=5000
RUN yarn build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80

# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# Stage 2: Runtime
FROM nginx:alpine
# Adjust path if your Angular output folder is different (usually dist/[project-name])
COPY --from=build /app/dist/gestion-skills/browser /usr/share/nginx/html
COPY --from=build /app/src/assets /usr/share/nginx/html/assets
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

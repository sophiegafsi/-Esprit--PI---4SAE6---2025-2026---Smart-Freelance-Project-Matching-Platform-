FROM nginx:alpine
COPY dist /tmp/dist
RUN find /tmp/dist -name "index.html" -exec dirname {} \; | head -n 1 | xargs -I {} cp -r {}/* /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

FROM nginx:1.27-alpine

# nginx.conf는 Dockerfile 기준으로 같은 폴더 안에 있음
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 정적 파일은 상위 폴더 기준으로 복사
COPY src/main/resources/static /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
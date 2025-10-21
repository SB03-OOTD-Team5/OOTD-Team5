FROM nginx:1.27-alpine

# nginx.conf는 Dockerfile 기준으로 같은 폴더 안에 있음
RUN apk add --no-cache \
      bash \
      curl \
      openssl \
      certbot \
      aws-cli \
  && mkdir -p /var/www/certbot /opt/certbot /etc/letsencrypt \
  && rm -rf /var/cache/apk/*

COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 정적 파일은 상위 폴더 기준으로 복사
COPY src/main/resources/static /usr/share/nginx/html

COPY nginx/options-ssl-nginx.conf /etc/letsencrypt/options-ssl-nginx.conf
COPY nginx/ssl-dhparams.pem /etc/letsencrypt/ssl-dhparams.pem

COPY nginx/entrypoint.sh /opt/certbot/entrypoint.sh
COPY nginx/certbot-renew.sh /opt/certbot/certbot-renew.sh
COPY nginx/post-renew.sh /opt/certbot/post-renew.sh
COPY nginx/cron/certbot /etc/crontabs/root

RUN chmod +x /opt/certbot/entrypoint.sh /opt/certbot/certbot-renew.sh /opt/certbot/post-renew.sh

EXPOSE 80 443

ENTRYPOINT ["/opt/certbot/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

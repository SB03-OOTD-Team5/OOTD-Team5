#!/bin/bash
set -euo pipefail

: "${LETSENCRYPT_DOMAIN:?Missing required environment variable LETSENCRYPT_DOMAIN}"
: "${LETSENCRYPT_S3_BUCKET:?Missing required environment variable LETSENCRYPT_S3_BUCKET}"

WEBROOT=${WEBROOT:-/var/www/certbot}

echo "[renew] Running certbot renew check for ${LETSENCRYPT_DOMAIN}"
certbot renew \
  --webroot \
  --webroot-path "${WEBROOT}" \
  --deploy-hook "/opt/certbot/post-renew.sh" \
  --no-random-sleep-on-renew \
  --quiet
echo "[renew] certbot command completed."

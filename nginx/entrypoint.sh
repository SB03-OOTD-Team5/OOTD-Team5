#!/bin/bash
set -euo pipefail

: "${LETSENCRYPT_DOMAIN:?Missing required environment variable LETSENCRYPT_DOMAIN}"
: "${LETSENCRYPT_EMAIL:?Missing required environment variable LETSENCRYPT_EMAIL}"
: "${LETSENCRYPT_S3_BUCKET:?Missing required environment variable LETSENCRYPT_S3_BUCKET}"

WEBROOT=${WEBROOT:-/var/www/certbot}
LE_ROOT=/etc/letsencrypt
LIVE_DIR="${LE_ROOT}/live/${LETSENCRYPT_DOMAIN}"
S3_URI="s3://${LETSENCRYPT_S3_BUCKET}/cert"
TEMP_CERT_CREATED=0
TEMP_NGINX_STARTED=0

if grep -q "__LETSENCRYPT_DOMAIN__" /etc/nginx/nginx.conf; then
  sed -i "s#__LETSENCRYPT_DOMAIN__#${LETSENCRYPT_DOMAIN}#g" /etc/nginx/nginx.conf
fi

cleanup() {
  if [ "${TEMP_NGINX_STARTED}" -eq 1 ]; then
    nginx -s stop || true
  fi
}
trap cleanup EXIT

mkdir -p "${WEBROOT}" "${LIVE_DIR}"
touch /var/log/cron.log

cat > /opt/certbot/env.sh <<EOF
export LETSENCRYPT_DOMAIN="${LETSENCRYPT_DOMAIN}"
export LETSENCRYPT_EMAIL="${LETSENCRYPT_EMAIL}"
export LETSENCRYPT_S3_BUCKET="${LETSENCRYPT_S3_BUCKET}"
export WEBROOT="${WEBROOT}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-}"
export AWS_SESSION_TOKEN="${AWS_SESSION_TOKEN:-}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-}"
export AWS_REGION="${AWS_REGION:-}"
EOF
chmod 600 /opt/certbot/env.sh

echo "[bootstrap] Checking S3 for existing certificates at ${S3_URI}"
if aws s3 ls "${S3_URI}/" >/dev/null 2>&1; then
  aws s3 sync "${S3_URI}/" "${LE_ROOT}/" --no-progress --exact-timestamps
else
  echo "[bootstrap] No certificate objects found in S3 (continuing with issuance)."
fi

if [ ! -f "${LIVE_DIR}/fullchain.pem" ] || [ ! -f "${LIVE_DIR}/privkey.pem" ]; then
  echo "[bootstrap] Local certificate bundle missing. Creating temporary self-signed certificate."
  openssl req -x509 -nodes -newkey rsa:2048 \
    -days 1 \
    -keyout "${LIVE_DIR}/privkey.pem" \
    -out "${LIVE_DIR}/fullchain.pem" \
    -subj "/CN=${LETSENCRYPT_DOMAIN}"
  cp "${LIVE_DIR}/fullchain.pem" "${LIVE_DIR}/chain.pem"
  TEMP_CERT_CREATED=1
else
  echo "[bootstrap] Local certificate bundle present. Skipping initial issuance."
fi

CERTBOT_EXIT=0
if [ "${TEMP_CERT_CREATED}" -eq 1 ]; then
  echo "[bootstrap] Starting temporary Nginx instance for ACME HTTP-01 challenge."
  nginx
  TEMP_NGINX_STARTED=1
  sleep 2

  echo "[bootstrap] Requesting Let's Encrypt certificate for ${LETSENCRYPT_DOMAIN}."
  set +e
  certbot certonly \
    --webroot \
    --webroot-path "${WEBROOT}" \
    --domain "${LETSENCRYPT_DOMAIN}" \
    --email "${LETSENCRYPT_EMAIL}" \
    --agree-tos \
    --non-interactive \
    --rsa-key-size 4096 \
    --keep-until-expiring \
    --no-eff-email
  CERTBOT_EXIT=$?
  set -e

  if [ "${CERTBOT_EXIT}" -eq 0 ]; then
    echo "[bootstrap] Certificate issued."
  else
    echo "[bootstrap] Warning: certbot failed with exit code ${CERTBOT_EXIT}. Continuing with self-signed certificate."
  fi

  echo "[bootstrap] Stopping temporary Nginx instance."
  nginx -s stop || true
  TEMP_NGINX_STARTED=0
fi

trap - EXIT

if [ -d "${LE_ROOT}" ]; then
  if ! aws s3 sync "${LE_ROOT}/" "${S3_URI}/" --delete --no-progress; then
    echo "[bootstrap] Warning: Failed to synchronize certificates to ${S3_URI}. Continuing without remote backup."
  fi
fi

echo "[service] Launching cron daemon for automated renewals."
crond -l 2

echo "[service] Starting Nginx."
exec "$@"

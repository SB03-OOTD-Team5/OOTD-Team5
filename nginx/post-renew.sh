#!/bin/bash
set -euo pipefail

: "${LETSENCRYPT_DOMAIN:?Missing required environment variable LETSENCRYPT_DOMAIN}"
: "${LETSENCRYPT_S3_BUCKET:?Missing required environment variable LETSENCRYPT_S3_BUCKET}"

LE_ROOT=/etc/letsencrypt
S3_URI="s3://${LETSENCRYPT_S3_BUCKET}/cert"

echo "[renew-hook] Syncing renewed certificates to ${S3_URI}"
aws s3 sync "${LE_ROOT}/" "${S3_URI}/" --delete --no-progress

echo "[renew-hook] Reloading Nginx to pick up renewed certificates."
nginx -s reload
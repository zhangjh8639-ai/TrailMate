#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${TRAILMATE_BASE_URL:-http://127.0.0.1:8080}"
AUTH_BASE="${BASE_URL%/}/api/v1/auth"
PHONE_NUMBER="${TRAILMATE_SMOKE_PHONE:-+8613800138000}"
SMS_CODE="${TRAILMATE_SMOKE_CODE:-123456}"
WECHAT_CODE="${TRAILMATE_SMOKE_WECHAT_CODE:-wx-preview-code}"
WECHAT_STATE="${TRAILMATE_SMOKE_WECHAT_STATE:-local-smoke}"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

post_json() {
  local path="$1"
  local body="$2"
  curl -fsS \
    -X POST "${AUTH_BASE}${path}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

require_command curl
require_command jq

echo "TrailMate auth smoke: ${AUTH_BASE}"

health_body="$(curl -fsS "${BASE_URL%/}/actuator/health")"
echo "$health_body" | jq -e '.status == "UP"' >/dev/null
echo "health: UP"

phone_code_body="$(jq -nc \
  --arg phone "$PHONE_NUMBER" \
  '{phoneNumber: $phone, scene: "LOGIN_OR_REGISTER"}')"
phone_code_response="$(post_json "/phone/code" "$phone_code_body")"
echo "$phone_code_response" | jq -e \
  '.expiresInSeconds == 300 and .retryAfterSeconds == 60' >/dev/null
echo "phone code: requested"

phone_login_body="$(jq -nc \
  --arg phone "$PHONE_NUMBER" \
  --arg code "$SMS_CODE" \
  '{phoneNumber: $phone, smsCode: $code}')"
phone_session="$(post_json "/phone/login" "$phone_login_body")"
phone_user_id="$(echo "$phone_session" | jq -r '.userId')"
phone_refresh_token="$(echo "$phone_session" | jq -r '.refreshToken')"
echo "$phone_session" | jq -e \
  --arg phone "$PHONE_NUMBER" \
  '.provider == "PHONE" and .phoneNumber == $phone and (.accessToken | length > 0) and (.refreshToken | length > 0)' >/dev/null
echo "phone login: ${phone_user_id}"

refresh_body="$(jq -nc --arg token "$phone_refresh_token" '{refreshToken: $token}')"
refresh_session="$(post_json "/refresh" "$refresh_body")"
refresh_token="$(echo "$refresh_session" | jq -r '.refreshToken')"
echo "$refresh_session" | jq -e \
  --arg user "$phone_user_id" \
  '.provider == "PHONE" and .userId == $user and (.refreshToken | length > 0)' >/dev/null
echo "refresh: rotated"

logout_body="$(jq -nc --arg token "$refresh_token" '{refreshToken: $token}')"
logout_status="$(curl -sS -o /dev/null -w "%{http_code}" \
  -X POST "${AUTH_BASE}/logout" \
  -H "Content-Type: application/json" \
  -d "$logout_body")"
if [ "$logout_status" != "204" ]; then
  echo "logout failed with HTTP ${logout_status}" >&2
  exit 1
fi
echo "logout: 204"

wechat_body="$(jq -nc \
  --arg code "$WECHAT_CODE" \
  --arg state "$WECHAT_STATE" \
  '{authCode: $code, state: $state}')"
wechat_session="$(post_json "/wechat/login" "$wechat_body")"
echo "$wechat_session" | jq -e \
  '.provider == "WECHAT" and (.wechatOpenId | length > 0) and (.accessToken | length > 0)' >/dev/null
echo "wechat login: $(echo "$wechat_session" | jq -r '.wechatOpenId')"

echo "TrailMate auth smoke passed"

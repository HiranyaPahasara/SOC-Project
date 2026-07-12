#!/bin/bash
set -euo pipefail

echo "Starting Temperature Converter on :8181 ..."
java ${JAVA_OPTS:-} -jar /app/tempconverter.jar \
  --server.port=8181 \
  --spring.data.mongodb.uri="${SPRING_DATA_MONGODB_URI}" &
TEMP_PID=$!

echo "Starting Currency Converter on :8081 ..."
java ${JAVA_OPTS:-} -jar /app/currencyconverter.jar \
  --server.port=8081 \
  --spring.mongodb.uri="${SPRING_MONGODB_URI}" &
CURR_PID=$!

cleanup() {
  echo "Shutting down..."
  kill "$TEMP_PID" "$CURR_PID" 2>/dev/null || true
  nginx -s quit 2>/dev/null || true
}
trap cleanup SIGTERM SIGINT

echo "Waiting for backends..."
for i in $(seq 1 60); do
  if curl -fsS "http://127.0.0.1:8181/" >/dev/null 2>&1 \
     && curl -fsS "http://127.0.0.1:8081/api/currency/preview?amount=1&from=USD" >/dev/null 2>&1; then
    echo "Backends are ready."
    break
  fi
  if ! kill -0 "$TEMP_PID" 2>/dev/null || ! kill -0 "$CURR_PID" 2>/dev/null; then
    echo "A backend process exited unexpectedly."
    wait || true
    exit 1
  fi
  sleep 2
done

echo "Starting nginx on :80 ..."
nginx -g 'daemon off;' &
NGINX_PID=$!

wait -n "$TEMP_PID" "$CURR_PID" "$NGINX_PID"
EXIT_CODE=$?
cleanup
exit "$EXIT_CODE"

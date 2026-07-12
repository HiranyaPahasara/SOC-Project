# Convert Hub — single multi-service Docker image
# Stages: build both Spring Boot JARs, then run nginx + both backends

# ---------- Build Temperature Converter ----------
FROM maven:3.9-eclipse-temurin-21 AS build-temp
WORKDIR /build
COPY tempconverter/backend/pom.xml .
COPY tempconverter/backend/.mvn ./.mvn
COPY tempconverter/backend/mvnw tempconverter/backend/mvnw.cmd ./
COPY tempconverter/backend/src ./src
RUN mvn -q -DskipTests package

# ---------- Build Currency Converter ----------
FROM maven:3.9-eclipse-temurin-21 AS build-currency
WORKDIR /build
COPY currencyconverter/backend/pom.xml .
COPY currencyconverter/backend/.mvn ./.mvn
COPY currencyconverter/backend/mvnw currencyconverter/backend/mvnw.cmd ./
COPY currencyconverter/backend/src ./src
RUN mvn -q -DskipTests package

# ---------- Runtime: one image, frontend + both APIs ----------
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache nginx curl bash \
    && addgroup -S spring \
    && adduser -S -G spring spring \
    && mkdir -p /run/nginx /var/log/nginx /var/lib/nginx/tmp \
    && chown -R nginx:nginx /var/log/nginx /var/lib/nginx /run/nginx

WORKDIR /app

COPY --from=build-temp /build/target/tempconverter-0.0.1-SNAPSHOT.jar /app/tempconverter.jar
COPY --from=build-currency /build/target/currencyconverter-0.0.1-SNAPSHOT.jar /app/currencyconverter.jar
COPY frontend/ /usr/share/nginx/html/

# nginx config (frontend + reverse proxy to both backends) — generated inline
RUN cat <<'NGINX_CONF' > /etc/nginx/nginx.conf
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /run/nginx/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    sendfile      on;
    keepalive_timeout 65;
    client_max_body_size 10m;

    server {
        listen 80;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

        location /api/temperatures/ {
            proxy_pass         http://127.0.0.1:8181;
            proxy_http_version 1.1;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
            proxy_set_header   X-API-KEY $http_x_api_key;
        }

        location /api/currency/ {
            proxy_pass         http://127.0.0.1:8081;
            proxy_http_version 1.1;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
        }

        location / {
            try_files $uri $uri/ /index.html;
        }
    }
}
NGINX_CONF

# Startup script: launch both backends, then nginx — generated inline
RUN cat <<'ENTRYPOINT_SH' > /app/entrypoint.sh
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
  --spring.data.mongodb.uri="${SPRING_MONGODB_URI}" &
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
ENTRYPOINT_SH

RUN chmod +x /app/entrypoint.sh \
    && chown -R spring:spring /app

ENV SPRING_DATA_MONGODB_URI=mongodb://temp-mongo:27017/tempconverter \
    SPRING_MONGODB_URI=mongodb://currency-mongo:27017/currency_db \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0"

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1/ || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]

# Convert Hub — single multi-service Docker image
# Stages: build both Spring Boot JARs, then run nginx + both backends

# ---------- Build Temperature Converter ----------
FROM maven:3.9-eclipse-temurin-21 AS build-temp
WORKDIR /build
COPY tempconverter/pom.xml .
COPY tempconverter/.mvn ./.mvn
COPY tempconverter/mvnw tempconverter/mvnw.cmd ./
COPY tempconverter/src ./src
RUN mvn -q -DskipTests package

# ---------- Build Currency Converter ----------
FROM maven:3.9-eclipse-temurin-21 AS build-currency
WORKDIR /build
COPY currencyconverter/pom.xml .
COPY currencyconverter/.mvn ./.mvn
COPY currencyconverter/mvnw currencyconverter/mvnw.cmd ./
COPY currencyconverter/src ./src
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
COPY tempconverter/frontend/ /usr/share/nginx/html/
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh \
    && chown -R spring:spring /app

ENV SPRING_DATA_MONGODB_URI=mongodb://temp-mongo:27017/tempconverter \
    SPRING_MONGODB_URI=mongodb://currency-mongo:27017/currency_db \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0"

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1/ || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]

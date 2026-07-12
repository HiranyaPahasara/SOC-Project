FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system spring && useradd --system --gid spring spring \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/currencyconverter-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f "http://localhost:8081/api/currency/preview?amount=1&from=USD" || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

ARG TRAILMATE_JDK_IMAGE=eclipse-temurin:17-jdk
ARG TRAILMATE_JRE_IMAGE=eclipse-temurin:17-jre

FROM ${TRAILMATE_JDK_IMAGE} AS build

WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x ./gradlew && ./gradlew :trailmate-server:bootJar --no-daemon

FROM ${TRAILMATE_JRE_IMAGE}

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /usr/sbin/nologin trailmate
WORKDIR /app
COPY --from=build /workspace/trailmate-server/build/libs/trailmate-server-*.jar /app/trailmate-server.jar
RUN chown -R trailmate:trailmate /app

USER trailmate
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=5 --start-period=40s \
    CMD curl -fsS http://localhost:8080/actuator/health | grep -q 'UP'

ENTRYPOINT ["java", "-jar", "/app/trailmate-server.jar"]

# ==========================
# Stage 1 — Build JAR
# ==========================
FROM bellsoft/liberica-openjdk-debian:24 as builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests


# ==========================
# Stage 2 — Runtime + Playwright
# ==========================
FROM bellsoft/liberica-openjre-debian:24

WORKDIR /app

# Accept TZ from build or docker-compose
ARG TZ
ENV TZ=${TZ}

# Install Chromium + tzdata + Playwright deps + missing libraries
RUN apt-get update && apt-get install -y \
    chromium \
    tzdata \
    libnss3 \
    fonts-liberation \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    wget \
    ca-certificates \
    libgstreamer1.0-0 \
    libgtk-4-1 \
    libgraphene-1.0-0 \
    libvpx7 \
    libevent-2.1-7 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-gl \
    flite1-dev \
    libwebpdemux2 \
    libavif15 \
    libharfbuzz-icu0 \
    libwebpmux3 \
    libenchant-2-2 \
    libsecret-1-0 \
    libhyphen0 \
    libmanette-0.2-0 \
    libgles2 \
    libx264-164 \
    fonts-wqy-zenhei \
    libwoff1 \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

ENV PLAYWRIGHT_BROWSERS_PATH=/usr/bin
ENV CHROMIUM_PATH=/usr/bin/chromium

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
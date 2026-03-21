# Multi-stage: build with JDK, run with JRE. Layered JAR for faster starts and better layer cache.
# Build: docker build -t user:<tag> .
# Run:   docker run --rm -p 8082:8082 -e SPRING_DATASOURCE_URL=... user:<tag>

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

WORKDIR /app/layers
RUN java -Djarmode=tools -jar /app/build/libs/*.jar extract --layers --destination .

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S -g 1000 spring && adduser -S -u 1000 -G spring spring

WORKDIR /app
COPY --from=builder --chown=spring:spring /app/layers/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/layers/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/layers/application/ ./

USER spring:spring

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

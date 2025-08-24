# build stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar --no-daemon

# run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/app.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

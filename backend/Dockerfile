FROM gradle:9-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test --no-daemon && \
    find /app/build/libs -name "*.jar" ! -name "*plain*" -exec cp {} /app/build/app.jar \;

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

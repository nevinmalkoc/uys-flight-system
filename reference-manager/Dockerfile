# 1. Aşama: Maven ve Java 21 ile projeyi derleme (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true
# 2. Aşama: Sadece JAR dosyasını çalıştıracak hafif ortam (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

# docker a ekleme mikroservisleri docker üzerinden kontrol etmek için
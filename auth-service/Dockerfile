FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="prod"
WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar /app/app.jar"]

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/auth-service/target/*.jar /app/app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

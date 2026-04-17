FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn -pl eureka-server -am clean package -DskipTests

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/eureka-server/target/*.jar /app/app.jar

EXPOSE 8761
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

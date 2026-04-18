FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl eureka-server -am

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8761
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
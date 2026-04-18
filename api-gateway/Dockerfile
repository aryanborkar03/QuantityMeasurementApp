FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl api-gateway -am -Dspring-boot.repackage.skip=false

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/api-gateway/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
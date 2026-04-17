FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG MODULE
WORKDIR /workspace

COPY . .
RUN mvn -pl ${MODULE} -am clean package -DskipTests

FROM eclipse-temurin:17-jre

ARG MODULE
ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

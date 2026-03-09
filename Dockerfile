FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN apk add --no-cache netcat-openbsd curl

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD echo "URI: $SPRING_DATA_MONGODB_URI" && java -jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
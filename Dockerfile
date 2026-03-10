# -------- Build stage --------
# imagem base da amazon para java 21, com recursos basicos nessarios para o build
FROM maven:3.9.9-amazoncorretto-21-alpine AS build

WORKDIR /build

# cache dependencies, acelera o build e gasta menos recursos
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

# build da apliação, pulando etapa de testes para ter um stage limpo
COPY src ./src
RUN mvn -q -DskipTests package

# -------- Runtime stage --------
# imagem base da amazon para java 21, mas sem recursos para o build, apenas para execução
FROM amazoncorretto:21-alpine-jre

WORKDIR /app

# cria e define um usuario não root na aplicação, evite problemas/alertas de segurança
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /build/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
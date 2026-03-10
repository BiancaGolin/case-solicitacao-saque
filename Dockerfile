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

# ================= Test Stage =================
FROM maven:3.9.9-amazoncorretto-21-alpine AS test

WORKDIR /test

# Copia o pom e instala dependências (cache)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Comando padrão do stage de teste: executa todos os testes
CMD ["mvn", "clean", "test"]

# ================= Runtime Stage =================
# imagem base da amazon para java 21, mas sem recursos para o build, apenas para execução
FROM amazoncorretto:21-alpine

WORKDIR /app

# cria e define um usuario não root na aplicação, evite problemas/alertas de segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Copia o JAR do stage de build
COPY --from=build /build/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

# Entrada padrão para rodar a aplicação
ENTRYPOINT ["java","-jar","/app/app.jar"]
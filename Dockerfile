# syntax=docker/dockerfile:1

# =============================================================================
# Build
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# pom + contrato OpenAPI numa camada isolada do código-fonte: enquanto eles não
# mudarem, esta camada é reaproveitada do cache e o download das dependências
# não roda de novo. O contrato entra aqui porque o openapi-generator lê ele
# durante o build.
COPY pom.xml .
COPY openapi ./openapi
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q dependency:go-offline -DskipTests

COPY src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q clean package -DskipTests

# Explode o fat jar (~65 MB) nas camadas do Spring Boot. Sem isso o jar inteiro
# é UMA camada Docker: qualquer alteração de código reescreve os 65 MB e o
# registry/VPS precisa transferir tudo de novo. Depois do split:
#   dependencies/        ~62 MB   muda só quando o pom muda
#   spring-boot-loader/  ~676 KB  praticamente nunca muda
#   snapshot-dependencies/ vazio
#   application/         ~496 KB  <- a única que muda a cada commit
RUN java -Djarmode=tools -jar target/*.jar \
    extract --layers --launcher --destination extracted

# =============================================================================
# Runtime
# =============================================================================
# JRE Alpine em vez do Debian: base ~3x menor, e é a camada que a VPS baixa
# no primeiro pull.
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app
USER app

# A ordem dos COPY é o que faz o cache funcionar: da camada mais estável para a
# mais volátil. Só a última é reenviada num deploy de código.
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./

EXPOSE 8080

# MaxRAMPercentage: a JVM enxerga o limite do container, não a RAM da VPS.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", \
            "org.springframework.boot.loader.launch.JarLauncher"]

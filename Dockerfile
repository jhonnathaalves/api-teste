FROM openjdk:17-jdk-slim-buster

WORKDIR /app

# Instala pacotes necessários de forma segura e enxuta
RUN apt-get update && \
    apt-get install --no-install-recommends -y tzdata && \
    rm -rf /var/lib/apt/lists/*

COPY target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Roda como usuário não-root (criado logo abaixo)
RUN useradd -m spring
USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
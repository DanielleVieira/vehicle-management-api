# =========================
# Etapa de build
# =========================
FROM gradle:jdk25-corretto AS build

WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle ./gradle

RUN gradle dependencies --no-daemon

COPY . .

RUN gradle bootJar --no-daemon

# =========================
# Etapa de execução
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


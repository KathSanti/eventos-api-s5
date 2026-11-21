# ---------- Etapa 1: compilar el .jar ----------
# --platform=$BUILDPLATFORM: compila en la CPU nativa del runner (AMD64); el .jar sirve en cualquier CPU,
# así el build ARM64 no emula Maven (mucho más rápido).
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /code
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# ---------- Etapa 2: imagen final, solo JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build --chown=app:app /code/target/ms-resumenes-*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

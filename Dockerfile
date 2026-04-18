# =============================================
# Stage 1: Build con Maven
# =============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven primero (cache de dependencias)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias (aprovecha capa de cache Docker)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src src
RUN ./mvnw package -DskipTests -B

# =============================================
# Stage 2: Runtime con JRE ligero
# =============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR compilado del stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto (Railway inyecta PORT como variable de entorno)
EXPOSE 8080

# Ejecutar con perfil de producción
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

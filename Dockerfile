# Lucas: etapa de compilação separada da imagem final para manter o contêiner menor.
FROM maven:3.9-eclipse-temurin-25 AS compilacao
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
ENV PORT=8080
COPY --from=compilacao /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-jar", "/app/app.jar"]
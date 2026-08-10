FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
LABEL org.opencontainers.image.source="https://github.com/kubeshift/kubeshift"
LABEL org.opencontainers.image.description="KubeShift Core Service"
LABEL org.opencontainers.image.version="1.0.0"

RUN addgroup -S kubeshift && adduser -S kubeshift -G kubeshift
USER kubeshift

COPY --from=builder /app/target/kubeshift-core-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

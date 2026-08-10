FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY kubeshift-core/pom.xml kubeshift-core/
COPY kubeshift-core/src kubeshift-core/src/
RUN mvn -pl kubeshift-core -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
LABEL org.opencontainers.image.source="https://github.com/kubeshift/kubeshift"
LABEL org.opencontainers.image.description="KubeShift Core Service"
LABEL org.opencontainers.image.version="1.0.0"

RUN addgroup -S kubeshift && adduser -S kubeshift -G kubeshift
USER kubeshift

COPY --from=builder /app/kubeshift-core/target/kubeshift-core-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

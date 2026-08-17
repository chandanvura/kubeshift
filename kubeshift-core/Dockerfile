FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY kubeshift-core/pom.xml kubeshift-core/
COPY kubeshift-remediate/pom.xml kubeshift-remediate/
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

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

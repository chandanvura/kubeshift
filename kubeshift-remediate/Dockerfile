FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app
# Copy the parent pom and module pom
COPY pom.xml .
COPY kubeshift-core/pom.xml kubeshift-core/
COPY kubeshift-remediate/pom.xml kubeshift-remediate/
COPY kubeshift-remediate/src kubeshift-remediate/src/

RUN mvn -pl kubeshift-remediate -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S kubeshift && adduser -S kubeshift -G kubeshift
USER kubeshift:kubeshift

WORKDIR /app
COPY --from=builder /app/kubeshift-remediate/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

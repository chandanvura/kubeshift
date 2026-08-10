FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app
# Copy the parent pom and module pom
COPY pom.xml ./kubeshift-remediate/pom.xml
# If building in context of parent, we might just copy all. Assuming standard module build.
COPY src ./kubeshift-remediate/src

WORKDIR /app/kubeshift-remediate
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S kubeshift && adduser -S kubeshift -G kubeshift
USER kubeshift:kubeshift

WORKDIR /app
COPY --from=builder /app/kubeshift-remediate/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

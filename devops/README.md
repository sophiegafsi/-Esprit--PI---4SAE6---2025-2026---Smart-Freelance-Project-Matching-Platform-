# DevOps Resources

This folder versions the Jenkins and monitoring resources used with the `Fares-portfolio` branch.

Active service pipeline in this branch:

- `devops/jenkins/Jenkinsfile.portfolio-service`

The pipeline runs:

- `Test`: `./mvnw -B clean test`
- `Package`: `./mvnw -B package -DskipTests`
- `SonarQube`: optional, controlled by `RUN_SONAR`
- `post`: JUnit publication and `*-SNAPSHOT.jar` artifact archiving

Monitoring resources included here:

- Prometheus scrape config
- Blackbox exporter probe config
- Grafana datasource provisioning
- Grafana dashboard provisioning
- MySQL init script used by the integrated demo stack

Note:

- The Prometheus targets in this folder point to the integrated microservice stack (`eureka`, `gateway`, `skills-service`, `portfolio-service`).
- For Jenkins, you can use either the root `Jenkinsfile` or `devops/jenkins/Jenkinsfile.portfolio-service`.

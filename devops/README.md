# DevOps Backend - Eureka and Gateway

This branch contains the Jenkins, SonarQube, JaCoCo, Prometheus and Grafana setup for the Eureka Server and API Gateway.

## Jenkins

Available pipeline files:

- `Jenkinsfile.eureka`
- `Jenkinsfile.gateway`
- `devops/jenkins/Jenkinsfile.eureka`
- `devops/jenkins/Jenkinsfile.gateway`

Each pipeline runs:

- `Test and Package`
- `JaCoCo Coverage`
- `SonarQube`

The SonarQube stage runs automatically with `Build Now`; no `RUN_SONAR` parameter is required.

Required Jenkins plugins:

- Pipeline
- Git
- JUnit
- JaCoCo
- SonarQube Scanner for Jenkins
- Credentials Binding

Required Jenkins credential:

```text
Kind: Secret text
ID: sonarqube-token
Secret: your SonarQube token
```

## SonarQube

Run SonarQube separately on:

```text
http://localhost:9000
```

If Jenkins runs inside Docker, replace `http://localhost:9000` in the Jenkinsfiles with:

```text
http://host.docker.internal:9000
```

## Prometheus and Grafana

Start Eureka and Gateway locally first:

```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

```powershell
cd api-gateway
.\mvnw.cmd spring-boot:run
```

Then start monitoring:

```bash
cd devops
docker compose -f docker-compose.monitoring.yml up -d
```

Open:

```text
Prometheus: http://localhost:9090
Grafana: http://localhost:3000
Grafana login: admin / admin
```

Prometheus scrapes:

```text
http://host.docker.internal:8761/actuator/prometheus
http://host.docker.internal:8088/actuator/prometheus
```

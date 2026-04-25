# Monitoring Prometheus Grafana

Ce dossier lance le monitoring du projet Smart Freelance.

## Services surveilles

Prometheus collecte les metriques Spring Boot via `/actuator/prometheus`:

- Eureka Server: `http://host.docker.internal:8761/actuator/prometheus`
- API Gateway: `http://host.docker.internal:8088/actuator/prometheus`
- Evaluation Service: `http://host.docker.internal:8085/actuator/prometheus`
- Recompense Service: `http://host.docker.internal:8094/actuator/prometheus`

Le blackbox exporter verifie aussi la disponibilite HTTP de:

- Frontend Angular: `http://host.docker.internal:4200`
- Jenkins: `http://host.docker.internal:8081/login`
- SonarQube: `http://host.docker.internal:9000/api/system/status`

## Lancement

Demarrer d'abord les applications a surveiller, puis:

```powershell
cd devops
docker compose -f docker-compose.monitoring.yml up -d
```

## Acces

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Grafana login: `admin` / `admin`

Dans Grafana, le datasource Prometheus et le dashboard `Smart Freelance Monitoring` sont provisionnes automatiquement.

## Verification rapide

Dans Prometheus, ouvrir:

```text
Status > Targets
```

Les targets doivent etre `UP` pour les services lances.

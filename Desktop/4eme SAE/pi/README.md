# Spring Boot Microservices Project - PiDev

This project is a microservices-based application built with Spring Boot, using Keycloak for authentication and Mailhog for email testing.

## Prerequisites

- Docker and Docker Compose installed on your machine.
- Git (to clone the repository).

## Getting Started

### 1. Clone the Project
Make sure you are on the `youssef` branch:
```bash
git checkout youssef
```

### 2. Start the Infrastructure (Keycloak & Mailhog)
Run Docker Compose to start Keycloak and Mailhog:
```bash
docker-compose up -d
```
The Keycloak instance is pre-configured to import the realm file from `./keycloak/freelink-realm.json`.

- **Keycloak Console**: [http://localhost:8080](http://localhost:8080) (Admin: `admin`/`admin`)
- **Mailhog UI**: [http://localhost:8025](http://localhost:8025)

### 3. Run Microservices
Each microservice is located in its own directory:
- `API-Gateway`
- `condature`
- `eureka-server`
- `user`

You can run them using Maven:
```bash
./mvnw spring-boot:run
```

## Team Collaboration
When pulling changes from this branch, ensure you run `docker-compose up -d` to get the latest Keycloak configuration.

---
**Branch:** `youssef`

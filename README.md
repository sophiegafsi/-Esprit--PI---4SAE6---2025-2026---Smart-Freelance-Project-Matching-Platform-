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

## Troubleshooting

### Keycloak '400' Error on Registration
If you encounter a `400 Bad Request` or `Failed to create user` error:
1.  **Check the Console**: The updated `user-service` now logs the exact error body from Keycloak. Look for "Body: { ... }".
2.  **Run the Debug Script**: Run the included PowerShell script to verify your Keycloak setup:
    ```powershell
    ./debug_keycloak.ps1
    ```
3.  **Clean Restart**: If the realm import seems broken, try:
    ```bash
    docker-compose down -v
    docker-compose up -d
    ```
    This deletes the local Keycloak data and forces a fresh import of `freelink-realm.json`.

# run_app.ps1
# Runs the Frontend, Gateway, User Service, and Eureka Server.

Write-Host "Checking Keycloak status..."
# Check if keycloak-yesterday container is running
$keycloak = docker ps --filter "name=keycloak-yesterday" --format "{{.ID}}"
if (-not $keycloak) {
    Write-Warning "Keycloak container (keycloak-yesterday) does not seem to be running. You might need to run 'docker-compose up -d' first."
} else {
    Write-Host "Keycloak is running."
}

Write-Host "Starting Eureka Server..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'eureka-server\eureka-server'; .\mvnw spring-boot:run"

Write-Host "Starting API Gateway..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'API-Gateway\API-Gateway'; .\mvnw spring-boot:run"

Write-Host "Starting User Service..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'user'; .\mvnw spring-boot:run"

Write-Host "Starting Frontend..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'front\sofia-pidev-front-Gestion-skills'; npm install; ng serve"

Write-Host "All requested services initiated."

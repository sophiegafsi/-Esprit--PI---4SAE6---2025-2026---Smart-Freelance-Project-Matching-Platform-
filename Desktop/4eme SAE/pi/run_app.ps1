# run_app.ps1
# Runs the Frontend, Gateway, User Service, and Eureka Server.

Write-Host "Checking Keycloak status..."
# Check if keycloak container is running
$keycloak = docker ps --filter "name=keycloak" --format "{{.ID}}"
if (-not $keycloak) {
    Write-Warning "Keycloak container (keycloak-yesterday) does not seem to be running. You might need to run 'docker-compose up -d' first."
}
else {
    Write-Host "Keycloak is running."
}

Write-Host "Starting Eureka Server..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'eureka-server\eureka-server'; .\mvnw clean install -DskipTests; .\mvnw spring-boot:run"

Write-Host "Starting API Gateway..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'API-Gateway\API-Gateway'; .\mvnw clean install -DskipTests; .\mvnw spring-boot:run"

Write-Host "Starting User Service..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'user'; .\mvnw clean install -DskipTests; .\mvnw spring-boot:run"

Write-Host "Starting Condidature Service..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'condature'; .\mvnw clean install -DskipTests; .\mvnw spring-boot:run"

Write-Host "Starting Frontend..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'front\sofia-pidev-front-Gestion-skills'; npm install; ng serve"

Write-Host "All requested services initiated."

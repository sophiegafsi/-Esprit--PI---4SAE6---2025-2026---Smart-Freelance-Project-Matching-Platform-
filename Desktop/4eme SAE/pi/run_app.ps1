# run_app.ps1
# Runs the Frontend, Gateway, User Service, and Eureka Server.
# Using 'java -jar' with explicit JAR resolution for better stability.

Write-Host "Checking Keycloak status..."
$keycloak = docker ps --filter "name=keycloak" --format "{{.ID}}"
if (-not $keycloak) {
    Write-Warning "Keycloak container does not seem to be running. Run 'docker-compose up -d' first."
} else {
    Write-Host "Keycloak is running."
}

function Start-JavaService {
    param([string]$name, [string]$path)
    Write-Host "Starting $name..."
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$path'; Write-Host '--- Build Start: $name ---'; .\mvnw clean install -DskipTests; if (`$?) { `$jar = Get-ChildItem target/*.jar | Where-Object { `$_.Name -notmatch '.original' } | Select-Object -First 1; if (`$jar) { Write-Host 'Running: ' `$jar.Name; java -jar `$jar.FullName } else { Write-Error 'No executable JAR found in target folder!' } } else { Write-Error 'Maven Build Failed!' }; Read-Host 'Service stopped. Press Enter to close window...'"
}

Start-JavaService "Eureka Server" "eureka-server\eureka-server"
Start-JavaService "API Gateway" "API-Gateway\API-Gateway"
Start-JavaService "User Service" "user"
Start-JavaService "Condidature Service" "condature"
Start-JavaService "Projet Service" "porjectservice"
Start-JavaService "Time Tracking Service" "time-tracking-service"
Start-JavaService "Reclamation Service" "reclamation"
Start-JavaService "Skills Service" "skills-service"
Start-JavaService "Portfolio Service" "portfolio-service"
Start-JavaService "Planning Service" "gestion-planing"
Start-JavaService "Reservation Service" "reservation-service"
Write-Host "Starting Frontend..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'front\sofia-pidev-front-Gestion-skills'; npm install; ng serve; Read-Host 'Frontend stopped. Press Enter to close...'"

Write-Host "All requested services initiated."

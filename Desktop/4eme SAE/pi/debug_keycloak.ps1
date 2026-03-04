$SERVER_URL = "http://localhost:8080"
$REALM = "freelink-realm"

Write-Host "--- Keycloak Troubleshooting Script ---" -ForegroundColor Cyan

# 1. Check if Keycloak is up
Write-Host "1. Checking Keycloak connectivity..."
try {
    $response = Invoke-WebRequest -Uri "$SERVER_URL/health" -Method Get -ErrorAction Stop
    Write-Host "   SUCCESS: Keycloak is up." -ForegroundColor Green
}
catch {
    Write-Host "   ERROR: Cannot connect to Keycloak at $SERVER_URL. Is it running?" -ForegroundColor Red
    exit
}

# 2. Check if realm exists
Write-Host "2. Checking for realm '$REALM'..."
try {
    $realmUrl = "$SERVER_URL/realms/$REALM"
    $response = Invoke-WebRequest -Uri $realmUrl -Method Get -ErrorAction Stop
    Write-Host "   SUCCESS: Realm '$REALM' found." -ForegroundColor Green
}
catch {
    Write-Host "   ERROR: Realm '$REALM' NOT found at $realmUrl." -ForegroundColor Red
    Write-Host "   TIP: Try 'docker-compose down -v' and then 'docker-compose up -d' to re-import the realm." -ForegroundColor Yellow
}

# 3. Check for specific clients
Write-Host "3. Note: If the realm exists but registration fails with 400, it might be due to:"
Write-Host "   - Missing 'client' role in the realm."
Write-Host "   - Invalid email format or password policy violation."
Write-Host "   - Concurrent requests with the same email."

Write-Host "`nPlease pull the latest changes, restart your app, and check the console for the 'Body: {...}' error message from Keycloak." -ForegroundColor Cyan

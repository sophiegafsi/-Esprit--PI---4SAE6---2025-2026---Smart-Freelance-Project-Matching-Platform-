Write-Host "=== Create a New Project Step-by-Step ===" -ForegroundColor Yellow

$email = Read-Host "1. Enter Client Email"
$title = Read-Host "2. Enter Project Title"
$description = Read-Host "3. Enter Project Description"
$budgetInput = Read-Host "4. Enter Project Budget"

# Validate budget input
if ($budgetInput -as [double]) {
    $budget = [double]$budgetInput
}
else {
    Write-Host "ERROR: Invalid budget amount. Please enter a number." -ForegroundColor Red
    exit
}

Write-Host "`n>>> Resolving user: $email ..." -ForegroundColor Cyan

try {
    # 1. Resolve User with client role
    $userResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8082/api/users/set-role?email=$($email)&role=client"
    $clientId = $userResponse.id

    if (-not $clientId) {
        Write-Error "Could not find user or retrieve ID for email: $email"
        exit
    }

    Write-Host ">>> Client resolved with ID: $clientId" -ForegroundColor Green
    Write-Host ">>> Creating project..." -ForegroundColor Cyan

    # 2. Create Project
    $projectBody = @{
        title       = $title
        description = $description
        clientId    = $clientId
        budget      = $budget
    } | ConvertTo-Json

    $projectResponse = Invoke-RestMethod -Method Post -Uri "http://localhost:8083/api/projects" -ContentType "application/json" -Body $projectBody
    
    Write-Host "`nSUCCESS: Project created successfully!" -ForegroundColor Green
    $projectResponse | ConvertTo-Json
}
catch {
    Write-Host "`nERROR: Request failed." -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)"
}

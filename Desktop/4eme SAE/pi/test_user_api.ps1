$token = "YOUR_JWT_TOKEN_HERE"

Write-Host "Testing /api/users/me endpoint..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/users/me" `
        -Method GET `
        -Headers @{
            "Authorization" = "Bearer $token"
        }
    
    Write-Host "`nUser Data:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
    
    Write-Host "`nRole: $($response.role)" -ForegroundColor Yellow
    
    if ($response.role -eq "admin") {
        Write-Host "✓ User has admin role!" -ForegroundColor Green
    } else {
        Write-Host "✗ User does NOT have admin role (current: $($response.role))" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

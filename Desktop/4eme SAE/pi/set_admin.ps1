param([string]$email)

if (-not $email) {
    Write-Host "Please provide an email address."
    Write-Host "Usage: .\set_admin.ps1 <email>"
    exit
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/users/set-role?email=$email&role=admin" -Method Post
    Write-Host "Success! User '$email' is now an admin."
    Write-Host "Response: "
    $response | Format-List
} catch {
    Write-Host "Error setting role. Ensure the user service is running and the email exists."
    Write-Host $_.Exception.Message
}

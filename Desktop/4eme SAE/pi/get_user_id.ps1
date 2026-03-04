$email = "dali@exemple.com"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/users/set-role?email=$email&role=client" -Method Post
    Write-Host "User ID for $email is: $($response.id)"
}
catch {
    Write-Host "Error: $($_.Exception.Message)"
}

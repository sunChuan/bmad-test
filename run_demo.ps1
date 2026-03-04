Write-Host "Killing existing Java/Node processes..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
$nodeProcs = Get-Process -Name node -ErrorAction SilentlyContinue | Where-Object { $_.Path -notmatch "playwright" }
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force -ErrorAction SilentlyContinue
}

Write-Host "Starting Spring Boot Backend..."
Start-Process -FilePath "mvn.cmd" -ArgumentList "spring-boot:run" -WorkingDirectory "d:\WorkSpace\ai\Antigravity\bmad-test\backend"

Write-Host "Waiting 5 seconds for Backend to Compile and Start..."
Start-Sleep -Seconds 5

Write-Host "Starting Vite Frontend..."
Start-Process -FilePath "pnpm.cmd" -ArgumentList "dev" -WorkingDirectory "d:\WorkSpace\ai\Antigravity\bmad-test\frontend"

Write-Host "Services started! Frontend should be available at http://localhost:3000"

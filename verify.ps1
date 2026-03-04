Write-Host "Killing existing Java/Node processes..."
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
# 只 kill 非 playwright 相关的 Node 进程以保真
$nodeProcs = Get-Process -Name node -ErrorAction SilentlyContinue | Where-Object { $_.Path -notmatch "playwright" }
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force -ErrorAction SilentlyContinue
}

Write-Host "Starting Spring Boot Backend..."
$backendJob = Start-Job -ScriptBlock {
    cd d:\WorkSpace\ai\Antigravity\bmad-test\backend
    mvn spring-boot:run
}

Write-Host "Waiting 20 seconds for Backend to Compile and Start..."
Start-Sleep -Seconds 20

Write-Host "Starting Vite Frontend..."
$frontendJob = Start-Job -ScriptBlock {
    cd d:\WorkSpace\ai\Antigravity\bmad-test\frontend
    pnpm dev
}

Write-Host "Waiting 5 seconds for Frontend..."
Start-Sleep -Seconds 5

Write-Host "Running Playwright E2E Test for RefCard..."
cd d:\WorkSpace\ai\Antigravity\bmad-test\frontend
node test-refcard.mjs
$exitCode = $LASTEXITCODE

Write-Host "Stopping background jobs..."
$backendJob | Stop-Job
$frontendJob | Stop-Job

Receive-Job $backendJob | Write-Verbose
Receive-Job $frontendJob | Write-Verbose

exit $exitCode

$ErrorActionPreference = "Stop"

$jarPath = Join-Path (Resolve-Path ".") "target\legend-game-0.1.0-SNAPSHOT.jar"

Write-Host "[package] jar: $jarPath"
if (Test-Path $jarPath) {
    $item = Get-Item $jarPath
    Write-Host "[package] jar exists, size=$($item.Length), updated=$($item.LastWriteTime)"
} else {
    Write-Host "[package] jar does not exist yet"
    Write-Host "[package] jar write lock check: OK"
    exit 0
}

Write-Host "[package] java processes:"
$javaProcesses = Get-Process | Where-Object { $_.ProcessName -like "*java*" }
if (-not $javaProcesses) {
    Write-Host "[package] no java process found"
} else {
    $javaProcesses | Select-Object Id, ProcessName, MainWindowTitle, StartTime | Format-Table -AutoSize
}

try {
    $stream = [System.IO.File]::Open($jarPath, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
    $stream.Close()
    Write-Host "[package] jar write lock check: OK"
} catch {
    Write-Host "[package] jar write lock check: LOCKED"
    Write-Host "[package] close the local Spring Boot run, IDE run configuration, or java process that is using this jar, then run:"
    Write-Host "[package] mvn package -DskipTests"
    exit 2
}

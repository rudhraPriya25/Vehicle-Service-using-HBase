# ===================================================================
#   Vehicle Service Record Management using Apache HBase
#   PowerShell Startup & Run Script (Full Automation)
# ===================================================================

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
$env:HADOOP_HOME = "C:\hadoop"
$env:HBASE_HOME = "C:\hbase-2.6.7-hadoop3"
$env:PATH = "$env:JAVA_HOME\bin;$env:HADOOP_HOME\bin;$env:HBASE_HOME\bin;$env:PATH"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  Vehicle Service Record Management using Apache HBase           " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan

# Step 1: Check if HBase is running; if not, launch it and wait until it is ready.
Write-Host "[1/3] Checking Apache HBase Service..." -ForegroundColor Yellow
$hbaseRunning = $false
foreach ($port in 2181, 16000) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $async = $tcp.BeginConnect("127.0.0.1", $port, $null, $null)
        $success = $async.AsyncWaitHandle.WaitOne(1500)
        if ($success -and $tcp.Connected) {
            $hbaseRunning = $true
            $tcp.EndConnect($async)
        }
        $tcp.Close()
    } catch {
        $hbaseRunning = $false
    }
    if ($hbaseRunning) { break }
}

if (-not $hbaseRunning) {
    Write-Host "   -> HBase is not running. Starting HBase Master in background..." -ForegroundColor Yellow
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c title HBase Master && hbase.cmd master start" -WindowStyle Normal

    Write-Host "   -> Waiting for HBase to initialize on ports 2181 and 16000..." -NoNewline -ForegroundColor Cyan
    $retries = 60
    while ($retries -gt 0 -and -not $hbaseRunning) {
        Start-Sleep -Seconds 2
        Write-Host "." -NoNewline -ForegroundColor Cyan
        foreach ($port in 2181, 16000) {
            try {
                $tcp = New-Object System.Net.Sockets.TcpClient
                $async = $tcp.BeginConnect("127.0.0.1", $port, $null, $null)
                $success = $async.AsyncWaitHandle.WaitOne(1000)
                if ($success -and $tcp.Connected) {
                    $hbaseRunning = $true
                    $tcp.EndConnect($async)
                }
                $tcp.Close()
            } catch {
                $hbaseRunning = $false
            }
            if ($hbaseRunning) { break }
        }
        $retries--
    }
    Write-Host ""
    if ($hbaseRunning) {
        Write-Host "   -> HBase Cluster is ready and listening on the required ports!" -ForegroundColor Green
        Start-Sleep -Seconds 4
    } else {
        Write-Host "   -> Warning: HBase did not report ready within the expected time. Continuing with launch..." -ForegroundColor Red
    }
} else {
    Write-Host "   -> HBase is already active on the required ports!" -ForegroundColor Green
}

# Step 2: Compile Java Source Files
Write-Host "[2/3] Compiling Java Backend..." -ForegroundColor Yellow
if (!(Test-Path "target\classes")) { New-Item -ItemType Directory -Path "target\classes" -Force | Out-Null }

$cp = "C:\hbase-2.6.7-hadoop3\lib\shaded-clients\hbase-shaded-client-2.6.7-hadoop3.jar;C:\hbase-2.6.7-hadoop3\lib\client-facing-thirdparty\*"
$sources = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

& "$env:JAVA_HOME\bin\javac.exe" -cp $cp -d "target\classes" $sources
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "   -> Java Compilation Successful!" -ForegroundColor Green

# Step 3: Run Main Application
Write-Host "[3/3] Starting Web Server on http://localhost:8080 ..." -ForegroundColor Yellow
$opens = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.net.dns=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED"
$runCp = "target\classes;$cp"

& "$env:JAVA_HOME\bin\java.exe" $opens.Split(" ") -cp $runCp "-Dhadoop.home.dir=C:\hadoop" com.vehicleservice.Main 8080

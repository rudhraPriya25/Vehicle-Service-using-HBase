@echo off
setlocal
echo ===================================================================
echo   Vehicle Service Record Management using Apache HBase
echo ===================================================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set HADOOP_HOME=C:\hadoop
set HBASE_HOME=C:\hbase-2.6.7-hadoop3
set PATH=%JAVA_HOME%\bin;%HADOOP_HOME%\bin;%HBASE_HOME%\bin;%PATH%

echo [1/3] Checking / Starting Apache HBase...
start "HBase Master" cmd /c "title HBase Master && hbase.cmd master start"

echo Waiting for HBase to become ready on ports 2181 and 16000...
:waitForHBase
powershell -NoProfile -Command "$tcp = New-Object System.Net.Sockets.TcpClient; try { $tcp.Connect('127.0.0.1',16000); $tcp.Close(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto waitForHBase
)

echo [2/3] Compiling Java Backend...
if not exist "target\classes" mkdir target\classes
javac -cp "C:\hbase-2.6.7-hadoop3\lib\shaded-clients\hbase-shaded-client-2.6.7-hadoop3.jar;C:\hbase-2.6.7-hadoop3\lib\client-facing-thirdparty\*" -d target\classes src\main\java\com\vehicleservice\*.java src\main\java\com\vehicleservice\config\*.java src\main\java\com\vehicleservice\dao\*.java src\main\java\com\vehicleservice\model\*.java src\main\java\com\vehicleservice\server\*.java src\main\java\com\vehicleservice\util\*.java

echo [3/3] Starting Application Web Server on http://localhost:8080 ...
java --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.net.dns=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED -Dhadoop.home.dir=C:\hadoop -cp "target\classes;C:\hbase-2.6.7-hadoop3\lib\shaded-clients\hbase-shaded-client-2.6.7-hadoop3.jar;C:\hbase-2.6.7-hadoop3\lib\client-facing-thirdparty\*" com.vehicleservice.Main 8080

pause

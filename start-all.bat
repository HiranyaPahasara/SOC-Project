@echo off
REM Starts both Spring Boot backends, then opens the unified frontend.
REM Pre-req: MongoDB for temp + currency. Run start-mongodb.bat first if needed.

echo Starting MongoDB for currency if not already running...
call "%~dp0start-mongodb.bat"

set ROOT=%~dp0

echo Starting Temperature backend (port 8181)...
start "Temperature Backend (8181)" cmd /k "cd /d "%ROOT%tempconverter" && mvnw.cmd spring-boot:run"

echo Starting Currency backend (port 8081)...
start "Currency Backend (8081)" cmd /k "cd /d "%ROOT%currencyconverter" && mvnw.cmd spring-boot:run"

echo.
echo Waiting 30 seconds for backends to boot...
timeout /t 30 /nobreak >nul

echo Opening frontend in default browser...
start "" "%ROOT%tempconverter\frontend\index.html"

echo.
echo All set! Two backend windows are running. Close them when you are done.
pause

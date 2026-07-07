@echo off
REM Starts both Spring Boot backends in new windows, then opens the frontend.
REM Pre-req: MongoDB on 27017 (temp) + 27018 (currency)

set ROOT=%~dp0..\..

echo Starting Temperature backend (port 8181)...
start "Temperature Backend (8181)" cmd /k "cd /d "%ROOT%\tempconverter" && mvnw.cmd spring-boot:run"

echo Starting Currency backend (port 8081)...
start "Currency Backend (8081)" cmd /k "cd /d "%ROOT%\currencyconverter" && mvnw.cmd spring-boot:run"

echo.
echo Waiting 25 seconds for backends to boot...
timeout /t 25 /nobreak >nul

echo Opening frontend in default browser...
start "" "%~dp0index.html"

echo.
echo All set! Two backend windows are running. Close them when you are done.
pause

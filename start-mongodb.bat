@echo off
REM Starts a local MongoDB instance for currencyconverter.
REM Temperature uses the default MongoDB service.

set MONGOD="C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe"
set CFG=%~dp0mongodb\mongod.cfg

if not exist "%~dp0mongodb\data" mkdir "%~dp0mongodb\data"
if not exist "%~dp0mongodb\log" mkdir "%~dp0mongodb\log"

echo Starting MongoDB for currency...
start "MongoDB Currency" cmd /k %MONGOD% --config %CFG%

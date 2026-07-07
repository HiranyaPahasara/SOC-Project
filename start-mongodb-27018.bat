@echo off
REM Starts a second MongoDB instance for currencyconverter on port 27018.
REM Temperature uses the default MongoDB service on port 27017.

set MONGOD="C:\Program Files\MongoDB\Server\8.2\bin\mongod.exe"
set CFG=%~dp0mongodb-27018\mongod.cfg

if not exist "%~dp0mongodb-27018\data" mkdir "%~dp0mongodb-27018\data"
if not exist "%~dp0mongodb-27018\log" mkdir "%~dp0mongodb-27018\log"

echo Starting MongoDB for currency on port 27018...
start "MongoDB Currency (27018)" cmd /k %MONGOD% --config %CFG%

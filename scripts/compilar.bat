@echo off
setlocal
for %%I in ("%~dp0..") do set "AGROGEST_ROOT=%%~fI"
cd /d "%AGROGEST_ROOT%"
call mvn clean verify
if errorlevel 1 (pause & exit /b 1)
if not exist dist mkdir dist
copy /y target\agrogest.war dist\agrogest.war >nul
echo Compilacao e testes concluidos. WAR actualizado em dist\agrogest.war.
pause

@echo off
setlocal
cd /d "%~dp0"
call mvn clean verify
if errorlevel 1 (pause & exit /b 1)
if not exist dist mkdir dist
copy /y target\agrogest.war dist\agrogest.war >nul
echo Compilacao e testes concluidos. WAR actualizado em dist\agrogest.war.
pause

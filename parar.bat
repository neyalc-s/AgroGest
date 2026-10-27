@echo off
setlocal
if not defined JAVA_HOME if not defined JRE_HOME (
  for /f "tokens=1,* delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do for /f "tokens=*" %%C in ("%%B") do set "JRE_HOME=%%C"
)
set "CATALINA_HOME=%~dp0runtime\tomcat"
set "CATALINA_BASE=%CATALINA_HOME%"
call "%CATALINA_HOME%\bin\shutdown.bat"
echo Aguarde a confirmacao de gravacao no terminal do servidor.
pause

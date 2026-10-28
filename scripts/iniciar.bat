@echo off
setlocal
for %%I in ("%~dp0..") do set "AGROGEST_ROOT=%%~fI"
cd /d "%AGROGEST_ROOT%"
if defined JAVA_HOME (set "AGROGEST_JAVA=%JAVA_HOME%\bin\java.exe") else if defined JRE_HOME (set "AGROGEST_JAVA=%JRE_HOME%\bin\java.exe") else (set "AGROGEST_JAVA=java")
"%AGROGEST_JAVA%" -version >nul 2>&1
if errorlevel 1 (
  echo Java nao encontrado. Instale Java 17 e configure JAVA_HOME ou o PATH.
  pause
  exit /b 1
)
if not defined JAVA_HOME if not defined JRE_HOME (
  for /f "tokens=1,* delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do for /f "tokens=*" %%C in ("%%B") do set "JRE_HOME=%%C"
)
set "CATALINA_HOME=%AGROGEST_ROOT%\runtime\tomcat"
set "CATALINA_BASE=%CATALINA_HOME%"
if not defined AGROGEST_DATA_DIR set "AGROGEST_DATA_DIR=%AGROGEST_ROOT%\dados"
if not defined AGROGEST_DEMO_LOGIN set "AGROGEST_DEMO_LOGIN=true"
if not exist "%AGROGEST_DATA_DIR%" mkdir "%AGROGEST_DATA_DIR%"
if not exist "%CATALINA_BASE%\webapps" mkdir "%CATALINA_BASE%\webapps"
copy /y "%AGROGEST_ROOT%\dist\agrogest.war" "%CATALINA_BASE%\webapps\agrogest.war" >nul
if errorlevel 1 (pause & exit /b 1)
echo AgroGest: http://localhost:8081/agrogest/
echo Para parar e gravar, abra scripts\parar.bat noutra janela.
call "%CATALINA_HOME%\bin\catalina.bat" run
pause

@echo off
cd /d "%~dp0"
set "JAVAW="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
if not defined JAVAW for %%P in (javaw.exe) do if not "%%~$PATH:P"=="" set "JAVAW=%%~$PATH:P"
if not defined JAVAW for /d %%D in ("%USERPROFILE%\.jdks\*" "%ProgramFiles%\Java\*" "%ProgramFiles%\Eclipse Adoptium\*" "%ProgramFiles%\Microsoft\jdk-*" "%ProgramFiles%\Zulu\*" "%ProgramFiles%\Amazon Corretto\*" "%ProgramFiles%\BellSoft\*") do if exist "%%~D\bin\javaw.exe" set "JAVAW=%%~D\bin\javaw.exe"
if not defined JAVAW (
    echo Nao foi encontrado o Java. Instale o Java 17 ou superior e volte a tentar.
    pause
    exit /b 1
)
start "" "%JAVAW%" -jar "%~dp0Hotel4You.jar"

@echo off
cd /d "%~dp0"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" (
    start "" "%JAVA_HOME%\bin\javaw.exe" -jar "%~dp0Hotel4You.jar"
    exit /b 0
)
where javaw >nul 2>nul
if errorlevel 1 (
    echo Nao foi encontrado o Java. Instale o Java 17 ou superior e volte a tentar.
    pause
    exit /b 1
)
start "" javaw -jar "%~dp0Hotel4You.jar"

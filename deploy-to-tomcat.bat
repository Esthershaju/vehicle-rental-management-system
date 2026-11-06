@echo off
echo ==============================================================
echo Deploying Vehicle Rental Management System to Apache Tomcat
echo ==============================================================

set MAVEN_CMD=mvn
if exist "..\apache-maven-3.9.9\bin\mvn.cmd" (
    set MAVEN_CMD=..\apache-maven-3.9.9\bin\mvn.cmd
)

set "TOMCAT_DIR=%~dp0..\apache-tomcat-10.1.60"
if not exist "%TOMCAT_DIR%\bin\catalina.bat" (
    echo [ERROR] Tomcat not found at %TOMCAT_DIR%
    pause
    exit /b 1
)
set "CATALINA_HOME=%TOMCAT_DIR%"

echo 1. Packaging application...
call %MAVEN_CMD% package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Packaging failed.
    pause
    exit /b 1
)

echo 2. Copying WAR to Tomcat webapps directory...
copy /Y target\vehicle-rental.war "%TOMCAT_DIR%\webapps\"

echo 3. Starting Apache Tomcat...
echo Application will be accessible at:
echo http://localhost:8080/vehicle-rental/
echo.
call "%TOMCAT_DIR%\bin\startup.bat"

echo Tomcat started! Check the Tomcat console window.
pause

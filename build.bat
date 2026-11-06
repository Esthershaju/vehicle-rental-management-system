@echo off
echo ==============================================================
echo Building Vehicle Rental Management System (WAR Package)
echo ==============================================================

set MAVEN_CMD=mvn
if exist "..\apache-maven-3.9.9\bin\mvn.cmd" (
    set MAVEN_CMD=..\apache-maven-3.9.9\bin\mvn.cmd
)

echo Using Maven: %MAVEN_CMD%
call %MAVEN_CMD% clean package

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==============================================================
    echo BUILD SUCCESSFUL!
    echo WAR File generated at: target\vehicle-rental.war
    echo ==============================================================
) else (
    echo.
    echo [ERROR] Build failed with exit code %ERRORLEVEL%.
)
pause

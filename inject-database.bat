@echo off
REM ==============================================================================
REM Vehicle Rental Management System - One-Click Database Injector
REM ==============================================================================
REM Executes 'database\inject-database.js' to inject schema, constraints,
REM and seed records directly into the MySQL CLI backend.
REM Compatible with both Node.js and native Windows Script Host (cscript).
REM ==============================================================================

setlocal enabledelayedexpansion

echo ====================================================================
echo  VEHICLE RENTAL MANAGEMENT SYSTEM - DATABASE INJECTOR RUNNER
echo ====================================================================

where node >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [Environment] Detected Node.js runtime.
    node "%~dp0database\inject-database.js" %*
) else (
    echo [Environment] Using native Windows JavaScript Host: cscript
    cscript //nologo "%~dp0database\inject-database.js" %*
)

echo.

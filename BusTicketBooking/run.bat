@echo off
echo ============================================
echo   Bus Ticket Booking System - GUI Launcher
echo ============================================

cd /d "%~dp0"

echo Compiling...
javac -encoding UTF-8 *.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo Starting application...
java -Dfile.encoding=UTF-8 BookingSystem

pause

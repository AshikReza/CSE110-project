@echo off
title Bus Ticket Booking System
color 0A

echo.
echo  =============================================
echo    BUS TICKET BOOKING SYSTEM — Java Project
echo  =============================================
echo.
echo  [1/2] Compiling Java files...
echo.

javac -encoding UTF-8 Vehicle.java Bus.java Ticket.java BookingSystem.java

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Compilation failed. Make sure Java JDK is installed.
    echo  Download from: https://www.oracle.com/java/technologies/downloads/
    echo.
    pause
    exit /b 1
)

echo  [2/2] Compilation successful! Starting application...
echo.
echo  =============================================
echo.

java BookingSystem

echo.
echo  =============================================
echo    Session ended. Press any key to close.
echo  =============================================
pause > nul

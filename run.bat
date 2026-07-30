@echo off
title Quizfy - Console Flashcard Maker

:: Go into the src folder (relative to where this bat file is)
cd /d "%~dp0src"

echo =====================================
echo   Compiling Quizfy...
echo =====================================

javac Main.java Deck.java Question.java PromptTools.java InvalidPromptFormatException.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed.
    echo Make sure Java JDK is installed and added to PATH.
    pause
    exit /b 1
)

echo Compilation successful. Starting app...
echo.
java Main

echo.
echo =====================================
echo   Quizfy has exited.
echo =====================================
pause

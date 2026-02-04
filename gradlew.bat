@rem Gradle startup script for Windows. Run 'gradle wrapper' to generate full wrapper if needed.
@if "%DEBUG%"=="" @echo off

where gradle >nul 2>nul
if %ERRORLEVEL% equ 0 (
    gradle %*
) else (
    echo Gradle not found. Install Gradle or run from an IDE with Gradle support.
    exit /b 1
)

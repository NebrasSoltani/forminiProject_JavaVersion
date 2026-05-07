@echo off
echo Test de compilation du projet...
cd /d "c:\Users\a\Desktop\pi java\forminiProject_JavaVersion"

echo Compilation avec Maven...
call mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation réussie !
) else (
    echo ❌ Erreur de compilation
)

pause

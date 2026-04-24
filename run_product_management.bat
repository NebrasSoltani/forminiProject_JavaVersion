@echo off
echo Starting Product Management Interface...
echo.

cd /d "%~dp0"

echo Running: tn.formini.product.launchers.ProduitManagementLauncher
java -cp "src/main/java" tn.formini.product.launchers.ProduitManagementLauncher

if errorlevel 1 (
    echo.
    echo Error occurred while running the application.
    echo Make sure Java is properly installed and configured.
    echo.
    pause
)

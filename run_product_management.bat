@echo off
echo Starting Product Management Interface (via Maven javafx:run)...
echo.

cd /d "%~dp0"

echo Running: mvn javafx:run
mvn javafx:run

if errorlevel 1 (
    echo.
    echo Error occurred while running 'mvn javafx:run'.
    echo Make sure Maven is installed and in PATH and JavaFX dependencies are available.
    echo To run a packaged jar instead, use:
    echo   java --module-path ^"<path-to-javafx-sdk-lib>^" --add-modules=javafx.controls,javafx.fxml,javafx.web -jar target\yourapp.jar
    echo.
    pause
)

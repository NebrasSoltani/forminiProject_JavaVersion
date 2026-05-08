Write-Host "Starting Product Management Interface (via Maven javafx:run)..." -ForegroundColor Green
Write-Host ""

Set-Location $PSScriptRoot

Write-Host "Running via Maven plugin: javafx:run" -ForegroundColor Yellow
try {
    mvn javafx:run
} catch {
    Write-Host "Error occurred while running 'mvn javafx:run'." -ForegroundColor Red
    Write-Host "Make sure Maven is installed and in PATH, and JavaFX dependencies are available." -ForegroundColor Red
    Write-Host "If you prefer to run the packaged jar, use: java --module-path <javafx-sdk-lib> --add-modules=javafx.controls,javafx.fxml,javafx.web -jar target\yourapp.jar" -ForegroundColor Yellow
    Write-Host "Press any key to continue..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

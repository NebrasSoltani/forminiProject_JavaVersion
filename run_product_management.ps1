Write-Host "Starting Product Management Interface..." -ForegroundColor Green
Write-Host ""

Set-Location $PSScriptRoot

Write-Host "Running: tn.formini.product.launchers.ProduitManagementLauncher" -ForegroundColor Yellow
try {
    java -cp "src/main/java" tn.formini.product.launchers.ProduitManagementLauncher
} catch {
    Write-Host "Error occurred while running the application." -ForegroundColor Red
    Write-Host "Make sure Java is properly installed and configured." -ForegroundColor Red
    Write-Host ""
    Write-Host "Press any key to continue..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

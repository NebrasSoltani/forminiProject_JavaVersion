@echo off
echo Testing Java environment...
echo.

echo Java version:
java -version
echo.

echo Current directory:
cd
echo.

echo Trying to run simple test...
java -cp "src/main/java" tn.formini.product.launchers.ProduitManagementLauncher

pause

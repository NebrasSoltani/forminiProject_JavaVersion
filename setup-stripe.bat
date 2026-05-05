@echo off
echo === CONFIGURATION AUTOMATIQUE STRIPE ===
echo.

REM Définir la variable d'environnement pour la session actuelle
set STRIPE_SECRET_KEY=votre_clé_stripe_test_ici

echo ✅ Clé Stripe définie pour cette session
echo.
echo Pour configurer de manière permanente (Windows PowerShell) :
echo $env:STRIPE_SECRET_KEY="votre_clé_stripe_test_ici"
echo.
echo Pour configurer de manière permanente (Windows CMD) :
echo setx STRIPE_SECRET_KEY "votre_clé_stripe_test_ici"
echo.
echo Pour obtenir une clé de test :
echo 1. Allez sur https://dashboard.stripe.com/apikeys
echo 2. Créez une clé de test
echo 3. Remplacez "votre_clé_stripe_test_ici" par votre clé
echo.
echo === CONFIGURATION TERMINÉE ===
echo Lancez maintenant l'application et testez le paiement Stripe !
pause

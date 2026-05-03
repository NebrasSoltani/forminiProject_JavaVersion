# Configuration Stripe - Guide Rapide

## 🚀 Pour tester rapidement (sans risque pour Git)

### Option 1 : Script Windows (recommandé)
1. Double-cliquez sur `SETUP_STRIPE_KEYS.bat`
2. Lancez votre application depuis le même terminal
3. Vos clés seront configurées pour cette session uniquement

### Option 2 : Manuel (terminal)
```cmd
set STRIPE_SECRET_KEY=sk_test_51YOUR_STRIPE_SECRET_KEY_HERE
set STRIPE_PUBLIC_KEY=pk_test_51YOUR_STRIPE_PUBLIC_KEY_HERE
```

### Option 3 : Variables d'environnement Windows (permanent)
1. Panneau de configuration > Système > Variables d'environnement
2. Ajouter les variables :
   - `STRIPE_SECRET_KEY` = `sk_test_51YOUR_STRIPE_SECRET_KEY_HERE`
   - `STRIPE_PUBLIC_KEY` = `pk_test_51YOUR_STRIPE_PUBLIC_KEY_HERE`

## 🔒 Sécurité Git

✅ **AUCUN RISQUE** pour votre dépôt Git car :
- Pas de fichier de configuration avec des clés secrètes
- Variables d'environnement uniquement
- Fichiers sensibles déjà dans `.gitignore`

## 🎯 Test du paiement

1. Configurez les variables avec une des options ci-dessus
2. Lancez l'application
3. Ajoutez des produits au panier
4. Cliquez sur "Payer avec Stripe"
5. Utilisez les cartes de test Stripe :
   - `4242 4242 4242 4242` - Succès
   - `4000 0000 0000 0002` - Échec

## 📝 Notes importantes

- Les clés fournies sont des clés de **TEST** uniquement
- Aucune transaction réelle ne sera effectuée
- Les variables ne sont valables que pour la session terminal (Option 1 & 2)
- Pour la production, utilisez des clés live et configurez les webhooks

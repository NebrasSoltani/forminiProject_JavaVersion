# Guide: Ajouter des Recommandations YouTube et Google Books dans les Formations

## 🎯 Objectif
Permettre aux formateurs d'enrichir leurs formations en ajoutant des recommandations de livres (Google Books) et de vidéos (YouTube) comme leçons.

## 📦 Composants Implémentés

### 1. **Service de Recommandations** 
- **Fichier**: `src/main/java/tn/formini/services/recommendations/FormationRecommendationService.java`
- **Fonctionnalité**:
  - Récupère les livres depuis l'API Google Books
  - Récupère les vidéos depuis l'API YouTube
  - Fournit des suggestions locales en cas d'indisponibilité des APIs
  - Classes imbriquées: `BookRecommendation`, `VideoRecommendation`, `RecommendationResult`

### 2. **Interface pour les Apprenants**
- **Fichier FXML**: `src/main/resources/fxml/formations/formation-learning.fxml`
- **Contrôleur**: `src/main/java/tn/formini/controllers/formations/FormationLearningController.java`
- **Fonctionnalités**:
  - Onglets pour afficher les leçons, les livres recommandés et les vidéos
  - Sélection et affichage des détails
  - Bouton "Voir Recommandations"
  - Publication rapide comme leçon

### 3. **Interface pour les Formateurs**
- **Fichier FXML**: `src/main/resources/fxml/formations/formation-recommendations.fxml`
- **Contrôleur**: `src/main/java/tn/formini/controllers/formations/FormationRecommendationsController.java`
- **Fonctionnalités**:
  - Interface dédiée pour gérer les recommandations
  - Deux onglets: Livres et Vidéos
  - Affichage des détails (auteur, éditeur, chaîne, etc.)
  - Boutons: Publier, Ouvrir dans navigateur, Actualiser

### 4. **Intégration dans la Gestion des Formations**
- **Modifications**:
  - Ajout du bouton "Recommandations" dans `formation-crud.fxml` (Mes formations)
  - Ajout du bouton "Recommandations" dans `lecon-crud.fxml` (Gestion des leçons)
  - Méthode `handleRecommendations()` dans `FormationCrudController`
  - Méthode `handleRecommendations()` dans `LeconCrudController`

## 🔑 Configuration des APIs

### Clés API
Les clés API doivent être stockées dans le fichier `.env` à la racine du projet:

```dotenv
YOUTUBE_API_KEY=votre_cle_youtube
YOUTUBE_MAX_RESULTS=15
GOOGLE_BOOKS_API_KEY=votre_cle_google_books
GOOGLE_BOOKS_MAX_RESULTS=15
```

### Chargement des Clés
Les clés sont chargées via `DotEnvLoader.java` et injectées comme propriétés système Java au démarrage de l'application.

## 📚 Flux d'Utilisation Apprenant

1. L'apprenant accède à "Formation Learning"
2. Dans la liste des formations, il sélectionne une formation
3. Il clique sur "Voir Recommandations" ou bascule les onglets
4. Il sélectionne un livre ou une vidéo
5. Les détails s'affichent
6. Il peut ouvrir le lien ou cliquer "Publier comme leçon"

## 👨‍🏫 Flux d'Utilisation Formateur

### Option 1: Depuis "Mes Formations"
1. Formateur accède à "Mes formations" (FormationCrudController)
2. Il sélectionne une formation
3. Il clique le bouton "Recommandations" (violet)
4. La fenêtre de recommandations s'ouvre

### Option 2: Depuis "Gestion des Leçons"
1. Formateur accède à "Mes formations"
2. Il clique "Gerer les lecons" sur la formation
3. Il clique "Recommandations" (violet)
4. La fenêtre de recommandations s'ouvre

### Dans la Fenêtre Recommandations
1. Les livres et vidéos se chargent automatiquement
2. Formateur sélectionne un livre ou vidéo
3. Les détails (auteur, chaîne, description) s'affichent
4. Il clique "✓ Publier comme leçon"
5. La leçon est créée et ajoutée à la formation
6. Un message de succès s'affiche

## ✅ Validation et Sauvegarde

### Validation des Titres
- **Limite**: 255 caractères maximum
- **Application**: Avant insertion en base de données
- **Normalisation**: Suppression des retours à la ligne
- **Troncature**: Les titres trop longs sont automatiquement tronqués

### Détection des Doublons
- Les titres des leçons sont vérifiés pour éviter les doublons
- Alerte utilisateur si le titre existe déjà

### Contenu de la Leçon
- **Titre**: Titre du livre/vidéo (normalisé)
- **Description**: Description du livre/vidéo
- **URL**: Lien preview du livre ou URL YouTube
- **Contenu**: Label source + informations additionnelles
- **Gratuit**: Coché par défaut
- **Ordre**: Automatiquement assigné

## 🔄 Flux de Données

```
Google Books API / YouTube API
        ↓
FormationRecommendationService
        ↓
FormationRecommendationsController (Formateur)
FormationLearningController (Apprenant)
        ↓
LeconService.ajouter(newLecon)
        ↓
Leçon sauvegardée en base de données
```

## 🛠️ Maintenance

### Erreurs Courantes
1. **APIs non répondants**: Le système fournit des suggestions locales
2. **Titres trop longs**: Sont automatiquement tronqués à 255 caractères
3. **Doublons**: Avertissement utilisateur
4. **Connexion réseau**: Vérifier la connexion et réessayer

### Logs
- Les messages de chargement/erreur s'affichent dans console
- Le label "statusLabel" indique l'état du traitement

## 📝 Notes Techniques

- **Framework**: JavaFX 21
- **APIs**: Google Books v1, YouTube v3
- **HTTP Client**: java.net.http.HttpClient (Java 11+)
- **Parsing JSON**: Manuel (regex) pour éviter les dépendances externes
- **Validations**: Trois niveaux (entité, form, publication)

## 🎨 UX/UI Améliorations

- Icônes emoji dans les onglets (📚, 🎥)
- Codage couleur des boutons (vert=action, bleu=ouvrir, violet=recommandations)
- Labels informatifs (compteur, détails)
- Affichage dynamique des détails au survol
- Rafraîchir disponible à tout moment

## 🚀 Prochaines Améliorations Possibles

1. Cache local des recommandations
2. Historique des recommandations publiées
3. Note/Évaluation des recommandations
4. Partage de recommandations entre formateurs
5. Recommandations automatisées basées sur profil de formation
6. Intégration de plus de sources (Udemy, Coursera, etc.)


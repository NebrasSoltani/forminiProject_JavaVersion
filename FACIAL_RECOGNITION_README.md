# Reconnaissance Faciale - Documentation

## Vue d'ensemble

L'intégration de la reconnaissance faciale AI a été ajoutée au projet Formini. Cette fonctionnalité permet aux utilisateurs de s'authentifier via leur visage au lieu d'utiliser uniquement email/mot de passe.

## Composants Ajoutés

### 1. Dépendances Maven
- **OpenCV 4.7.0-0** (org.openpnp:opencv) - Bibliothèque de vision par ordinateur pour la détection et reconnaissance faciale

### 2. Services Créés

#### FaceRecognitionService
- **Emplacement**: `src/main/java/tn/formini/services/face/FaceRecognitionService.java`
- **Fonctionnalités**:
  - Détection de visages dans les images
  - Extraction d'encodages faciaux (représentation numérique du visage)
  - Comparaison de visages pour la similarité
  - Reconnaissance de visages pré-enregistrés
  - Entraînement du modèle de reconnaissance

#### CameraCaptureService
- **Emplacement**: `src/main/java/tn/formini/services/face/CameraCaptureService.java`
- **Fonctionnalités**:
  - Accès à la webcam
  - Capture de flux vidéo en temps réel
  - Capture d'images individuelles
  - Conversion OpenCV Mat vers JavaFX Image

### 3. Modifications des Entités

#### User Entity
- **Emplacement**: `src/main/java/tn/formini/entities/Users/User.java`
- **Champs ajoutés**:
  - `face_encoding` (byte[]) - Stocke l'encodage facial de l'utilisateur
  - `face_auth_enabled` (boolean) - Indique si l'authentification faciale est activée

### 4. Modifications des Contrôleurs

#### LoginController
- **Emplacement**: `src/main/java/tn/formini/controllers/auth/LoginController.java`
- **Nouvelles fonctionnalités**:
  - Bouton de connexion par reconnaissance faciale
  - Panneau de caméra pour la capture en temps réel
  - Contrôles pour démarrer/arrêter la caméra
  - Capture et comparaison de visage pour l'authentification

#### SignupController
- **Emplacement**: `src/main/java/tn/formini/controllers/auth/SignupController.java`
- **Nouvelles fonctionnalités**:
  - Option pour activer l'authentification faciale lors de l'inscription
  - Capture de visage lors de l'inscription
  - Stockage de l'encodage facial dans le profil utilisateur

### 5. Ressources
- **Haar Cascade XML**: `src/main/resources/haarcascade_frontalface_alt.xml`
  - Modèle pré-entraîné pour la détection de visages
  - Téléchargé depuis le dépôt officiel OpenCV

## Configuration Requise

### 1. Base de Données

Ajoutez les colonnes suivantes à votre table `users`:

```sql
ALTER TABLE users 
ADD COLUMN face_encoding LONGBLOB NULL,
ADD COLUMN face_auth_enabled BOOLEAN DEFAULT FALSE;
```

### 2. Installation d'OpenCV

Le projet utilise OpenCV via Maven, mais vous devez vous assurer que:
- OpenCV est correctement chargé (la bibliothèque native est chargée automatiquement par `nu.pattern.OpenCV`)
- Les pilotes de webcam sont installés sur votre système

### 3. Webcam
- Une webcam fonctionnelle est requise pour la capture de visages
- Assurez-vous que les permissions d'accès à la caméra sont accordées

## Utilisation

### Pour les Utilisateurs (Inscription)

1. Remplissez le formulaire d'inscription habituel
2. Cochez la case "Activer l'authentification faciale"
3. Cliquez sur le bouton "Capturer le visage"
4. La caméra s'ouvrira - positionnez votre visage devant
5. Cliquez sur "Démarrer la caméra"
6. Cliquez sur "Capturer" pour prendre une photo de votre visage
7. Si un visage est détecté, l'encodage sera sauvegardé
8. Complétez l'inscription

### Pour les Utilisateurs (Connexion)

1. Sur la page de connexion, cliquez sur "Connexion par reconnaissance faciale"
2. Cliquez sur "Démarrer la caméra"
3. Positionnez votre visage devant la caméra
4. Cliquez sur "Capturer"
5. Si votre visage correspond à un compte enregistré, vous serez connecté automatiquement

## Implémentation Restante

### Recherche d'Utilisateur par Encodage Facial

La méthode `findUserByFaceEncoding(byte[] encoding)` dans `LoginController` doit être implémentée avec l'intégration de la base de données.

**Implémentation suggérée**:

```java
private User findUserByFaceEncoding(byte[] encoding) {
    // 1. Récupérer tous les utilisateurs avec face_auth_enabled = true
    List<User> usersWithFaceAuth = loginService.getUsersWithFaceAuthEnabled();
    
    // 2. Pour chaque utilisateur, comparer les encodages
    for (User user : usersWithFaceAuth) {
        if (user.getFace_encoding() != null) {
            double similarity = faceService.compareFaces(encoding, user.getFace_encoding());
            // Seuil de similarité à ajuster selon vos besoins (0.7 = 70%)
            if (similarity > 0.7) {
                return user;
            }
        }
    }
    
    return null;
}
```

**Ajoutez cette méthode à votre LoginService**:

```java
public List<User> getUsersWithFaceAuthEnabled() {
    String query = "SELECT * FROM users WHERE face_auth_enabled = true";
    // Implémentez la récupération depuis la base de données
    // Retournez une liste d'objets User avec leurs encodages faciaux
}
```

## Limitations Actuelles

1. **Comparaison d'encodages**: L'implémentation actuelle utilise une comparaison euclidienne simple. Pour une meilleure précision, envisagez d'utiliser des réseaux de neurones profonds (Deep Learning) pour l'extraction de caractéristiques faciales.

2. **Base de données**: La méthode `findUserByFaceEncoding` doit être connectée à votre base de données pour rechercher les utilisateurs enregistrés.

3. **Performance**: La comparaison séquentielle de tous les utilisateurs peut être lente pour de grandes bases de données. Envisagez d'utiliser des index de recherche vectorielle pour des performances optimales.

4. **Conditions d'éclairage**: La reconnaissance faciale fonctionne mieux avec un bon éclairage et un visage bien orienté vers la caméra.

## Sécurité

- Les encodages faciaux sont stockés sous forme de bytes dans la base de données
- L'authentification faciale est optionnelle - les utilisateurs peuvent toujours utiliser email/mot de passe
- Le seuil de similarité peut être ajusté pour équilibrer sécurité et facilité d'utilisation

## Dépannage

### "Service de reconnaissance faciale non initialisé"
- Vérifiez qu'OpenCV est correctement chargé
- Assurez-vous que le fichier `haarcascade_frontalface_alt.xml` est dans les ressources

### "Aucune caméra détectée"
- Vérifiez que votre webcam est connectée
- Vérifiez les permissions d'accès à la caméra dans votre système
- Essayez de changer l'index de la caméra (0, 1, 2...)

### "Aucun visage détecté dans l'image"
- Assurez-vous d'avoir un bon éclairage
- Positionnez votre visage bien face à la caméra
- Évitez les mouvements rapides pendant la capture

## Améliorations Futures Possibles

1. **Liveness Detection**: Détecter si le visage est réel ou une photo/vidéo
2. **Multi-face Recognition**: Reconnaître plusieurs visages dans une seule image
3. **3D Face Recognition**: Utiliser la profondeur pour une meilleure précision
4. **Anti-spoofing**: Mesures de sécurité contre les attaques de présentation
5. **Emotion Recognition**: Détecter les émotions faciales
6. **Age and Gender Estimation**: Estimer l'âge et le genre

## Support

Pour toute question ou problème concernant l'intégration de la reconnaissance faciale, consultez la documentation OpenCV officielle ou contactez l'équipe de développement.

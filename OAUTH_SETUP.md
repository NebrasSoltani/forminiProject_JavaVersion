# OAuth Setup Guide for Formini

This guide will help you configure Google and GitHub OAuth authentication for the Formini application.

## Prerequisites

- A Google Cloud account
- A GitHub account
- Access to the Formini codebase

## Step 1: Google OAuth Setup

### 1.1 Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Navigate to **APIs & Services** > **Credentials**

### 1.2 Configure OAuth Consent Screen

1. Click on **OAuth consent screen**
2. Choose **External** (for public access)
3. Fill in the required information:
   - App name: Formini
   - User support email: your email
   - Developer contact: your email
4. Add the following scopes:
   - `openid`
   - `email`
   - `profile`
5. Save and continue

### 1.3 Create OAuth Credentials

1. Go to **Credentials** > **Create Credentials** > **OAuth client ID**
2. Application type: **Web application**
3. Name: Formini Web Client
4. Authorized redirect URIs:
   - `http://localhost:8080/callback/google`
5. Click **Create**
6. Copy the **Client ID** and **Client Secret**

## Step 2: GitHub OAuth Setup

### 2.1 Create GitHub OAuth App

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click on **New OAuth App**
3. Fill in the application details:
   - Application name: Formini
   - Homepage URL: `http://localhost:8080`
   - Application description: Formini Training Platform
   - Authorization callback URL: `http://localhost:8080/callback/github`
4. Click **Register application**
5. Copy the **Client ID** and generate a **Client Secret**

## Step 3: Configure Formini

### 3.1 Update oauth.properties

Open `src/main/resources/oauth.properties` and replace the placeholder values:

```properties
# Google OAuth 2.0
google.client.id=YOUR_GOOGLE_CLIENT_ID
google.client.secret=YOUR_GOOGLE_CLIENT_SECRET
google.redirect.uri=http://localhost:8080/callback/google

# GitHub OAuth 2.0
github.client.id=YOUR_GITHUB_CLIENT_ID
github.client.secret=YOUR_GITHUB_CLIENT_SECRET
github.redirect.uri=http://localhost:8080/callback/github
```

Replace:
- `YOUR_GOOGLE_CLIENT_ID` with your Google Client ID
- `YOUR_GOOGLE_CLIENT_SECRET` with your Google Client Secret
- `YOUR_GITHUB_CLIENT_ID` with your GitHub Client ID
- `YOUR_GITHUB_CLIENT_SECRET` with your GitHub Client Secret

### 3.2 Important Security Notes

- **Never commit** `oauth.properties` with real credentials to version control
- Add `oauth.properties` to `.gitignore` if not already there
- Use environment variables for production deployments
- Keep your client secrets secure

## Step 4: Test the Integration

1. Rebuild the project with Maven:
   ```bash
   mvn clean install
   ```

2. Run the application:
   ```bash
   mvn javafx:run
   ```

3. Navigate to the login page
4. Click on "Continuer avec Google" or "Continuer avec GitHub"
5. Complete the OAuth flow in your browser
6. Verify that you are successfully logged in

## How It Works

### Authentication Flow

1. User clicks OAuth button (Google/GitHub)
2. Application opens browser with OAuth authorization URL
3. User grants permissions
4. OAuth provider redirects to local callback server (port 8080)
5. Application exchanges authorization code for access token
6. Application fetches user information from OAuth provider
7. Application creates or updates user in database
8. User is logged in and redirected to dashboard

### User Creation

- **New users**: Automatically created with OAuth provider information
- **Existing users**: Updated with OAuth provider information if email matches
- **Default role**: New users are assigned "apprenant" role
- **Email verification**: Automatically marked as verified

### Database Schema

The User entity already includes OAuth-related fields:
- `google_id`: Google user ID
- `github_id`: GitHub user ID
- `oauth_provider`: Provider name ("google" or "github")
- `avatar_url`: Profile picture URL
- `is_email_verified`: Set to true for OAuth users

## Troubleshooting

### Port 8080 Already in Use

If port 8080 is already in use, you can change it in:
- `OAuthConfig.java` - Update redirect URIs
- `OAuthCallbackHandler.java` - Update server port (line 48)
- OAuth provider console - Update authorized redirect URIs

### "OAuth not configured" Error

This error appears when:
- OAuth credentials are still placeholder values
- `oauth.properties` file is missing
- Credentials are not properly loaded

### GitHub Email Not Public

If GitHub email is private, the authentication will fail. Users must:
- Make their email public in GitHub settings, or
- Use a different OAuth provider

### Google API Not Enabled

Ensure the following APIs are enabled in Google Cloud Console:
- Google+ API (if using older scopes)
- People API (for profile information)

## Production Deployment

For production deployment:

1. Use HTTPS redirect URIs instead of HTTP
2. Configure proper domain names
3. Use environment variables for credentials
4. Implement proper session management
5. Add rate limiting for OAuth requests
6. Implement proper error handling and logging

## Support

For issues or questions:
- Check the application logs for detailed error messages
- Verify OAuth provider console settings
- Ensure redirect URIs match exactly
- Check network connectivity

package tn.formini.services.auth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenExtractor;
import com.github.scribejava.core.model.Verb;

/**
 * Custom GoogleApi20 with required scopes for email and profile access
 */
public class GoogleApi20Custom extends DefaultApi20 {
    private static final GoogleApi20Custom INSTANCE = new GoogleApi20Custom();

    private GoogleApi20Custom() {
    }

    public static GoogleApi20Custom instance() {
        return INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://oauth2.googleapis.com/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuth2AccessTokenExtractor getAccessTokenExtractor() {
        return OAuth2AccessTokenExtractor.instance();
    }

    protected String getScope() {
        // Required scopes for Google OAuth to get email and profile
        return "openid email profile";
    }
}

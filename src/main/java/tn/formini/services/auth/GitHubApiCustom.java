package tn.formini.services.auth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenExtractor;
import com.github.scribejava.core.model.Verb;

/**
 * Custom GitHubApi with required scopes for email access
 */
public class GitHubApiCustom extends DefaultApi20 {
    private static final GitHubApiCustom INSTANCE = new GitHubApiCustom();

    private GitHubApiCustom() {
    }

    public static GitHubApiCustom instance() {
        return INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://github.com/login/oauth/access_token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://github.com/login/oauth/authorize";
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
        // Required scope to get user email
        return "user:email";
    }
}

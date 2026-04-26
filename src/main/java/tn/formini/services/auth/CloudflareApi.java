package tn.formini.services.auth;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenExtractor;
import com.github.scribejava.core.model.Verb;

public class CloudflareApi extends DefaultApi20 {
    private static final CloudflareApi INSTANCE = new CloudflareApi();

    private CloudflareApi() {
    }

    public static CloudflareApi instance() {
        return INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://dash.cloudflare.com/oauth2/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://dash.cloudflare.com/oauth2/authorize";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuth2AccessTokenExtractor getAccessTokenExtractor() {
        return OAuth2AccessTokenExtractor.instance();
    }
}

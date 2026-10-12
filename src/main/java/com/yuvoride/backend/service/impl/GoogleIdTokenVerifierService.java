package com.yuvoride.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.yuvoride.backend.config.AppProperties;
import com.yuvoride.backend.exception.ServiceUnavailableException;
import com.yuvoride.backend.exception.UnauthorizedException;
import com.yuvoride.backend.service.GoogleTokenVerifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
public class GoogleIdTokenVerifierService implements GoogleTokenVerifier {

    private final AppProperties properties;

    public GoogleIdTokenVerifierService(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public GoogleProfile verify(String idToken) {
        String clientId = properties.getGoogle().getClientId();
        if (!StringUtils.hasText(clientId)) {
            throw new ServiceUnavailableException("Google login is not configured. Set GOOGLE_CLIENT_ID.");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new UnauthorizedException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            if (!Boolean.TRUE.equals(payload.getEmailVerified()) || !StringUtils.hasText(email)) {
                throw new UnauthorizedException("Google account email is not verified");
            }
            return new GoogleProfile(
                    payload.getSubject(),
                    email,
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (UnauthorizedException | ServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Unable to validate Google ID token");
        }
    }
}

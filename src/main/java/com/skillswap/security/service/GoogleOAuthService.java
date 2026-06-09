package com.skillswap.security.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.skillswap.user.entity.User;
import com.skillswap.user.enums.AuthProvider;
import com.skillswap.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthService(UserRepository userRepository,
                              @Value("${app.google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    /**
     * Verifies the Google ID token and returns the user.
     * Creates a new user if one doesn't exist with that Google account.
     */
    public User authenticateWithGoogle(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new BadCredentialsException("Invalid Google ID token", e);
        }

        if (idToken == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String pictureUrl = (String) payload.get("picture");

        // Check if user exists by Google ID or email
        return userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            // Link Google account to existing email-registered user
                            existingUser.setGoogleId(googleId);
                            existingUser.setAuthProvider(AuthProvider.GOOGLE);
                            if (pictureUrl != null) {
                                existingUser.setProfilePictureUrl(pictureUrl);
                            }
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            // Create new user from Google profile
                            User newUser = new User();
                            newUser.setEmail(email);
                            newUser.setFirstName(firstName != null ? firstName : "");
                            newUser.setLastName(lastName != null ? lastName : "");
                            newUser.setGoogleId(googleId);
                            newUser.setAuthProvider(AuthProvider.GOOGLE);
                            newUser.setProfilePictureUrl(pictureUrl);
                            return userRepository.save(newUser);
                        })
                );
    }
}

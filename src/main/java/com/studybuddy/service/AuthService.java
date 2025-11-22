package com.studybuddy.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.studybuddy.dto.AuthResponse;
import com.studybuddy.dto.SignupRequest;
import com.studybuddy.exception.BadRequestException;
import com.studybuddy.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private FirebaseAuth firebaseAuth;

    @Autowired
    private Firestore firestore;

    public AuthResponse signup(SignupRequest request) {
        try {
            // Create user in Firebase Auth
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getName());

            UserRecord userRecord = firebaseAuth.createUser(createRequest);

            // Create user document in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", request.getName());
            userData.put("email", request.getEmail());
            userData.put("year", request.getYear());
            userData.put("modules", new ArrayList<String>());
            userData.put("createdAt", LocalDateTime.now().toString());
            userData.put("updatedAt", LocalDateTime.now().toString());

            firestore.collection("users").document(userRecord.getUid()).set(userData).get();

            // Generate custom token for the user
            String customToken = firebaseAuth.createCustomToken(userRecord.getUid());

            return AuthResponse.builder()
                    .token(customToken)
                    .userId(userRecord.getUid())
                    .name(request.getName())
                    .email(request.getEmail())
                    .build();
        } catch (FirebaseAuthException e) {
            throw new BadRequestException(getFirebaseErrorMessage(e));
        } catch (Exception e) {
            throw new BadRequestException("Error creating user: " + e.getMessage());
        }
    }

    public UserRecord getUserByUid(String uid) {
        try {
            return firebaseAuth.getUser(uid);
        } catch (FirebaseAuthException e) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    private String getFirebaseErrorMessage(FirebaseAuthException e) {
        String errorCode = e.getAuthErrorCode().name();
        switch (errorCode) {
            case "EMAIL_ALREADY_EXISTS":
                return "This email is already registered";
            case "INVALID_EMAIL":
                return "Invalid email address";
            case "WEAK_PASSWORD":
                return "Password is too weak";
            default:
                return e.getMessage();
        }
    }
}

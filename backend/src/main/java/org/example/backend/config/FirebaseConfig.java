package org.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_CREDENTIAL_PATH = "config/team2maldive-firebase-adminsdk-fbsvc-a8049c6c52.json";
    private static final String FIREBASE_BUCKET_NAME = "team2maldive.firebasestorage.app"; // 실제 Firebase Storage 버킷명으로 수정

    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized, skipping.");
            return;
        }

        try (InputStream serviceAccount = new FileInputStream(FIREBASE_CREDENTIAL_PATH)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(FIREBASE_BUCKET_NAME)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully from local file.");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase from local file", e);
            throw e;
        }
    }
}

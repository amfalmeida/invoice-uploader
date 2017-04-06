package com.aalmeida.invoice.uploader;

import com.aalmeida.invoice.uploader.tasks.StorageTask;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleConfiguration {

    @Value("${google.app.name}")
    private String googleAppName;
    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Bean
    @Autowired
    public StorageTask storageTask(final Drive drive) {
        return new StorageTask(drive);
    }

    @Bean
    public GoogleClientSecrets getCredentialSecrets() {
        final GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        final GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(googleClientId);
        details.setClientSecret(googleClientSecret);
        clientSecrets.setInstalled(details);
        return clientSecrets;
    }

    @Bean
    @Autowired
    public Credential getCredential(final HttpTransport httpTransport, final JsonFactory jsonFactory,
                                    final GoogleClientSecrets clientSecrets,
                                    final AbstractDataStoreFactory dataStoreFactory) throws IOException {
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                clientSecrets, Collections.singleton(DriveScopes.DRIVE))
            .setAccessType("offline").setApprovalPrompt("force")
            .setDataStoreFactory(dataStoreFactory)
            .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    public AbstractDataStoreFactory getDataStoreFactory() throws IOException {
        return new FileDataStoreFactory(new File(".store/invoice-uploader"));
    }

    @Bean
    public HttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    @Autowired
    public Drive getDrive(final HttpTransport httpTransport, final JsonFactory jsonFactory,
                          final Credential credential) {
        return new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(googleAppName).build();
    }
}

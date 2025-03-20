package com.mzwierzchowski.trading_app.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuth2Service {

  @Value("${google.oauth2.clientId}")
  private String clientId;

  @Value("${google.oauth2.clientSecret}")
  private String clientSecret;

  @Value("${google.oauth2.refreshToken}")
  private String refreshToken;

  public Credential getCredentials() throws IOException {
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    GoogleTokenResponse tokenResponse =
        new GoogleRefreshTokenRequest(
                httpTransport, jsonFactory, refreshToken, clientId, clientSecret)
            .execute();

    return new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(jsonFactory)
        .setClientSecrets(clientId, clientSecret)
        .build()
        .setAccessToken(tokenResponse.getAccessToken())
        .setRefreshToken(refreshToken);
  }
}

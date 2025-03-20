package com.mzwierzchowski.trading_app.service;

import com.google.api.client.auth.oauth2.Credential;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class OAuth2Authenticator extends Authenticator {

  private final Credential credential;

  public OAuth2Authenticator(Credential credential) {
    this.credential = credential;
  }

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication("", credential.getAccessToken());
  }
}

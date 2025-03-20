package com.mzwierzchowski.trading_app.service;

import com.google.api.client.auth.oauth2.Credential;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Log4j2
@Service
public class EmailService {

  private final TemplateEngine templateEngine;
  private final GoogleOAuth2Service googleOAuth2Service;

  public EmailService(TemplateEngine templateEngine, GoogleOAuth2Service googleOAuth2Service) {
    this.templateEngine = templateEngine;
    this.googleOAuth2Service = googleOAuth2Service;
  }

  public void sendPriceNotification(String to, String subject, Map<String, Object> model)
      throws Exception {
    String email = "pricetrakcer.alerts@gmail.com";

    Credential credential = googleOAuth2Service.getCredentials();
    if (!credential.refreshToken()) {
      log.error("Failed to refresh access token for email: {}", email);
      throw new RuntimeException("Failed to refresh access token");
    }

    String accessToken = credential.getAccessToken();
    log.info("Successfully refreshed access token for email: {}", email);

    Properties props = new Properties();
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
    props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    log.info("SMTP properties set, preparing email session");

    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, accessToken);
              }
            });

    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(email));
      message.setSubject(subject);
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setHeader("X-Entity-ID", UUID.randomUUID().toString());

      Context context = new Context();
      context.setVariables(model);
      String htmlContent = templateEngine.process("email-template", context);
      message.setContent(htmlContent, "text/html; charset=utf-8");

      Transport.send(message);
      log.info("Email successfully sent to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send email to: {}", to, e);
      throw e;
    }
  }
}

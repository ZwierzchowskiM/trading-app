package com.mzwierzchowski.trading_app.service;

import com.google.api.client.auth.oauth2.Credential;
import com.mzwierzchowski.trading_app.model.TradePosition;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
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

  public void sendTradeNotification(String to, String type, TradePosition position) throws Exception {
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

    Session session = Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(email, accessToken);
      }
    });

    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(email));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

      String htmlContent;

      String messageId = "<" + UUID.randomUUID().toString() + "trading-app.com>";
      message.setHeader("Message-ID", messageId);

      if ("BUY".equals(type)) {

        message.setSubject("Nowa transakcja: BUY ---" + UUID.randomUUID());
        htmlContent = "<h2>Nowa transakcja: BUY</h2>" +
                "<p><b>Cena zakupu:</b> " + position.getOpenPrice() + " USDT</p>" +
                "<p><b>Ilość:</b> " + position.getQuantity() + " BTC</p>" +
                "<p><b>Data otwarcia:</b> " + position.getOpenDate() + "</p>";
        message.setContent(htmlContent, "text/html; charset=utf-8");
      } else {
        message.setSubject("Nowa transakcja: SELL ---" + UUID.randomUUID());
        htmlContent = "<h2>Nowa transakcja: SELL</h2>" +
                "<p><b>Data otwarcia:</b> " + position.getOpenDate() +
                "<p><b>Data zamknięcia:</b> " + position.getCloseDate() +
                "<p><b>Ilość:</b> " + position.getQuantity() + " BTC</p>" +
                "<p><b>Cena zakupu:</b> " + position.getOpenPrice() + " USDT</p>" +
                "<p><b>Cena sprzedaży:</b> " + position.getClosePrice() + " USDT</p>" +
                "<p><b>Rezultat:</b> " + position.getResult() + " USDT</p>";
        message.setContent(htmlContent, "text/html; charset=utf-8");
      }


      Transport.send(message);
      log.info("Trade notification sent to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send trade notification to: {}", to, e);
      throw e;
    }
  }

}

package com.mzwierzchowski.trading_app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ScheduledService {

  private final RestTemplate restTemplate = new RestTemplate();



  // Uruchamiane co 5 minut: "0 */5 * * * *"
  @Scheduled(cron = "0 */4 * * * *")
  public void checkAppLive() {
    String url = "https://trading-app-1-ly5x.onrender.com/api/trading/status";
    try {
      String response = restTemplate.getForObject(url, String.class);
      //System.out.println("Scheduled health check response: " + response);
    } catch (Exception e) {
      System.err.println("Scheduled health check failed: " + e.getMessage());
    }
  }

  @Scheduled(cron = "0 */5 * * * *")
  public void checkPrice() {
    String url = "https://trading-app-1-ly5x.onrender.com/api/trading/check";
    try {
      String response = restTemplate.getForObject(url, String.class);
      //System.out.println("Scheduled health check response: " + response);
    } catch (Exception e) {
      System.err.println("Scheduled health check failed: " + e.getMessage());
    }
  }


}

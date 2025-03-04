package com.mzwierzchowski.trading_app.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.*;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M1;

@Service
public class XtbService {

  @Value("${XTB_USERID}")
  private String userId;
  @Value("${XTB_PASS}")
  private String password;

  private final RestTemplate restTemplate = new RestTemplate();

  public SyncAPIConnector connect() {

    try {
      SyncAPIConnector connector = new SyncAPIConnector(ServerData.ServerEnum.DEMO);
      LoginResponse loginResponse = null;
      loginResponse = APICommandFactory.executeLoginCommand(connector, new Credentials(userId, password));
      if (loginResponse.getStatus() == true) {
        System.out.println("User logged in");
      }
      return connector;
    } catch (APICommandConstructionException
        | APICommunicationException
        | APIReplyParseException
        | APIErrorResponse
        | IOException e) {
      throw new RuntimeException(e);
    }
  }

  ChartResponse getHistoricalData (SyncAPIConnector connector, String symbol){

    long endTime = Instant.now().toEpochMilli();
    long startTime = Instant.now().minus(4, ChronoUnit.HOURS).toEpochMilli();

    try {
      ChartResponse chartResponse = APICommandFactory.executeChartRangeCommand(connector, symbol, PERIOD_M1,startTime,endTime,0L);
      return chartResponse;
    } catch (APICommandConstructionException | APICommunicationException | APIReplyParseException e) {
      throw new RuntimeException(e);
    } catch (APIErrorResponse e) {
      System.out.println("błąd pobierania danych historycnych");
      return null;
    }
  }

  double getSymbol (SyncAPIConnector connector, String symbol) throws APIErrorResponse, APICommunicationException, APIReplyParseException, APICommandConstructionException {

    SymbolResponse symbolResponse = APICommandFactory.executeSymbolCommand(connector, symbol);
    System.out.println("Czas: " + LocalDateTime.now());
    System.out.println("Cena ASK: " + symbolResponse.getSymbol().getAsk());
    return symbolResponse.getSymbol().getAsk();
  }



  // Uruchamiane co 5 minut: "0 */5 * * * *"
  @Scheduled(cron = "0 */5 * * * *")
  public void checkAppLive() {
    String url = "https://trading-app-o7wc.onrender.com/api/trading/status";
    try {
      String response = restTemplate.getForObject(url, String.class);
      //System.out.println("Scheduled health check response: " + response);
    } catch (Exception e) {
      System.err.println("Scheduled health check failed: " + e.getMessage());
    }
  }

  @Scheduled(cron = "0 */2 * * * *")
  public void checkPrice() {
    String url = "https://trading-app-o7wc.onrender.com/api/trading/check";
    try {
      String response = restTemplate.getForObject(url, String.class);
      //System.out.println("Scheduled health check response: " + response);
    } catch (Exception e) {
      System.err.println("Scheduled health check failed: " + e.getMessage());
    }
  }


}

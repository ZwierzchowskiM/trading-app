package com.mzwierzchowski.trading_app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzwierzchowski.trading_app.model.Message;
import com.mzwierzchowski.trading_app.model.Sentiment;
import com.mzwierzchowski.trading_app.model.StockTwitsResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockTwitsService {

  private static final String BASE_URL = "https://api.stocktwits.com/api/2/streams/symbol/BTC.X.json";
  private static final int LIMIT = 30; // Maksymalna liczba wiadomości na żądanie

  public StockTwitsService() {}

  public String getStockInfo() throws IOException {

    List<Message> allMessages = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    long maxId = Long.MAX_VALUE; // Początkowa wartość maxId
    boolean moreMessages = true;

    try {
      while (moreMessages) {
        // Tworzenie URL z parametrami paginacji
        String url = BASE_URL + "?limit=" + LIMIT + "&max=" + maxId;
        StockTwitsResponse response = mapper.readValue(new URL(url), StockTwitsResponse.class);
        List<Message> messages = response.getMessages();

        if (messages.isEmpty()) {
          moreMessages = false; // Brak kolejnych wiadomości do pobrania
        } else {
          allMessages.addAll(messages);
          // Ustawienie maxId na ID ostatniej pobranej wiadomości minus 1
          maxId = messages.get(messages.size() - 1).getId() - 1;
        }

        // Warunek zakończenia pętli (np. pobranie 100 wiadomości)
        if (allMessages.size() >= 100) {
          moreMessages = false;
        }
      }

      int totalPosts = allMessages.size();
      int bullishCount = 0;
      int bearishCount = 0;
      int nullSentimentCount = 0;

      for (Message msg : allMessages) {
         //System.out.println(msg.getCreated_at());
        Sentiment sentiment = (msg.getEntities() != null) ? msg.getEntities().getSentiment() : null;
        if (sentiment == null || sentiment.getBasic() == null) {
          nullSentimentCount++;
        } else if ("Bullish".equalsIgnoreCase(sentiment.getBasic())) {
          bullishCount++;
        } else if ("Bearish".equalsIgnoreCase(sentiment.getBasic())) {
          bearishCount++;
        } else {
          // Jeśli wartość nie pasuje do Bullish ani Bearish, traktujemy ją jako null
          nullSentimentCount++;
        }
      }

      System.out.println("Total posts: " + totalPosts);
      System.out.println("Bullish posts: " + bullishCount);
      System.out.println("Bearish posts: " + bearishCount);
      System.out.println("Posts with no sentiment: " + nullSentimentCount);

      if (bullishCount >= bearishCount) {
        return "bullish";
      }

      if (bullishCount < bearishCount) {
        return "bearish";
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
//      System.out.println("Start");
//    // URL do API StockTwits dla symbolu SPY
//    String url = "https://api.stocktwits.com/api/2/streams/symbol/SPY.json";
//
//    // Parsowanie JSON-a
//    ObjectMapper mapper = new ObjectMapper();
//
//        StockTwitsResponse response = mapper.readValue(new URL(url), StockTwitsResponse.class);
//        List<Message> messages = response.getMessages();
//
//        int totalPosts = messages.size();
//        int bullishCount = 0;
//        int bearishCount = 0;
//        int nullSentimentCount = 0;
//
//        for (Message msg : messages) {
//      System.out.println(msg.getCreated_at());
//            Sentiment sentiment = (msg.getEntities() != null) ? msg.getEntities().getSentiment() :
// null;
//            if (sentiment == null || sentiment.getBasic() == null) {
//                nullSentimentCount++;
//            } else if ("Bullish".equalsIgnoreCase(sentiment.getBasic())) {
//                bullishCount++;
//            } else if ("Bearish".equalsIgnoreCase(sentiment.getBasic())) {
//                bearishCount++;
//            } else {
//                // Jeśli wartość nie pasuje do Bullish ani Bearish, traktujemy ją jako null
//                nullSentimentCount++;
//            }
//        }
//
//        System.out.println("Total posts: " + totalPosts);
//        System.out.println("Bullish posts: " + bullishCount);
//        System.out.println("Bearish posts: " + bearishCount);
//        System.out.println("Posts with no sentiment: " + nullSentimentCount);
//
// }

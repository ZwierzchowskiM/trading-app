package com.mzwierzchowski.trading_app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzwierzchowski.trading_app.model.StockTwits.Message;
import com.mzwierzchowski.trading_app.model.StockTwits.Sentiment;
import com.mzwierzchowski.trading_app.model.StockTwits.StockTwitsResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.mzwierzchowski.trading_app.model.StockTwits.StockTwitsResult;
import org.springframework.stereotype.Service;

@Service
public class StockTwitsService {

  private static final String BASE_URL = "https://api.stocktwits.com/api/2/streams/symbol/BTC.X.json";
  private static final int LIMIT = 30; // Maksymalna liczba wiadomości na żądanie

  // Zmienne do przechowywania poprzednich wyników
  private int previousBullishCount = -1;
  private int previousBearishCount = -1;

  public StockTwitsService() {}

  public StockTwitsResult getStockSentiment() throws IOException {
    double bullishPercentage = 0;
    double bearishPercentage = 0;
    int bullishDiff =0;
    int bearishDiff =0;

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
        if (allMessages.size() >= 200) {
          moreMessages = false;
        }
      }

      int totalPosts = allMessages.size();
      int bullishCount = 0;
      int bearishCount = 0;
      int nullSentimentCount = 0;

      for (Message msg : allMessages) {
        Sentiment sentiment = (msg.getEntities() != null) ? msg.getEntities().getSentiment() : null;
        //System.out.println(msg.getCreated_at());
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

      // Obliczanie procentowego udziału bullish i bearish (ignorujemy posty bez sentymentu)
      int effectivePosts = bullishCount + bearishCount;
      if (effectivePosts > 0) {
        bullishPercentage = (bullishCount / (double) effectivePosts) * 100;
        bearishPercentage = (bearishCount / (double) effectivePosts) * 100;
        System.out.printf("Bullish: %.2f%%, Bearish: %.2f%%%n", bullishPercentage, bearishPercentage);
      } else {
        System.out.println("Brak postów z określonym sentymentem do obliczenia procentów.");
      }

      // Porównanie z poprzednim cyklem
      if (previousBullishCount != -1 && previousBearishCount != -1) {
        bullishDiff = bullishCount - previousBullishCount;
        bearishDiff = bearishCount - previousBearishCount;
        System.out.println("Difference from previous cycle:");
        System.out.println("Bullish diff: " + bullishDiff);
        System.out.println("Bearish diff: " + bearishDiff);
      } else {
        System.out.println("No previous data available.");
      }

      // Aktualizacja poprzednich wartości na potrzeby kolejnego cyklu
      previousBullishCount = bullishCount;
      previousBearishCount = bearishCount;

      // Ustalanie sygnału na podstawie aktualnych danych (możesz dostosować logikę)
      return new StockTwitsResult(bullishPercentage, bearishPercentage, bullishDiff, bearishDiff);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}

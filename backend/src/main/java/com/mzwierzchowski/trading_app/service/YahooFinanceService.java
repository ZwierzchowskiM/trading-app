package com.mzwierzchowski.trading_app.service;

import com.github.hitzseb.YahooFinanceAPI;
import com.github.hitzseb.model.Chart;
import com.github.hitzseb.model.Indicators;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

@Service
public class YahooFinanceService {

  static BarSeries getDataFromYahoo() {

    // Pobranie danych dla VBTC.DE (interwał 5 minut, 1 dzień wstecz)
    Chart chart = null;
    try {
      chart = YahooFinanceAPI.getChartByRange("BTC-USD", "1m", "1d", "Europe/Warsaw");
      Indicators indicators = chart.getIndicators();

      List<Double> opens = indicators.getQuote().get(0).getOpen();
      List<Double> highs = indicators.getQuote().get(0).getHigh();
      List<Double> lows = indicators.getQuote().get(0).getLow();
      List<Double> closes = indicators.getQuote().get(0).getClose();
      List<Long> volumes = indicators.getQuote().get(0).getVolume();
      List<String> timestamps = chart.getTimestamp();

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

      // Inicjalizacja serii
      BarSeries series = new BaseBarSeries("VBTC.DE Series");
      series.setMaximumBarCount(100);

      for (int i = 0; i < opens.size(); i++) {
        // Odczytywanie rzeczywistego timestampu
        LocalDateTime localDateTime = LocalDateTime.parse(timestamps.get(i), formatter);
        ZonedDateTime barTime = localDateTime.atZone(ZoneId.systemDefault());

        if (closes.get(i) > 0) {
          series.addBar(
              barTime, opens.get(i), highs.get(i), lows.get(i), closes.get(i), volumes.get(i));
        }
      }
      return series;

    } catch (IOException | InterruptedException | NullPointerException e) {
      System.out.println("błąd przy pobieraniu danych");
      BarSeries series = new BaseBarSeries("VBTC.DE Series");
      series.setMaximumBarCount(100);
      return series;
    }
  }
}

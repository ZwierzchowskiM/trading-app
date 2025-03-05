package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.Candle;
import java.time.*;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

@Service
public class CandleConverter {

  public BarSeries convert(List<Candle> candles) {
    BarSeries series = new BaseBarSeries("BTC/USDT");
    for (Candle candle : candles) {

      //System.out.println(candle.toString());

      ZonedDateTime endTime =
          ZonedDateTime.ofInstant(
              Instant.ofEpochMilli(candle.getOpenTime()), ZoneId.systemDefault());

      // Jeśli pola są przechowywane jako BigDecimal, konwertujemy na double
      double open = candle.getOpen().doubleValue();
      double high = candle.getHigh().doubleValue();
      double low = candle.getLow().doubleValue();
      double close = candle.getClose().doubleValue();
      double volume = candle.getVolume().doubleValue();

      // Dodajemy bar do serii
      series.addBar(endTime, open, high, low, close, volume);
    }
    return series;
  }
}

package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.Candle;
import java.time.*;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;

@Service
public class BinanceCandleConverter {

  public BarSeries convert(List<Candle> candles) {
    BarSeries series = new BaseBarSeries("BTC/USDT");
    for (Candle candle : candles) {


      ZonedDateTime endTime =
          ZonedDateTime.ofInstant(
              Instant.ofEpochMilli(candle.getOpenTime()), ZoneId.systemDefault());

      double open = candle.getOpen().doubleValue();
      double high = candle.getHigh().doubleValue();
      double low = candle.getLow().doubleValue();
      double close = candle.getClose().doubleValue();
      double volume = candle.getVolume().doubleValue();

      series.addBar(endTime, open, high, low, close, volume);
    }
    return series;
  }
}

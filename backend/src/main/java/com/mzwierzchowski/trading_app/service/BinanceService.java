package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.Candle;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.util.List;

@Service
public class BinanceService {

  BinanceCandleParser binanceCandleParser;
  CandleConverter candleConverter;

  public BinanceService(BinanceCandleParser binanceCandleParser, CandleConverter candleConverter) {
    this.binanceCandleParser = binanceCandleParser;
    this.candleConverter = candleConverter;
  }

  public BarSeries getHistoricalBarSeries() throws IOException {

    List<Candle> candleList = binanceCandleParser.parseCandles();
    candleList.remove(candleList.size() - 1);
    BarSeries series = candleConverter.convert(candleList);

    return series;
  }

  void printSeries(BarSeries series) {
    System.out.println("Number of bars: " + series.getBarCount());

    for (int i = 0; i < series.getBarCount(); i++) {

      System.out.println(
          "bar: "
              + i + "\n" + "\tVolume: "
              + series.getBar(i).getVolume()
              + "\n" + "\tOpen price: "
              + series.getBar(i).getOpenPrice()
              + "\n" + "\tClose price: "
              + series.getBar(i).getClosePrice()
              + "\n" + "\tTime: "
              + series.getBar(i).getEndTime()
              + "\n" + "\tVolumen: "
              + series.getBar(i).getVolume()
              + "\n" + "\tLow: "
              + series.getBar(i).getLowPrice()
              + "\n" + "\tHigh: "
              + series.getBar(i).getHighPrice());
    }
  }
}

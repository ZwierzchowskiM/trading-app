package com.mzwierzchowski.trading_app.service;


import com.mzwierzchowski.trading_app.model.StockTwits.StockTwitsResult;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;

@Service
public class RealTimeStrategyRunner {

  private BarSeries series;
  private double lastPrice = 0;
  private double newPrice;
  private StockTwitsResult stockTwitsResult;
  private StockTwitsService stockTwitsService;
  private final StrategyEvaluatorService strategyEvaluatorService;
  private BinanceService binanceService;

  public RealTimeStrategyRunner(
      StockTwitsService stockTwitsService,
      StrategyEvaluatorService strategyEvaluatorService,
      BinanceService binanceService) {
    this.stockTwitsService = stockTwitsService;
    this.strategyEvaluatorService = strategyEvaluatorService;
    this.binanceService = binanceService;
  }

  public void getSinglePrice() {

    try {
      series = binanceService.getHistoricalBarSeries();
      //stockTwitsResult = stockTwitsService.getStockSentiment();
      newPrice = series.getLastBar().getClosePrice().doubleValue();
      strategyEvaluatorService.evaluate(series);
      System.out.println("--------------------------");

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void compareResults() {
    double diff = newPrice - lastPrice;
    lastPrice = newPrice;
    System.out.println("różnica kursu względem ostatniej wartości: " + diff);
  }

  void printBar(Bar bar){
    System.out.println(
            "bar: " +
                     "\n" + "\tVolume: "
                    + bar.getVolume()
                    + "\n" + "\tOpen price: "
                    + bar.getOpenPrice()
                    + "\n" + "\tClose price: "
                    + bar.getClosePrice()
                    + "\n" + "\tTime: "
                    + bar.getEndTime()
                    + "\n" + "\tVolumen: "
                    + bar.getVolume()
                    + "\n" + "\tLow: "
                    + bar.getLowPrice()
                    + "\n" + "\tHigh: "
                    + bar.getHighPrice());
  }

}

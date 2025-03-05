package com.mzwierzchowski.trading_app.service;


import com.mzwierzchowski.trading_app.model.StockTwits.StockTwitsResult;
import java.io.IOException;
import javax.swing.*;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import pro.xstore.api.sync.SyncAPIConnector;

@Service
public class RealTimeStrategyRunner {

  private BarSeries series;
  private SyncAPIConnector connector;
  private String symbol = "BITCOIN";

  private double lastPrice = 0;
  private double newPrice;
  private StockTwitsResult stockTwitsResult;

  private StockTwitsService stockTwitsService;
  private final StrategyEvaluator strategyEvaluator;
  private BinanceService binanceService;

  public RealTimeStrategyRunner(
      StockTwitsService stockTwitsService,
      StrategyEvaluator strategyEvaluator,
      BinanceService binanceService) {
    this.stockTwitsService = stockTwitsService;
    this.strategyEvaluator = strategyEvaluator;
    this.binanceService = binanceService;
  }

  public void getSinglePrice() {

    try {
      series = binanceService.getHistoricalBarSeries();
      stockTwitsResult = stockTwitsService.getStockSentiment();
      printBar(series.getLastBar());
      System.out.println("new price: " + series.getLastBar().getClosePrice());
      compareResults();
      strategyEvaluator.evaluate(series);
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

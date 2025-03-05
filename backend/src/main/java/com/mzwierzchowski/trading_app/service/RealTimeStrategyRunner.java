package com.mzwierzchowski.trading_app.service;

import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M1;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.mzwierzchowski.trading_app.model.StockTwits.StockTwitsResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.message.response.ChartResponse;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.SyncAPIConnector;

import javax.swing.*;

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

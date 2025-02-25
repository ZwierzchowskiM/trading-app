package com.mzwierzchowski.trading_app.service;

import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M1;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

@Service
public class RealTimeStrategyRunner {

  private BarSeries series;
  private SyncAPIConnector connector;
  private volatile boolean newCandle = false;
  private volatile SCandleRecord newCandleRecord = null;
  private volatile boolean running = true;
  private TradingRecord tradingRecord = new BaseTradingRecord();
  private ChartResponse chartHistoricalData;
  String symbol = "BITCOIN";

  private XtbService xtbService;
  private final HistoricalDataConverter historicalDataConverter;
  private final StrategyEvaluator strategyEvaluator;

  public RealTimeStrategyRunner(
      XtbService xtbService,
      HistoricalDataConverter historicalDataConverter,
      StrategyEvaluator strategyEvaluator) {
    this.xtbService = xtbService;
    this.historicalDataConverter = historicalDataConverter;
    this.strategyEvaluator = strategyEvaluator;
  }

  @Async
  public void startLoop() {

    connector = xtbService.connect();
    chartHistoricalData = xtbService.getHistoricalData(connector, symbol);
    series = historicalDataConverter.convertToSeries(chartHistoricalData);
    printSeries(series);
    subscribeCandles(connector);

    while (running) {

      if (newCandle) {
        updateBarSeries(newCandleRecord);
        strategyEvaluator.evaluate(series, tradingRecord);
        newCandle = false;
      }
    }
    unsubscribeCandles(connector);
    connectorClose(connector);
    System.out.println("stratategia zatrzymana");
  }

  public void stop() {
    running = false;
  }

  private void updateBarSeries(SCandleRecord candleRecord) {
    if (candleRecord == null) {
      System.err.println("Received null candleRecord, skipping update.");
      return;
    }
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("MMM dd, yyyy, h:mm:ss a", Locale.ENGLISH);
    LocalDateTime localDateTime = LocalDateTime.parse(candleRecord.getCtmString(), formatter);
    ZonedDateTime barTime = localDateTime.atZone(ZoneId.systemDefault());

    double open = candleRecord.getOpen();
    double high = candleRecord.getHigh();
    double low = candleRecord.getLow();
    double close = candleRecord.getClose();
    double volume = candleRecord.getVol();

    series.addBar(barTime, open, high, low, close, volume);
    newCandleRecord = null;
  }

  public void subscribeCandles(SyncAPIConnector connector) {
    StreamingListener sl =
        new StreamingListener() {

          public void receiveCandleRecord(SCandleRecord candleRecord) {
            System.out.println("Stream candle record: " + candleRecord);
            newCandle = true;
            newCandleRecord = candleRecord;
          }
        };

    try {
      connector.connectStream(sl);
      chartRangeRequiredCommand(connector, symbol);
      connector.subscribeCandle(symbol);
      System.out.println("symbol subscribed");
    } catch (IOException | APICommunicationException e) {
      throw new RuntimeException(e);
    }
  }

  public void unsubscribeCandles(SyncAPIConnector connector) {
    try {
      connector.unsubscribeCandle(symbol);
    } catch (APICommunicationException e) {
      throw new RuntimeException(e);
    }
  }

  public void connectorClose(SyncAPIConnector connector) {
    try {
      connector.close();
    } catch (APICommunicationException e) {
      System.out.println("close connector error");
    }
  }

  void chartRangeRequiredCommand(SyncAPIConnector connector, String symbol) {
    try {
      APICommandFactory.executeChartLastCommand(connector, symbol, PERIOD_M1, 0L);
    } catch (APICommandConstructionException
        | APICommunicationException
        | APIReplyParseException e) {
      throw new RuntimeException(e);
    } catch (APIErrorResponse e) {
      System.out.println("chart range - błąd API");
    }
  }

  void printSeries(BarSeries series) {

    int countBar = series.getBarData().size();
    for (int i = 0; i < countBar; i++) {
      System.out.println(series.getBarData().get(i));
    }
  }
}

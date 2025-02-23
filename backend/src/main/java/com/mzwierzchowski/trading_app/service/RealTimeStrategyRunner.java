package com.mzwierzchowski.trading_app.service;

import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M1;

import com.mzwierzchowski.trading_app.strategy.BullishGChannelRule;
import com.mzwierzchowski.trading_app.strategy.GChannel;
import com.mzwierzchowski.trading_app.strategy.GChannelLowerIndicator;
import com.mzwierzchowski.trading_app.strategy.GChannelUpperIndicator;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.rules.AndRule;
import org.ta4j.core.rules.NotRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.RateInfoRecord;
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

  public RealTimeStrategyRunner(XtbService xtbService) {
    this.xtbService = xtbService;
  }

  @Async
  public void start() {

    connector = xtbService.connect();
    chartHistoricalData = xtbService.getHistoricalData(connector, symbol);
    series = covertChartReposnsetoSeries(chartHistoricalData);
    printSeries(series);
    subscribeCandles(connector);

    while (running) {

      if (newCandle) {
        updateBarSeries(newCandleRecord);
        printSeries(series);
        checkStrategy();
        newCandle = false;
      }
    }
    System.out.println("stratategia zatrzymana");
  }

  private BarSeries covertChartReposnsetoSeries(ChartResponse chartHistoricalData) {

    BarSeries newSeries = new BaseBarSeries();
    List<RateInfoRecord> rateInfoRecords = chartHistoricalData.getRateInfos();
    int digits = chartHistoricalData.getDigits();

    for (int i = 0; i < rateInfoRecords.size(); i++) {

      RateInfoRecord rateInfoRecord = rateInfoRecords.get(i);
      long ctmMillis = rateInfoRecord.getCtm();
      ZonedDateTime barTime = Instant.ofEpochMilli(ctmMillis).atZone(ZoneId.systemDefault());
      // Przeliczenie cen:
      // Cena otwarcia już jest pomnożona, więc dzielimy przez 10^digits
      double open = rateInfoRecord.getOpen() / Math.pow(10, digits);
      // Pozostałe wartości to przesunięcia od ceny otwarcia
      double close = open + (rateInfoRecord.getClose() / Math.pow(10, digits));
      double high = open + (rateInfoRecord.getHigh() / Math.pow(10, digits));
      double low = open + (rateInfoRecord.getLow() / Math.pow(10, digits));
      double volume = rateInfoRecord.getVol();

      newSeries.addBar(barTime, open, high, low, close, volume);
    }

    return newSeries;
  }

  public void stop() {
    running = false;
  }

  private void updateBarSeries(SCandleRecord candleRecord) {
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

  private void checkStrategy() {
    int endIndex = series.getEndIndex();

    if (endIndex < 1) {
      return;
    }
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    EMAIndicator ema = new EMAIndicator(closePrice, 200);
    GChannel gChannel = new GChannel(closePrice, 5, series);
    GChannelUpperIndicator gChannelUpper = new GChannelUpperIndicator(gChannel);
    GChannelLowerIndicator gChannelLower = new GChannelLowerIndicator(gChannel);

    BullishGChannelRule bullishRule =
        new BullishGChannelRule(series, gChannelUpper, gChannelLower, closePrice);

    Rule buyRule = new AndRule(bullishRule, new UnderIndicatorRule(closePrice, ema));
    Rule sellRule = new AndRule(new NotRule(bullishRule), new OverIndicatorRule(closePrice, ema));

    Strategy strategy = new BaseStrategy(buyRule, sellRule);

    boolean buySignal = buyRule.isSatisfied(endIndex, tradingRecord);
    boolean sellSignal = sellRule.isSatisfied(endIndex, tradingRecord);

    if (strategy.shouldEnter(endIndex) && tradingRecord.isClosed()) {

      System.out.println("Strategy should ENTER on " + endIndex);
      boolean entered =
          tradingRecord.enter(endIndex, series.getLastBar().getClosePrice(), DecimalNum.valueOf(1));
      if (entered) {
        Trade entry = tradingRecord.getLastEntry();
        System.out.println(
            "Entered: "
                + series.getLastBar().getEndTime()
                + " (price="
                + entry.getNetPrice().doubleValue()
                + ", amount="
                + entry.getAmount().doubleValue()
                + ")");
      }
    } else if (strategy.shouldExit(endIndex) && !tradingRecord.isClosed()) {

      System.out.println("Strategy should EXIT on " + endIndex);
      boolean exited =
          tradingRecord.exit(endIndex, series.getLastBar().getClosePrice(), DecimalNum.valueOf(1));
      if (exited) {
        Trade exit = tradingRecord.getLastExit();
        System.out.println(
            "Exited: "
                + series.getLastBar().getEndTime()
                + " (price="
                + exit.getNetPrice().doubleValue()
                + ", amount="
                + exit.getAmount().doubleValue()
                + ")");
      }
    }
  }

  public void subscribeCandles(SyncAPIConnector connector) {
    StreamingListener sl =
        new StreamingListener() {

          public void receiveCandleRecord(SCandleRecord candleRecord) {
            System.out.println("Stream candle record: " + candleRecord);
            newCandle = true;
            newCandleRecord = candleRecord;
            clearScreen();
          }
        };

    String symbol = "BITCOIN";

    try {
      connector.connectStream(sl);
      testCommand(connector, symbol);
      connector.subscribeCandle(symbol);
      System.out.println("symbol subscribed");
    } catch (IOException | APICommunicationException e) {
      throw new RuntimeException(e);
    }
  }

  void testCommand(SyncAPIConnector connector, String symbol) {
    try {
      APICommandFactory.executeChartLastCommand(connector, symbol, PERIOD_M1, 0L);
    } catch (APICommandConstructionException
        | APICommunicationException
        | APIReplyParseException e) {
      throw new RuntimeException(e);
    } catch (APIErrorResponse e) {
      System.out.println("błąd API");
    }
  }

  void printSeries(BarSeries series) {

    int countBar = series.getBarData().size();

    for (int i = 0; i < countBar; i++) {
      System.out.println(series.getBarData().get(i));
    }
  }

  public static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
}

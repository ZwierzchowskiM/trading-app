package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.strategy.BullishGChannelRule;
import com.mzwierzchowski.trading_app.strategy.GChannel;
import com.mzwierzchowski.trading_app.strategy.GChannelLowerIndicator;
import com.mzwierzchowski.trading_app.strategy.GChannelUpperIndicator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
import pro.xstore.api.message.records.SCandleRecord;
import pro.xstore.api.message.response.APIErrorResponse;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.SyncAPIConnector;

import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M1;
import static pro.xstore.api.message.codes.PERIOD_CODE.PERIOD_M5;

@Service
public class RealTimeStrategyRunner {

  private BarSeries series;
  private SyncAPIConnector connector;
  private volatile boolean newCandle = false;
  private volatile SCandleRecord newCandleRecord = null;
  private XtbService xtbService;
  private TradingRecord tradingRecord = new BaseTradingRecord();

  private volatile boolean running = true; // Flaga do zatrzymywania pętli


  public RealTimeStrategyRunner(XtbService xtbService) {
    this.xtbService = xtbService;
  }

  public void start() {

    series = YahooFinanceService.getDataFromYahoo();
    connector = xtbService.connect();
    subscribeCandles(connector);
    // printSeries(series);

    while (running) {

      //System.out.println("wątek start");
      if (newCandle) {
        updateBarSeries(newCandleRecord);
        // printSeries(series);
        checkStrategy();
        newCandle = false;
      }
    }
  }

  //  public void start() {
  //
  //    subscribeCandles(connector);
  //    // printSeries(series);
  //    Thread loopThread = new Thread(() -> {
  //      while (running) {
  //        try {
  //          System.out.println("wątek start");
  //          if (newCandle) {
  //            updateBarSeries(newCandleRecord);
  //            // printSeries(series);
  //            checkStrategy();
  //            newCandle = false;
  //          }
  //          Thread.sleep(500);
  //        } catch (InterruptedException e) {
  //          Thread.currentThread().interrupt();
  //        }
  //      }
  //    });
  //    loopThread.setDaemon(true);
  //    loopThread.start();
  //  }

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
       APICommandFactory.executeChartLastCommand(connector, symbol, PERIOD_M1, 0L);
      connector.subscribeCandle(symbol);
      System.out.println("symbol subscribed");
    }
    catch (IOException | APICommunicationException e) {
      throw new RuntimeException(e);
    }
    catch (APIErrorResponse e) {
      System.out.println("błąd API");
    }
    catch (APIReplyParseException e) {
        throw new RuntimeException(e);
    }
    catch (APICommandConstructionException e) {
        throw new RuntimeException(e);
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

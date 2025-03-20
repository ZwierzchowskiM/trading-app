package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.TradePosition;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

@Service
@Getter
@Setter
public class StrategyEvaluator {

  private Num totalBalance;
  private TradingRecord tradingRecord = new BaseTradingRecord();
  private BinanceClient binanceClient;
  private TradePosition position;

  private String symbol = "BTCUSDC";
  private double quantity;

  private final EmailService emailService;
  private final String notificationEmail = "app.mzwierzchowski@gmail.com";

  public StrategyEvaluator(BinanceClient binanceClient, EmailService emailService) {
    this.binanceClient = binanceClient;
      this.emailService = emailService;
      this.totalBalance = null;
    position = new TradePosition();
    quantity = 0.0001;
  }

  public void evaluate(BarSeries series) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

    EMAIndicator ema10 = new EMAIndicator(closePrice, 10);
    EMAIndicator ema50 = new EMAIndicator(closePrice, 50);
    EMAIndicator ema70 = new EMAIndicator(closePrice, 70);

    RSIIndicator rsi = new RSIIndicator(closePrice, 16);

    int lastIndex = series.getEndIndex();
    System.out.println("EMA(10): " + ema10.getValue(lastIndex));
    System.out.println("EMA(50): " + ema50.getValue(lastIndex));
    System.out.println("EMA(70): " + ema70.getValue(lastIndex));
    System.out.println("Close: " + closePrice.getValue(lastIndex));

    Rule entryRule =
        new CrossedUpIndicatorRule(ema10, ema50) // EMA(10) przecina EMA(50) w górę
            .and(new OverIndicatorRule(closePrice, ema70));

    Rule exitRule =
        new CrossedDownIndicatorRule(ema10, ema50) // EMA(10) przecina EMA(50) w dół
            .and(new UnderIndicatorRule(closePrice, ema70));

    Strategy strategy = new BaseStrategy(entryRule, exitRule);
    lastIndex = series.getEndIndex();
    Num currentPrice = closePrice.getValue(lastIndex);

    boolean shouldEnter = strategy.getEntryRule().isSatisfied(lastIndex, tradingRecord);
    boolean shouldExit = strategy.getExitRule().isSatisfied(lastIndex, tradingRecord);

    if (shouldEnter && !position.isOpened()) {
      String response = binanceClient.placeOrder(symbol, "BUY", "MARKET", quantity);
      System.out.println(response);
      System.out.println("Sygnał kupna aktywny! Kupuję za: " + currentPrice);
      position.setOpened(true);
      position.setOpenDate(LocalDateTime.now());
      position.setOpenPrice(currentPrice.doubleValue());
      try {
        emailService.sendTradeNotification(notificationEmail, "KUPNO", position);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    if (shouldExit && position.isOpened()) {
      String response = binanceClient.placeOrder(symbol, "SELL", "MARKET", quantity);
      System.out.println(response);
      System.out.println("Sygnał sprzedaży! Sprzedaję za: " + currentPrice);
      double result = position.getClosePrice() - position.getOpenPrice();
      System.out.println("Bilas pozycji: " + result);

      position.setOpened(false);
      position.setClosePrice(currentPrice.doubleValue());
      position.setCloseDate(LocalDateTime.now());
      position.setResult(result);

      try {
        emailService.sendTradeNotification(notificationEmail, "SPRZEDAŻ", position);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}

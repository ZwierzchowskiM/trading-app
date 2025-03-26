package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.TradePosition;
import java.time.LocalDateTime;

import com.mzwierzchowski.trading_app.repository.TradePositionRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
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
  private double quantity = 0.0001;
  private final String notificationEmail = "app.mzwierzchowski@gmail.com";

  private final EmailService emailService;
  private final TradePositionRepository tradePositionRepository;

  public StrategyEvaluator(
      BinanceClient binanceClient,
      EmailService emailService,
      TradePositionRepository tradePositionRepository) {
    this.binanceClient = binanceClient;
    this.emailService = emailService;
    this.tradePositionRepository = tradePositionRepository;
    this.totalBalance = null;
  }

  public void evaluate(BarSeries series) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

    EMAIndicator ema10 = new EMAIndicator(closePrice, 10);
    EMAIndicator ema50 = new EMAIndicator(closePrice, 50);
    EMAIndicator ema70 = new EMAIndicator(closePrice, 70);

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

    if (shouldEnter) {
      String response = binanceClient.placeOrder(symbol, "BUY", "MARKET", quantity);
      System.out.println(response);
      System.out.println("Sygnał kupna aktywny! Kupuję za: " + currentPrice);

      TradePosition newPosition = new TradePosition();
      newPosition.setOpened(true);
      newPosition.setOpenDate(LocalDateTime.now());
      newPosition.setOpenPrice(currentPrice.doubleValue());
      tradePositionRepository.save(newPosition);

      try {
        emailService.sendTradeNotification(notificationEmail, "BUY", position);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (shouldExit) {
      TradePosition openPosition =
          tradePositionRepository.findAll().stream()
              .filter(TradePosition::isOpened)
              .findFirst()
              .orElse(null);

      if (openPosition != null) {
        String response = binanceClient.placeOrder(symbol, "SELL", "MARKET", quantity);
        System.out.println(response);
        System.out.println("Sygnał sprzedaży! Sprzedaję za: " + currentPrice);

        openPosition.setOpened(false);
        openPosition.setClosePrice(currentPrice.doubleValue());
        openPosition.setCloseDate(LocalDateTime.now());
        openPosition.setResult(openPosition.getClosePrice() - openPosition.getOpenPrice());

        tradePositionRepository.save(openPosition);

        try {
          emailService.sendTradeNotification(notificationEmail, "SELL", position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}

package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.model.BitcoinPosition;
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
  private BitcoinPosition position;

  private String symbol = "BTCUSDC";
  private double quantity;

  public StrategyEvaluator(BinanceClient binanceClient) {
    this.binanceClient = binanceClient;
    this.totalBalance = null;
    position = new BitcoinPosition();
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
    //System.out.println("RSI(14): " + rsi.getValue(lastIndex));

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
      position.setOpenPrice(currentPrice.doubleValue());
    }
    if (shouldExit && position.isOpened()) {
      String response = binanceClient.placeOrder(symbol, "SELL", "MARKET", quantity);
      System.out.println(response);
      System.out.println("Sygnał sprzedaży! Sprzedaję za: " + currentPrice);
      position.setOpened(false);
      position.setClosePrice(currentPrice.doubleValue());
      double result = position.getClosePrice() - position.getOpenPrice();
      System.out.println("Bilas pozycji: " + result);
    }



    //    Num tradeAmount = series.numOf(1);
    //    if (shouldEnter && !tradingRecord.getCurrentPosition().isOpened()) {
    //      System.out.println("Sygnał kupna aktywny! Kupuję za: " + currentPrice);
    //      tradingRecord.enter(lastIndex, currentPrice, tradeAmount); // Kupujemy 1 jednostkę
    //    }
    //
    //    if (shouldExit && tradingRecord.getCurrentPosition().isOpened()) {
    //      Num entryPrice = tradingRecord.getCurrentPosition().getEntry().getNetPrice();
    //      System.out.println("Sygnał sprzedaży! Sprzedaję za: " + currentPrice);
    //      tradingRecord.exit(lastIndex, currentPrice, tradeAmount);
    //
    //      Num profitOrLoss = currentPrice.minus(entryPrice);
    //      totalBalance = (totalBalance == null) ? profitOrLoss : totalBalance.plus(profitOrLoss);
    //
    //      System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
    //      // Po zamknięciu pozycji możemy wyliczyć zysk/stratę
    //      System.out.println("Kupiono za: " + entryPrice + ", Sprzedano za: " + currentPrice);
    //      System.out.println("Zysk/strata: " + currentPrice.minus(entryPrice));
    //      System.out.println("------");
    //      System.out.println("Całkowity bilans strategii: " + totalBalance);
    //      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
    //    }
  }
}

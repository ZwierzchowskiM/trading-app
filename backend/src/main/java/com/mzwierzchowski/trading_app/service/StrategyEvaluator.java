package com.mzwierzchowski.trading_app.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

@Service
public class StrategyEvaluator {

  Num enterPrice;
  Num exitPrice;
  double result;

  private Num totalBalance; // Całkowity bilans strategii
  private TradingRecord tradingRecord = new BaseTradingRecord(); // Rekord transakcji

  public StrategyEvaluator() {
    this.totalBalance = null; // Inicjalizacja na null, bo TA4J wymaga Num zamiast double
  }

  public void evaluate(BarSeries series) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

    EMAIndicator emaShort = new EMAIndicator(closePrice, 10); // EMA 10-okresowa
    EMAIndicator emaLong = new EMAIndicator(closePrice, 50); // EMA 50-okresowa

    RSIIndicator rsi = new RSIIndicator(closePrice, 14); // RSI 14-okresowe

    int lastIndex = series.getEndIndex();
    System.out.println("EMA(10): " + emaShort.getValue(lastIndex));
    System.out.println("EMA(50): " + emaLong.getValue(lastIndex));
    System.out.println("RSI(14): " + rsi.getValue(lastIndex));

    Rule entryRule =
        new CrossedUpIndicatorRule(emaShort, emaLong) // EMA(10) przecina EMA(30) w górę
            .and(new OverIndicatorRule(rsi, 50)); // RSI powyżej 50

    Rule exitRule =
        new CrossedDownIndicatorRule(emaShort, emaLong) // EMA(10) przecina EMA(30) w dół
            .and(new UnderIndicatorRule(rsi, 70)); // RSI poniżej 70



    Strategy strategy = new BaseStrategy(entryRule, exitRule);
    lastIndex = series.getEndIndex();
    Num currentPrice = closePrice.getValue(lastIndex);

    boolean shouldEnter = strategy.getEntryRule().isSatisfied(lastIndex, tradingRecord);
    boolean shouldExit = strategy.getExitRule().isSatisfied(lastIndex, tradingRecord);

    if (shouldEnter){
      System.out.println("sygnał kupna");
    }
    if (shouldExit){
      System.out.println("sygnał sprzedaży");
    }


    Num tradeAmount = series.numOf(1);
    if (shouldEnter && !tradingRecord.getCurrentPosition().isOpened()) {
      System.out.println("Sygnał kupna aktywny! Kupuję za: " + currentPrice);
      tradingRecord.enter(lastIndex, currentPrice, tradeAmount); // Kupujemy 1 jednostkę
    }

    if (shouldExit && tradingRecord.getCurrentPosition().isOpened()) {
      Num entryPrice = tradingRecord.getCurrentPosition().getEntry().getNetPrice();
      System.out.println("Sygnał sprzedaży! Sprzedaję za: " + currentPrice);
      tradingRecord.exit(lastIndex, currentPrice, tradeAmount);

      Num profitOrLoss = currentPrice.minus(entryPrice);
      totalBalance = (totalBalance == null) ? profitOrLoss : totalBalance.plus(profitOrLoss);

      System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
      // Po zamknięciu pozycji możemy wyliczyć zysk/stratę
      System.out.println("Kupiono za: " + entryPrice + ", Sprzedano za: " + currentPrice);
      System.out.println("Zysk/strata: " + currentPrice.minus(entryPrice));
      System.out.println("------");
      System.out.println("Całkowity bilans strategii: " + totalBalance);
      System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }
  }
}

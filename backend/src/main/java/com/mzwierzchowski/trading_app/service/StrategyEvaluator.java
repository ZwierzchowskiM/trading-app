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
            .and(new OverIndicatorRule(rsi, 40)); // RSI poniżej 30

    Rule exitRule =
        new CrossedDownIndicatorRule(emaShort, emaLong) // EMA(10) przecina EMA(30) w dół
            .and(new UnderIndicatorRule(rsi, 70)); // RSI powyżej 70



    Strategy strategy = new BaseStrategy(entryRule, exitRule);
    TradingRecord tradingRecord = new BaseTradingRecord();
    lastIndex = series.getEndIndex();
    boolean shouldEnter = strategy.getEntryRule().isSatisfied(lastIndex, tradingRecord);
    boolean shouldExit = strategy.getExitRule().isSatisfied(lastIndex, tradingRecord);

    if (shouldEnter) {
      System.out.println("Wskaźniki. Sygnał kupna aktywny");
    }

    if (shouldExit) {
      System.out.println("Wskaźniki. Sygnał kupna sprzedaży");
    }

    Rule entryRule_noRSI = new CrossedUpIndicatorRule(emaShort, emaLong);

    Rule exitRule_noRSI = new CrossedDownIndicatorRule(emaShort, emaLong);

    Strategy strategy2 = new BaseStrategy(entryRule_noRSI, exitRule_noRSI);
    TradingRecord tradingRecord2 = new BaseTradingRecord();

    lastIndex = series.getEndIndex();
    boolean shouldEnter2 = strategy2.getEntryRule().isSatisfied(lastIndex, tradingRecord2);
    boolean shouldExit2 = strategy2.getExitRule().isSatisfied(lastIndex, tradingRecord2);

    if (shouldEnter2) {
      System.out.println("Wskaźniki bez RSI. Sygnał kupna aktywny");
    }
    if (shouldExit2) {
      System.out.println("Wskaźniki bez RSI. Sygnał kupna sprzedaży");
    }
  }
}

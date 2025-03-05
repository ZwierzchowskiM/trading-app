package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.strategy.BullishGChannelRule;
import com.mzwierzchowski.trading_app.strategy.GChannel;
import com.mzwierzchowski.trading_app.strategy.GChannelLowerIndicator;
import com.mzwierzchowski.trading_app.strategy.GChannelUpperIndicator;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

/** Klasa odpowiadająca za ocenę strategii i wykonywanie transakcji. */
@Service
public class StrategyEvaluator {

  Num enterPrice;
  Num exitPrice;
  double result;

  public void evaluate(BarSeries series) {

    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

    // Dodanie EMA
    EMAIndicator emaShort = new EMAIndicator(closePrice, 10); // EMA 10-okresowa
    EMAIndicator emaLong = new EMAIndicator(closePrice, 50); // EMA 50-okresowa

    // Dodanie RSI
    RSIIndicator rsi = new RSIIndicator(closePrice, 14); // RSI 14-okresowe

    // Wyświetlenie wartości wskaźników dla najnowszej świecy
    int lastIndex = series.getEndIndex();
    System.out.println("EMA(10): " + emaShort.getValue(lastIndex));
    System.out.println("EMA(50): " + emaLong.getValue(lastIndex));
    System.out.println("RSI(14): " + rsi.getValue(lastIndex));

    // Entry Rule (warunek kupna)
    Rule entryRule =
        new CrossedUpIndicatorRule(emaShort, emaLong) // EMA(10) przecina EMA(30) w górę
            .and(new UnderIndicatorRule(rsi, 30)); // RSI poniżej 30

    // Exit Rule (warunek sprzedaży)
    Rule exitRule =
        new CrossedDownIndicatorRule(emaShort, emaLong) // EMA(10) przecina EMA(30) w dół
            .or(new OverIndicatorRule(rsi, 70)); // RSI powyżej 70

    Strategy strategy = new BaseStrategy(entryRule, exitRule);
    TradingRecord tradingRecord = new BaseTradingRecord();
    // Sprawdzenie ostatniego baru (świecy)
    lastIndex = series.getEndIndex();
    boolean shouldEnter = strategy.getEntryRule().isSatisfied(lastIndex, tradingRecord);
    boolean shouldExit = strategy.getExitRule().isSatisfied(lastIndex, tradingRecord);

    if (shouldEnter) {
      System.out.println("Wskaźniki. Sygnał kupna aktywny");
    }

    if (shouldExit) {
      System.out.println("Wskaźniki. Sygnał kupna sprzedaży");
    }

    //    Strategy strategy = new BaseStrategy(buyRule, sellRule);

    //    if (strategy.shouldEnter(endIndex) && tradingRecord.isClosed()) {
    //      System.out.println("Strategy should ENTER on " + endIndex);
    //      boolean entered =
    //          tradingRecord.enter(
    //              endIndex, series.getLastBar().getClosePrice(), DecimalNum.valueOf(0.001));
    //      if (entered) {
    //        Trade entry = tradingRecord.getLastEntry();
    //        System.out.println(
    //            "Entered: "
    //                + series.getLastBar().getEndTime()
    //                + " (price="
    //                + entry.getNetPrice().doubleValue()
    //                + ", amount="
    //                + entry.getAmount().doubleValue()
    //                + ")");
    //      }
    //    }
    //
    //    if (strategy.shouldExit(endIndex) && !tradingRecord.isClosed()) {
    //      System.out.println("Strategy should EXIT on " + endIndex);
    //      enterPrice = tradingRecord.getLastEntry().getNetPrice();
    //      boolean exited =
    //          tradingRecord.exit(endIndex, series.getLastBar().getClosePrice(),
    // DecimalNum.valueOf(1));
    //      if (exited) {
    //        Trade exit = tradingRecord.getLastExit();
    //        System.out.println(
    //            "Exited: "
    //                + series.getLastBar().getEndTime()
    //                + " (price="
    //                + exit.getNetPrice().doubleValue()
    //                + ", amount="
    //                + exit.getAmount().doubleValue()
    //                + ")");
    //        exitPrice = tradingRecord.getLastExit().getNetPrice();
    //        result = exitPrice.doubleValue() - enterPrice.doubleValue();
    //        System.out.println("Wynik strategii: " + result);
    //        System.out.println("------------------------------------------------------");
    //      }
    //    }
  }
}

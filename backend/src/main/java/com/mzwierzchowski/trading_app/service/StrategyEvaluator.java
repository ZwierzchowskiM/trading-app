package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.strategy.BullishGChannelRule;
import com.mzwierzchowski.trading_app.strategy.GChannel;
import com.mzwierzchowski.trading_app.strategy.GChannelLowerIndicator;
import com.mzwierzchowski.trading_app.strategy.GChannelUpperIndicator;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AndRule;
import org.ta4j.core.rules.NotRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/** Klasa odpowiadająca za ocenę strategii i wykonywanie transakcji. */
@Service
public class StrategyEvaluator {

  Num eneterPrice;
  Num exitPrice;
  double result;

  public void evaluate(BarSeries series, TradingRecord tradingRecord) {
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

    if (strategy.shouldEnter(endIndex) && tradingRecord.isClosed()) {
      System.out.println("Strategy should ENTER on " + endIndex);
      boolean entered =
          tradingRecord.enter(
              endIndex, series.getLastBar().getClosePrice(), DecimalNum.valueOf(0.001));
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
    }

    if (strategy.shouldExit(endIndex) && !tradingRecord.isClosed()) {
      System.out.println("Strategy should EXIT on " + endIndex);
      eneterPrice = tradingRecord.getLastEntry().getNetPrice();
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
        exitPrice = tradingRecord.getLastExit().getNetPrice();
        result = exitPrice.doubleValue() - eneterPrice.doubleValue();
        System.out.println("Wynik strategii: " + result);
        System.out.println("------------------------------------------------------");
      }
    }
  }
}

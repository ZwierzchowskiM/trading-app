package com.mzwierzchowski.trading_app.service;

import com.mzwierzchowski.trading_app.strategy.BullishGChannelRule;
import com.mzwierzchowski.trading_app.strategy.GChannel;
import com.mzwierzchowski.trading_app.strategy.GChannelLowerIndicator;
import com.mzwierzchowski.trading_app.strategy.GChannelUpperIndicator;

import java.io.IOException;

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

@Service
public class StrategyTester {

  YahooFinanceService yahooFinanceService;
  DataFileWriter dataFileWriter;

  public StrategyTester(YahooFinanceService yahooFinanceService, DataFileWriter dataFileWriter) {
    this.yahooFinanceService = yahooFinanceService;
    this.dataFileWriter = dataFileWriter;
  }

  public static void main(String[] args) throws IOException, InterruptedException {

    YahooFinanceService yahooFinanceService = new YahooFinanceService();
    DataFileWriter dataFileWriter = new DataFileWriter();

    BarSeries series = yahooFinanceService.getDataFromYahoo();
    DataFileWriter.writeDataToFile(series);


    // Wskaźnik ceny zamknięcia
    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

    // EMA o okresie 200
    EMAIndicator ema = new EMAIndicator(closePrice, 200);

    // G-Channel z okresem 5 (dla źródła 'close')
    GChannel gChannel = new GChannel(closePrice, 5, series);


    GChannelUpperIndicator gChannelUpper = new GChannelUpperIndicator(gChannel);
    GChannelLowerIndicator gChannelLower = new GChannelLowerIndicator(gChannel);


    // Reguła bullish oparta o G-Channel
    BullishGChannelRule bullishRule =
        new BullishGChannelRule(series, gChannelUpper, gChannelLower, closePrice);

    Rule buyRule = new AndRule(bullishRule, new UnderIndicatorRule(closePrice, ema));
    Rule sellRule = new AndRule(new NotRule(bullishRule), new OverIndicatorRule(closePrice, ema));

    Strategy strategy = new BaseStrategy(buyRule, sellRule);

    // Uruchomienie testu strategii
    BarSeriesManager seriesManager = new BarSeriesManager(series);
    TradingRecord tradingRecord = seriesManager.run(strategy);

    Num strategyResult = DecimalNum.valueOf(0);
    int countWinning = 0;
    double result =0;
    for (int i = 0; i < tradingRecord.getPositions().size(); i++) {
      System.out.println("------------");
      System.out.println(tradingRecord.getPositions().get(i).getEntry());
      System.out.println(tradingRecord.getPositions().get(i).getExit());
      strategyResult = strategyResult.plus(tradingRecord.getPositions().get(i).getGrossProfit());
      result = tradingRecord.getPositions().get(i).getGrossProfit().doubleValue();
      if (result>0) {
        countWinning++;
      }
    }
    System.out.println("Liczba transakcji: " + tradingRecord.getPositionCount());
    System.out.println("ilość wygranych transakcji: " + countWinning);
    System.out.println("Wynik strategii: "+ strategyResult);
  }
}


package com.mzwierzchowski.trading_app.model.StockTwits;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockTwitsResult {

  double bullishPercentage;
  double bearishPercentage;
  int bullishDiff;
  int bearishDiff;

  public StockTwitsResult(
      double bullishPercentage, double bearishPercentage, int bullishDiff, int bearishDiff) {
    this.bullishPercentage = bullishPercentage;
    this.bearishPercentage = bearishPercentage;
    this.bullishDiff = bullishDiff;
    this.bearishDiff = bearishDiff;
  }
}

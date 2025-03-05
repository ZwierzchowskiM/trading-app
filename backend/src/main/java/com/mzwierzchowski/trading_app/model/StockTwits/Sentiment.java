package com.mzwierzchowski.trading_app.model.StockTwits;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sentiment {
    private String basic; // np. "Bullish" lub "Bearish"

    public String getBasic() { return basic; }
    public void setBasic(String basic) { this.basic = basic; }
}
package com.mzwierzchowski.trading_app.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Candle {
    private long openTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private long closeTime;
    private BigDecimal quoteAssetVolume;
    private int numberOfTrades;
    private BigDecimal takerBuyBaseVolume;
    private BigDecimal takerBuyQuoteVolume;


    @Override
    public String toString() {
        return "Candle{" +
                "openTime=" + openTime +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", closeTime=" + closeTime +
                ", quoteAssetVolume=" + quoteAssetVolume +
                ", numberOfTrades=" + numberOfTrades +
                ", takerBuyBaseVolume=" + takerBuyBaseVolume +
                ", takerBuyQuoteVolume=" + takerBuyQuoteVolume +
                '}';
    }
}
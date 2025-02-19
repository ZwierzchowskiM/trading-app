package com.mzwierzchowski.trading_app.strategy;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

// Wrapper dla dolnej linii G-Channel (b)
public class GChannelLowerIndicator extends CachedIndicator<Num> {
    private final GChannel gChannel;

    public GChannelLowerIndicator(GChannel gChannel) {
        super(gChannel);
        this.gChannel = gChannel;
    }

    @Override
    protected Num calculate(int index) {
        return gChannel.getLower(index);
    }
}
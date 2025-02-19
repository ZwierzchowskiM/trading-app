package com.mzwierzchowski.trading_app.strategy;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

// Wrapper dla górnej linii G-Channel (a)
public class GChannelUpperIndicator extends CachedIndicator<Num> {
    private final GChannel gChannel;

    public GChannelUpperIndicator(GChannel gChannel) {
        super(gChannel);
        this.gChannel = gChannel;
    }

    @Override
    protected Num calculate(int index) {
        return gChannel.getUpper(index);
    }
}


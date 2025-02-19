package com.mzwierzchowski.trading_app.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

// Reguła określająca, czy jesteśmy w trendzie bullish według G-Channel
// Definicja: bullish, gdy liczba świec od ostatniego crossdn (górna linia)
// jest mniejsza lub równa liczbie świec od ostatniego crossup (dolna linia)
public class BullishGChannelRule extends AbstractRule {

    private final BarSeries series;
    private final GChannelUpperIndicator upper;
    private final GChannelLowerIndicator lower;
    private final ClosePriceIndicator closePrice;

    public BullishGChannelRule(BarSeries series, GChannelUpperIndicator upper, GChannelLowerIndicator lower, ClosePriceIndicator closePrice) {
        this.series = series;
        this.upper = upper;
        this.lower = lower;
        this.closePrice = closePrice;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        int lastCrossUp = lastCrossIndex(index, true);
        int lastCrossDn = lastCrossIndex(index, false);

        int barsSinceCrossUp = (lastCrossUp == -1) ? Integer.MAX_VALUE : index - lastCrossUp;
        int barsSinceCrossDn = (lastCrossDn == -1) ? Integer.MAX_VALUE : index - lastCrossDn;

        boolean bullish = barsSinceCrossDn <= barsSinceCrossUp;
        // Można dodać logowanie: System.out.println("Bar " + index + " bullish: " + bullish);
        return bullish;
    }

    // Szuka ostatniego indeksu, w którym wystąpiło dane "krzyżowanie"
    // Jeśli isCrossUp == true – szukamy crossup, w przeciwnym wypadku crossdn.
    private int lastCrossIndex(int index, boolean isCrossUp) {
        for (int i = index; i >= 1; i--) {
            if (isCrossEvent(i, isCrossUp)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isCrossEvent(int index, boolean isCrossUp) {
        if (index < 1) return false;

        Num prevClose = closePrice.getValue(index - 1);
        Num currClose = closePrice.getValue(index);

        if (isCrossUp) {
            // Sprawdzamy przecięcie dolnej linii z ceną
            Num prevLower = lower.getValue(index - 1);
            Num currLower = lower.getValue(index);

            // Cena przebija się w dół przez dolną linię
            return prevClose.isGreaterThan(prevLower) &&
                    currClose.isLessThan(currLower);
        } else {
            // Sprawdzamy przecięcie górnej linii z ceną
            Num prevUpper = upper.getValue(index - 1);
            Num currUpper = upper.getValue(index);

            // Cena przebija się w dół przez górną linię
            return prevClose.isGreaterThan(prevUpper) &&
                    currClose.isLessThan(currUpper);
        }
    }

}
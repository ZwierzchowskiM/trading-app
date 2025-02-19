package com.mzwierzchowski.trading_app.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.Map;

// G-Channel wskaźnik (oblicza rekurencyjnie górną (a) i dolną (b) linię)
public class GChannel extends CachedIndicator<Num> {

    private final Indicator<Num> src;
    private final int length;
    private final BarSeries series;
    private final Map<Integer, Num> upperCache = new HashMap<>();
    private final Map<Integer, Num> lowerCache = new HashMap<>();

    public GChannel(Indicator<Num> src, int length, BarSeries series) {
        super(src);
        this.src = src;
        this.length = length;
        this.series = series;
    }

    // Metoda calculate zwraca średnią z górnej i dolnej linii
    @Override
    protected Num calculate(int index) {
        Num upper = getUpper(index);
        Num lower = getLower(index);
        return upper.plus(lower).dividedBy(series.numOf(2));
    }

    // Obliczenie górnej linii (a)
    public Num getUpper(int index) {
        if (upperCache.containsKey(index)) {
            return upperCache.get(index);
        }
        Num value;
        if (index == 0) {
            value = src.getValue(0);
        } else {
            Num prevA = getUpper(index - 1);
            Num prevB = getLower(index - 1);
            Num currentSrc = src.getValue(index);
            // a = max(src, a[1]) - (a[1]-b[1])/length
            Num maxVal = currentSrc.isLessThan(prevA) ? prevA : currentSrc;
            value = maxVal.minus(prevA.minus(prevB).dividedBy(series.numOf(length)));
        }
        upperCache.put(index, value);
        return value;
    }

    // Obliczenie dolnej linii (b)
    public Num getLower(int index) {
        if (lowerCache.containsKey(index)) {
            return lowerCache.get(index);
        }
        Num value;
        if (index == 0) {
            value = src.getValue(0);
        } else {
            Num prevA = getUpper(index - 1);
            Num prevB = getLower(index - 1);
            Num currentSrc = src.getValue(index);
            // b = min(src, a[1]) + (a[1]-b[1])/length
            Num minVal = currentSrc.isGreaterThan(prevA) ? prevA : currentSrc;
            value = minVal.plus(prevA.minus(prevB).dividedBy(series.numOf(length)));
        }
        lowerCache.put(index, value);
        return value;
    }
}
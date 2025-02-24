package com.mzwierzchowski.trading_app.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import pro.xstore.api.message.records.RateInfoRecord;
import pro.xstore.api.message.response.ChartResponse;

@Service
public class HistoricalDataConverter {

    /**
     * Konwertuje ChartResponse (dane historyczne) na BarSeries.
     *
     * @param chartHistoricalData dane historyczne z API XTB
     * @return utworzona seria świec
     */
    public BarSeries convertToSeries(ChartResponse chartHistoricalData) {
        BarSeries series = new BaseBarSeries();
        List<RateInfoRecord> rateInfoRecords = chartHistoricalData.getRateInfos();
        int digits = chartHistoricalData.getDigits();

        for (RateInfoRecord rateInfoRecord : rateInfoRecords) {
            long ctmMillis = rateInfoRecord.getCtm();
            ZonedDateTime barTime = Instant.ofEpochMilli(ctmMillis).atZone(ZoneId.systemDefault());
            // Cena otwarcia już jest pomnożona, więc dzielimy przez 10^digits
            double open = rateInfoRecord.getOpen() / Math.pow(10, digits);
            // Pozostałe wartości to przesunięcia od ceny otwarcia
            double close = open + (rateInfoRecord.getClose() / Math.pow(10, digits));
            double high = open + (rateInfoRecord.getHigh() / Math.pow(10, digits));
            double low = open + (rateInfoRecord.getLow() / Math.pow(10, digits));
            double volume = rateInfoRecord.getVol();

            series.addBar(barTime, open, high, low, close, volume);
        }

        return series;
    }
}
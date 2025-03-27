package com.mzwierzchowski.trading_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzwierzchowski.trading_app.model.Candle;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceCandleParser {

    String symbol = "BTCUSDC";
    String interval = "5m";
    int limit = 100;
    String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=%s&limit=%d",
            symbol, interval, limit);

    public List<Candle> parseCandles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new URL(url));
        List<Candle> candles = new ArrayList<>();

        for (JsonNode node : root) {
            Candle candle = new Candle();
            candle.setOpenTime(node.get(0).asLong());
            candle.setOpen(new BigDecimal(node.get(1).asText()));
            candle.setHigh(new BigDecimal(node.get(2).asText()));
            candle.setLow(new BigDecimal(node.get(3).asText()));
            candle.setClose(new BigDecimal(node.get(4).asText()));
            candle.setVolume(new BigDecimal(node.get(5).asText()));
            candle.setCloseTime(node.get(6).asLong());
            candle.setQuoteAssetVolume(new BigDecimal(node.get(7).asText()));
            candle.setNumberOfTrades(node.get(8).asInt());
            candle.setTakerBuyBaseVolume(new BigDecimal(node.get(9).asText()));
            candle.setTakerBuyQuoteVolume(new BigDecimal(node.get(10).asText()));
            candles.add(candle);
        }

        // Sprawdzenie, czy ostatnia świeca jest zamknięta
        if (!candles.isEmpty()) {
            Candle lastCandle = candles.get(candles.size() - 1);
            long currentTimeMillis = System.currentTimeMillis();

            if (lastCandle.getCloseTime() > currentTimeMillis) {
                candles.remove(candles.size() - 1);
            }
        }

        return candles;
    }
}

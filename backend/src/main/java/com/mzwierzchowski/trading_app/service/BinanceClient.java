package com.mzwierzchowski.trading_app.service;

import com.binance.connector.client.impl.SpotClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BinanceClient {
    private final SpotClientImpl client;


    public BinanceClient(@Value("${binance.apiKey}") String apiKey,
                         @Value("${binance.secret}") String secretKey) {
        client = new SpotClientImpl(apiKey, secretKey);
    }

    public String getAccountInformation() {
        return client.createTrade().account(new HashMap<>());
    }

    public String placeOrder(String symbol, String side, String type, double quantity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", side);
        parameters.put("type", type);
        parameters.put("quantity", quantity);
        return client.createTrade().newOrder(parameters);
    }
}
package com.mzwierzchowski.trading_app.model.StockTwits;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private long id;
    private String body;
    private String created_at;
    private boolean discussion;
    private Entities entities;


}
package com.mzwierzchowski.trading_app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockTwitsResponse {

    private List<Message> messages;

}
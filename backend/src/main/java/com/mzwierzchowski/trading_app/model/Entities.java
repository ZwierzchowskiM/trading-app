package com.mzwierzchowski.trading_app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {


    private Sentiment sentiment;

}

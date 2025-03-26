package com.mzwierzchowski.trading_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class TradePosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean isOpened;
    private double openPrice;
    private double closePrice;
    private double result;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;

}

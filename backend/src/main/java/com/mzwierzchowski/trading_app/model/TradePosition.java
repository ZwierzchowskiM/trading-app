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
    private boolean isOpened;         // Czy pozycja jest otwarta
    private double openPrice;         // Cena otwarcia
    private double closePrice;        // Cena zamknięcia
    private double result;            // Wynik transakcji
    private LocalDateTime openDate;   // Data i czas otwarcia
    private LocalDateTime closeDate;  // Data i czas zamknięcia

}

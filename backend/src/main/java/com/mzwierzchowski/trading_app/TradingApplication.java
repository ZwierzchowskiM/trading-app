package com.mzwierzchowski.trading_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TradingApplication {

  public static void main(String[] args) {
    SpringApplication.run(TradingApplication.class, args);
  }
}

// RealTimeStrategyRunner realTimeStrategyRunner = new RealTimeStrategyRunner();
// realTimeStrategyRunner.start();

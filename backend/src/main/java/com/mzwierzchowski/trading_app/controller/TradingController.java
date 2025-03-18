package com.mzwierzchowski.trading_app.controller;

import com.mzwierzchowski.trading_app.service.BinanceClient;
import com.mzwierzchowski.trading_app.service.RealTimeStrategyRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/trading")
@CrossOrigin(
    origins = {"http://localhost:3000", "https://your-vercel-app.vercel.app"},
    allowCredentials = "true")
public class TradingController {

  private final RealTimeStrategyRunner strategyRunner;
  private BinanceClient binanceClient;

  public TradingController(RealTimeStrategyRunner strategyRunner, BinanceClient binanceClient) {
    this.strategyRunner = strategyRunner;
      this.binanceClient = binanceClient;
  }

  @GetMapping("/check")
  public ResponseEntity checkSymbol() {
    strategyRunner.getSinglePrice();

    return new ResponseEntity<>(HttpStatusCode.valueOf(200));
  }

  @GetMapping("/status")
  public ResponseEntity getStatus() {

    return new ResponseEntity<>(HttpStatusCode.valueOf(200));
  }

  @GetMapping("/buy")
  public ResponseEntity placeOrderBuy() {

    String response = binanceClient.placeOrder("BTCUSDT", "BUY", "MARKET", 0.00005);
    System.out.println(response);

    return new ResponseEntity<>(HttpStatusCode.valueOf(200));
  }

  @GetMapping("/sell")
  public ResponseEntity placeOrderSell() {

    String response = binanceClient.placeOrder("BTCUSDT", "SELL", "MARKET", 0.00005);
    System.out.println(response);

    return new ResponseEntity<>(HttpStatusCode.valueOf(200));
  }

}

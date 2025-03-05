package com.mzwierzchowski.trading_app.controller;

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

  public TradingController(RealTimeStrategyRunner strategyRunner) {
    this.strategyRunner = strategyRunner;
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

}

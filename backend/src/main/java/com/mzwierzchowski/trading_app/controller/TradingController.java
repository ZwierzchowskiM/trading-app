package com.mzwierzchowski.trading_app.controller;

import com.mzwierzchowski.trading_app.model.TradePosition;
import com.mzwierzchowski.trading_app.service.BinanceClient;
import com.mzwierzchowski.trading_app.service.EmailService;
import com.mzwierzchowski.trading_app.service.RealTimeStrategyRunner;
import com.mzwierzchowski.trading_app.service.StrategyEvaluator;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Log4j2
@RestController
@RequestMapping("/api/trading")
@CrossOrigin(
    origins = {"http://localhost:3000", "https://your-vercel-app.vercel.app"},
    allowCredentials = "true")
public class TradingController {

  private final RealTimeStrategyRunner strategyRunner;
  private BinanceClient binanceClient;
  private StrategyEvaluator strategyEvaluator;
  private EmailService emailService;

  public TradingController(RealTimeStrategyRunner strategyRunner, BinanceClient binanceClient, StrategyEvaluator strategyEvaluator, EmailService emailService) {
    this.strategyRunner = strategyRunner;
      this.binanceClient = binanceClient;
      this.strategyEvaluator = strategyEvaluator;
      this.emailService = emailService;
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
  public ResponseEntity<String> placeOrderBuy(
          @RequestParam String symbol,
          @RequestParam double quantity) {

    String response = binanceClient.placeOrder(symbol, "BUY", "MARKET", quantity);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/sell")
  public ResponseEntity<String> placeOrderSell(
          @RequestParam String symbol,
          @RequestParam double quantity) {

    String response = binanceClient.placeOrder(symbol, "SELL", "MARKET", quantity);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/set-params")
  public ResponseEntity<String> setTradingParams(
          @RequestParam String symbol,
          @RequestParam double quantity) {

    strategyEvaluator.setSymbol(symbol);
    strategyEvaluator.setQuantity(quantity);

    return ResponseEntity.ok("Zaktualizowano parametry: symbol = " + symbol + ", ilość = " + quantity);
  }

  @PostMapping("/email-buy")
  public ResponseEntity<String> sendBuyEmail() {
    TradePosition position = new TradePosition();
    position.setOpenPrice(50000.0);
    position.setOpenDate(LocalDateTime.now());

    try {
      emailService.sendTradeNotification("app.mzwierzchowski@gmail.com", "BUY", position);
      return ResponseEntity.ok("Email z informacją o kupnie wysłany.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd wysyłania emaila.");
    }
  }

  @PostMapping("/email-sell")
  public ResponseEntity<String> sendSellEmail() {
    TradePosition position = new TradePosition();
    position.setOpenDate(LocalDateTime.now());
    position.setCloseDate(LocalDateTime.now());
    position.setOpenPrice(50000.0);
    position.setClosePrice(50500.0);
    position.setResult(500.0);
    position.setCloseDate(LocalDateTime.now());

    try {
      emailService.sendTradeNotification("app.mzwierzchowski@gmail.com", "SELL", position);
      return ResponseEntity.ok("Email z informacją o sprzedaży wysłany.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd wysyłania emaila.");
    }
  }

}

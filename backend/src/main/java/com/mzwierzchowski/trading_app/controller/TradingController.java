package com.mzwierzchowski.trading_app.controller;

import com.mzwierzchowski.trading_app.service.RealTimeStrategyRunner;
import lombok.extern.log4j.Log4j2;
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

    @PostMapping("/start")
    public String startStrategy() {
        strategyRunner.start();
    System.out.println("Zadanie uruchomienia strategii");
        return "Strategia uruchomiona!";
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Strategia działa!";
    }

    @PostMapping("/stop")
    public String stopStrategy() {
        strategyRunner.stop();
        System.out.println("Zadanie zatrzymania strategii");
        return "Strategia zatrzymana!";
    }
}
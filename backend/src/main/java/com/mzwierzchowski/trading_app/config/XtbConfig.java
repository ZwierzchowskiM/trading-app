package com.mzwierzchowski.trading_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

import java.io.IOException;

@Configuration
@EnableWebSocket
public class XtbConfig implements WebSocketConfigurer {

//    @Value("${xtb.userId}")
    private String userId = "marcin.zwierzchowski1@gmail.com";

//    @Value("${xtb.password}")
    private String password = "!0okm9iJJ";

    public SyncAPIConnector syncConnector() throws IOException {
        return new SyncAPIConnector(ServerData.ServerEnum.DEMO);
    }

    public Credentials xtbCredentials() {
        return new Credentials(userId, password);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Konfiguracja WebSocket jeśli potrzebna
    }
}
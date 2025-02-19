package com.mzwierzchowski.trading_app.service;

import java.io.IOException;
import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.records.*;
import pro.xstore.api.message.response.*;
import pro.xstore.api.streaming.StreamingListener;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

@Service
@Slf4j
@RequiredArgsConstructor
public class XtbService {

  @Value("${xtb.userId}")
  private String userId;
  @Value("${xtb.pass}")
  private String password ;

  public Credentials xtbCredentials() {
    return new Credentials(userId, password);
  }

  public SyncAPIConnector connect() {

    try {
      SyncAPIConnector connector = new SyncAPIConnector(ServerData.ServerEnum.DEMO);
      LoginResponse loginResponse = null;
      loginResponse = APICommandFactory.executeLoginCommand(connector, xtbCredentials());
      if (loginResponse.getStatus() == true) {
        System.out.println("User logged in");
      }
      return connector;
    } catch (APICommandConstructionException
        | APICommunicationException
        | APIReplyParseException
        | APIErrorResponse
        | IOException e) {
      throw new RuntimeException(e);
    }
  }

}

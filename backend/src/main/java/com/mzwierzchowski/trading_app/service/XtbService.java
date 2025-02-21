package com.mzwierzchowski.trading_app.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.xstore.api.message.command.APICommandFactory;
import pro.xstore.api.message.error.APICommandConstructionException;
import pro.xstore.api.message.error.APICommunicationException;
import pro.xstore.api.message.error.APIReplyParseException;
import pro.xstore.api.message.response.*;
import pro.xstore.api.sync.Credentials;
import pro.xstore.api.sync.ServerData;
import pro.xstore.api.sync.SyncAPIConnector;

@Service
public class XtbService {

  @Value("${XTB_USERID}")
  private String userId;
  @Value("${XTB_PASS}")
  private String password;

  public SyncAPIConnector connect() {

    try {
      SyncAPIConnector connector = new SyncAPIConnector(ServerData.ServerEnum.DEMO);
      LoginResponse loginResponse = null;
      System.out.println(userId);
      System.out.println(password);
      loginResponse = APICommandFactory.executeLoginCommand(connector, new Credentials(userId, password));
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

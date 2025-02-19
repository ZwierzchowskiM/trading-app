package com.mzwierzchowski.trading_app.model.dtos;

import com.mzwierzchowski.trading_app.model.NotificationType;
import lombok.Data;

@Data
public class NotificationRequestDTO {
  private NotificationType notificationType;
  private Double notificationPrice;
    }

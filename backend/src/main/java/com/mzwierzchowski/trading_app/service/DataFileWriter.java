package com.mzwierzchowski.trading_app.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

@Service
public class DataFileWriter {

  public static void writeDataToFile(BarSeries series) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    try (BufferedWriter writer = new BufferedWriter(new FileWriter("dane_z_yahoo.txt"))) {
      writer.write("timestamp,open,high,low,close,volume");
      writer.newLine();

      for (int i = 0; i < series.getBarCount(); i++) {
        Bar bar = series.getBar(i);
        ZonedDateTime barTime = bar.getEndTime();

        StringBuilder line = new StringBuilder();
        line.append(i).append(",");
        line.append(barTime.format(formatter)).append(",");
        line.append(bar.getOpenPrice()).append(",");
        line.append(bar.getHighPrice()).append(",");
        line.append(bar.getLowPrice()).append(",");
        line.append(bar.getClosePrice()).append(",");
        line.append(bar.getVolume());

        writer.write(line.toString());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

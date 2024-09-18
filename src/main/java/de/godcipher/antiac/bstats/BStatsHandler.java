package de.godcipher.antiac.bstats;

import lombok.experimental.UtilityClass;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class BStatsHandler {

  private int flagged;

  public void init(JavaPlugin javaPlugin) {
    Metrics metrics = new Metrics(javaPlugin, 6473);
    metrics.addCustomChart(
        new SingleLineChart(
            "flagged_players",
            () -> {
              int totalFlagged = flagged;
              flagged = 0;
              return totalFlagged;
            }));
  }

  public synchronized void increaseFlagged() {
    flagged++;
  }
}

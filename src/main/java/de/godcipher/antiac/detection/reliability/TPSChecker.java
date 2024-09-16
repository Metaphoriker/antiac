package de.godcipher.antiac.detection.reliability;

import de.godcipher.antiac.AntiAC;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

public class TPSChecker {

  @Getter private double tps;

  private byte tick;
  private double lastFinish;

  public void start() {
    new BukkitRunnable() {
      @Override
      public void run() {
        tick();
      }
    }.runTaskTimer(AntiAC.getInstance(), 1, 1);
  }

  public boolean isReliable() {
    return tps > 18; // TODO: make this configurable
  }

  private void tick() {
    tick++;
    if (tick == 20) {
      tps = tick;
      tick = 0;

      if (lastFinish + 1000 < System.currentTimeMillis())
        tps /= (System.currentTimeMillis() - lastFinish) / 1000;

      lastFinish = System.currentTimeMillis();
    }
  }
}

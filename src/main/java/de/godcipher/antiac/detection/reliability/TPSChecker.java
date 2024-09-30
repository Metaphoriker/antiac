package de.godcipher.antiac.detection.reliability;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.AntiACConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class TPSChecker {

  private final AntiACConfig configuration;

  @Getter private double tps;

  private int reliableTpsThreshold = 18;

  private byte tick;
  private double lastFinish;

  public void start() {
    reliableTpsThreshold = configuration.getTpsProtection();
    schedule();
  }

  private void schedule() {
    new BukkitRunnable() {
      @Override
      public void run() {
        tick();
      }
    }.runTaskTimer(AntiAC.getInstance(), 1, 1);
  }

  public boolean isReliable() {
    return tps > reliableTpsThreshold;
  }

  private void tick() {
    if (tick++ == 20) finish();
  }

  private void finish() {
    tps = calculateTPS();
    tick = 0;
    lastFinish = System.currentTimeMillis();
  }

  private double calculateTPS() {
    double currentTime = System.currentTimeMillis();
    double elapsedTime = currentTime - lastFinish;
    return elapsedTime > 1000 ? 20.0 / (elapsedTime / 1000) : 20.0;
  }
}

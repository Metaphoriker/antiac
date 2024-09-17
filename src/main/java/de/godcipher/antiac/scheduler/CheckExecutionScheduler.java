package de.godcipher.antiac.scheduler;

import de.godcipher.antiac.detection.CheckRegistry;
import de.godcipher.antiac.detection.reliability.TPSChecker;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;

@RequiredArgsConstructor
@Slf4j
public class CheckExecutionScheduler implements Runnable {

  private final ClickTracker clickTracker;
  private final CheckRegistry checkRegistry;
  private final TPSChecker tpsChecker;

  @Override
  public void run() {
    if (!tpsChecker.isReliable()) {
      log.debug("TPS is not reliable, skipping check execution");
      return;
    }

    for (var player : Bukkit.getOnlinePlayers()) {
      ensureFirstCPS(player.getUniqueId());
      checkRegistry.performChecks(player);
      clickTracker.addNewCPS(player.getUniqueId());
    }

    clickTracker.removeLastCPSIfExceedsLimit(); // cleanup
  }

  private void ensureFirstCPS(UUID player) {
    if (clickTracker.getLatestCPS(player) == CPS.EMPTY) {
      clickTracker.addNewCPS(player);
    }
  }
}

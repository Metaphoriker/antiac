package de.godcipher.antiac.tasks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.CheckRegistry;
import de.godcipher.antiac.detection.reliability.TPSChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Slf4j
public class CheckExecutionTask implements Runnable {

  private final ClickTracker clickTracker;
  private final CheckRegistry checkRegistry;
  private final TPSChecker tpsChecker;

  @Override
  public void run() {
    if (!isTPSReliable()) {
      return;
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      processPlayer(player);
    }

    cleanupOldCPS();
  }

  private boolean isTPSReliable() {
    boolean reliable = tpsChecker.isReliable();
    if (!reliable) {
      log.debug("TPS is not reliable, skipping check execution");
    }
    return reliable;
  }

  private void processPlayer(Player player) {
    UUID playerId = player.getUniqueId();
    ensureFirstCPS(playerId);
    checkRegistry.performChecks(player);
    clickTracker.addNewCPS(playerId);
  }

  private void ensureFirstCPS(UUID playerId) {
    if (clickTracker.getLatestCPS(playerId) == CPS.EMPTY) {
      clickTracker.addNewCPS(playerId);
    }
  }

  private void cleanupOldCPS() {
    clickTracker.removeLastCPSIfExceedsLimit();
  }
}

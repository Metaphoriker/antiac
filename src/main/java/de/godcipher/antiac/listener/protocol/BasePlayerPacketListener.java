package de.godcipher.antiac.listener.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.click.ClickType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Slf4j
public abstract class BasePlayerPacketListener implements PacketListener {

  protected final ClickTracker clickTracker;

  protected void handleClick(Player player, ClickType clickType) {
    long delay = calculateDelay(player, clickType);
    clickTracker.addClick(player.getUniqueId(), new Click(now(), delay, clickType));
    log.debug("Player: {} - Click registered with delay: {}", player.getName(), delay);
  }

  private long now() {
    return System.currentTimeMillis();
  }

  private long calculateDelay(Player player, ClickType clickType) {
    Click lastClick = findLastValidClick(player);
    long delay = calculateDelay(now(), lastClick);
    if (delay > 0) {
      log.debug("Player: {} - Delay: {}ms - ClickType: {}", player.getName(), delay, clickType);
    }
    return delay;
  }

  private long calculateDelay(long now, Click lastClick) {
    if (lastClick == null || lastClick.equals(Click.EMPTY)) {
      return 0;
    }
    return now - lastClick.getTime();
  }

  /**
   * Iterates through the CPS history to find the most recent valid click.
   *
   * @param player The player for whom we are checking the CPS history.
   * @return The last valid click or null if no valid click is found.
   */
  private Click findLastValidClick(Player player) {
    List<CPS> cpsHistory = clickTracker.getCPSList(player.getUniqueId());
    if (cpsHistory == null) return null;

    return findLastValidClickInHistory(cpsHistory);
  }

  /**
   * Finds the most recent valid click in the given CPS history.
   *
   * @param cpsHistory The CPS history list.
   * @return The last valid click or null if no valid click is found.
   */
  private Click findLastValidClickInHistory(List<CPS> cpsHistory) {
    for (int i = cpsHistory.size() - 1; i >= 0; i--) {
      CPS cps = cpsHistory.get(i);
      if (cps != null && !cps.isEmpty()) {
        return cps.getLastClick();
      }
    }
    return null;
  }
}

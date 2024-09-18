package de.godcipher.antiac.listener.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
@Slf4j
public abstract class BasePlayerPacketListener implements PacketListener {

  protected final ClickTracker clickTracker;

  protected void handleClick(Player player) {
    long now = System.currentTimeMillis();

    Click lastClick = findLastValidClick(player);

    long delay;

    if (lastClick == null || lastClick.equals(Click.EMPTY)) {
      delay = 0;
    } else {
      delay = now - lastClick.getTime();
    }

    clickTracker.addClick(player.getUniqueId(), new Click(now, delay));
    log.debug("Player: {} - Click registered with delay: {}", player.getName(), delay);
  }

  /**
   * Iterates through the CPS history to find the most recent valid click.
   *
   * @param player The player for whom we are checking the CPS history.
   * @return The last valid click or null if no valid click is found.
   */
  private Click findLastValidClick(Player player) {
    List<CPS> cpsHistory = clickTracker.getCPSList(player.getUniqueId());

    if (cpsHistory == null) {
      return null;
    }

    for (int i = cpsHistory.size() - 1; i >= 0; i--) {
      CPS cps = cpsHistory.get(i);
      if (cps != null && !cps.isEmpty()) {
        return cps.getLastClick();
      }
    }

    return null;
  }
}

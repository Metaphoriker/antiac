package de.godcipher.antiac.listener.bukkit;

import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.CheckRegistry;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

  private final ClickTracker clickTracker;
  private final CheckRegistry checkRegistry;

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    clickTracker.removePlayer(event.getPlayer().getUniqueId());
    checkRegistry.getChecks().forEach(check -> check.handlePlayerQuit(event.getPlayer()));
  }
}

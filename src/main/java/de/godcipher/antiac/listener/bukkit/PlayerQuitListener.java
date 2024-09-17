package de.godcipher.antiac.listener.bukkit;

import de.godcipher.antiac.click.ClickTracker;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

  private final ClickTracker clickTracker;

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    clickTracker.removePlayer(event.getPlayer().getUniqueId());
  }
}

package de.godcipher.antiac.listener.bukkit;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.event.PlayerFlaggedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class PlayerFlaggedListener implements Listener {

  private final Configuration configuration;

  @EventHandler
  public void onFlag(PlayerFlaggedEvent event) {
    List<String> commands = (List<String>) configuration.getConfigOption("commands").getValue();

    if (commands.isEmpty()) {
      return;
    }

    commands.forEach(
        command -> {
          command = command.replace("%player%", event.getPlayer().getName());
          command = command.replace("%check%", event.getCheck().getName());
          String finalCommand = command;
          Bukkit.getScheduler()
              .runTask(
                  AntiAC.getInstance(),
                  () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
        });
  }
}

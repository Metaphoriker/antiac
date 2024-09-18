package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.event.PlayerFlaggedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class FlagHandler {

  private final ClickTracker clickTracker;
  private final Check check;

  public void flagPlayer(Player player) {
    BStatsHandler.increaseFlagged();
    Bukkit.getScheduler()
        .runTask(
            AntiAC.getInstance(),
            () ->
                Bukkit.getPluginManager()
                    .callEvent(
                        new PlayerFlaggedEvent(
                            player, clickTracker.getCPSList(player.getUniqueId()), check)));

    handleFlag(player);
  }

  public void handleFlag(Player player) {
    List<String> commands =
        (List<String>)
            AntiAC.getInstance().getConfiguration().getConfigOption("commands").getValue();

    if (commands.isEmpty()) {
      return;
    }

    commands.forEach(
        command -> {
          command = command.replace("%player%", player.getName());
          command = command.replace("%check%", check.getName());
          String finalCommand = command;
          Bukkit.getScheduler()
              .runTask(
                  AntiAC.getInstance(),
                  () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
        });
  }
}

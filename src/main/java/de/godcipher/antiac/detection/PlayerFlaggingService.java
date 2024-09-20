package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.event.PlayerFlaggedEvent;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import de.godcipher.antiac.hibernate.repository.impl.LogEntryRepositoryImpl;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlayerFlaggingService {

  private final LogEntryRepositoryImpl logEntryRepository = new LogEntryRepositoryImpl();

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

    if (isLoggingActivated()) logFlag(player);
  }

  public void handleFlag(Player player) {
    List<String> commands =
        AntiAC.getInstance().getConfiguration().getConfigOption("commands").asStringList();

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

  private boolean isLoggingActivated() {
    return AntiAC.getInstance().getConfiguration().getConfigOption("logging").asBoolean();
  }

  private void logFlag(Player player) {
    UUID playerUuid = player.getUniqueId();
    String checkName = check.getName();
    LogEntry logEntry = new LogEntry(playerUuid, checkName);
    Bukkit.getScheduler()
        .runTaskAsynchronously(AntiAC.getInstance(), () -> logEntryRepository.save(logEntry));
  }
}

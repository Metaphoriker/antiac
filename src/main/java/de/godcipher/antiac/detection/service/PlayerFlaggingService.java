package de.godcipher.antiac.detection.service;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.click.ClickType;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.event.PlayerFlaggedEvent;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PlayerFlaggingService {

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
    CPS latestCPS = clickTracker.getLatestCPS(playerUuid);
    LogEntry logEntry =
        new LogEntry(playerUuid, checkName, latestCPS.getCPS(), getAverageClickType(latestCPS));
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AntiAC.getInstance(),
            () -> AntiAC.getInstance().getLogEntryRepository().save(logEntry));
  }

  private ClickType getAverageClickType(CPS cps) {
    List<ClickType> clickTypes = new ArrayList<>();
    cps.getClicks().stream().map(Click::getClickType).forEach(clickTypes::add);

    return clickTypes.stream()
        .max(
            (clickType1, clickType2) -> {
              int count1 = (int) clickTypes.stream().filter(type -> type == clickType1).count();
              int count2 = (int) clickTypes.stream().filter(type -> type == clickType2).count();
              return Integer.compare(count1, count2);
            })
        .orElse(null);
  }
}

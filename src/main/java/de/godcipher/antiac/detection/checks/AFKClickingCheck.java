package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** AfkClickingCheck checks for players that are clicking while being AFK. */
@Slf4j
public class AFKClickingCheck extends Check {

  private static final String AFK_AFTER_SECONDS_CONFIG = "afk-after-seconds";

  private final Map<UUID, List<Location>> playerLocations = new HashMap<>();
  private final Map<UUID, Long> afkMap = new HashMap<>();

  private int afkAfterSeconds = 10;

  public AFKClickingCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  public void handlePlayerQuit(Player player) {
    playerLocations.remove(player.getUniqueId());
    afkMap.remove(player.getUniqueId());
  }

  @Override
  protected void onLoad() {
    setupDefaults();
    setConfigValue();
  }

  private void setConfigValue() {
    afkAfterSeconds = getCheckConfiguration().getConfigOption(AFK_AFTER_SECONDS_CONFIG).asInteger();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    List<Location> locations =
        playerLocations.computeIfAbsent(
            player.getUniqueId(), k -> new ArrayList<>(List.of(player.getLocation())));
    updateLocations(player, locations);
    updateAfkMap(player, locations);
    return isAfkClicking(locations) && isClicking(player, afkAfterSeconds);
  }

  private void updateLocations(Player player, List<Location> locations) {
    if (locations.size() < afkAfterSeconds) {
      locations.add(player.getLocation());
    } else if (locations.size() > afkAfterSeconds) {
      locations.removeFirst();
    }
  }

  private void updateAfkMap(Player player, List<Location> locations) {
    if (!isEnoughDataCollected(locations)) return;
    if (isAfkClicking(locations)) {
      afkMap.put(player.getUniqueId(), System.currentTimeMillis());
    } else {
      afkMap.remove(player.getUniqueId());
    }
  }

  private boolean isEnoughDataCollected(List<Location> locations) {
    return locations.size() >= afkAfterSeconds;
  }

  private boolean isAfkClicking(List<Location> locations) {
    Location firstLocation = locations.getFirst();
    return locations.stream().allMatch(location -> location.equals(firstLocation));
  }

  private boolean isClicking(Player player, int span) {
    List<CPS> cpsList = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> cpsToProcess = trimList(cpsList, span);

    if (cpsToProcess.size() < span) {
      return false;
    }

    return cpsToProcess.stream()
        .anyMatch(
            cps ->
                cps.getLastClick().getTime() > afkMap.get(player.getUniqueId())
                    && !cps.isEmpty()
                    && cps.getCPS() > 0);
  }

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(
            AFK_AFTER_SECONDS_CONFIG,
            new ConfigurationOption<>(10, "Number of seconds before a player is considered AFK"));
  }
}

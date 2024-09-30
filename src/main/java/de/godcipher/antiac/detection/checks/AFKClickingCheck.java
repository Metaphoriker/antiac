package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.checks.configs.AFKClickingCheckConfig;
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
public class AFKClickingCheck extends Check<AFKClickingCheckConfig> {

  private final Map<UUID, List<Location>> playerLocations = new HashMap<>();
  private final Map<UUID, Long> afkMap = new HashMap<>();

  public AFKClickingCheck(ClickTracker clickTracker) {
    super(clickTracker, new AFKClickingCheckConfig());
  }

  @Override
  public void handlePlayerQuit(Player player) {
    playerLocations.remove(player.getUniqueId());
    afkMap.remove(player.getUniqueId());
  }

  @Override
  public boolean check(Player player) {
    List<Location> locations =
        playerLocations.computeIfAbsent(
            player.getUniqueId(), k -> new ArrayList<>(List.of(player.getLocation())));
    updateLocations(player, locations);
    updateAfkMap(player, locations);
    return isAfkClicking(locations) && isClicking(player);
  }

  private void updateLocations(Player player, List<Location> locations) {
    if (locations.size() < getConfiguration().getAfkAfterSeconds()) {
      locations.add(player.getLocation());
    } else if (locations.size() > getConfiguration().getAfkAfterSeconds()) {
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
    return locations.size() >= getConfiguration().getAfkAfterSeconds();
  }

  private boolean isAfkClicking(List<Location> locations) {
    Location firstLocation = locations.getFirst();
    return locations.stream().allMatch(location -> location.equals(firstLocation));
  }

  private boolean isClicking(Player player) {
    List<CPS> cpsList = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> cpsToProcess = trimList(cpsList, getConfiguration().getAfkAfterSeconds());

    if (cpsToProcess.size() < getConfiguration().getAfkAfterSeconds()) {
      return false;
    }

    return cpsToProcess.stream()
        .anyMatch(
            cps ->
                cps.getLastClick().getTime() > afkMap.get(player.getUniqueId())
                    && !cps.isEmpty()
                    && cps.getCPS() > 0);
  }
}

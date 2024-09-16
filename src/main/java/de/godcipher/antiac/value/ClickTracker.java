package de.godcipher.antiac.value;

import de.godcipher.antiac.config.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClickTracker {

  private final Map<UUID, List<CPS>> playerClicksMap = new HashMap<>();

  private final Configuration configuration;

  public synchronized void addClick(UUID player, Click click) {
    ensurePlayer(player);
    CPS currentCPS = getLatestCPS(player);
    currentCPS.addClick(click);
  }

  public synchronized void addNewCPS(UUID player) {
    ensurePlayer(player);
    playerClicksMap.get(player).add(new CPS());
  }

  public synchronized void removePlayer(UUID player) {
    playerClicksMap.remove(player);
  }

  private void ensurePlayer(UUID player) {
    if (!playerClicksMap.containsKey(player)) {
      playerClicksMap.put(player, new ArrayList<>());
    }
  }

  public synchronized List<CPS> getCPSList(UUID player) {
    return playerClicksMap.get(player);
  }

  public synchronized CPS getLatestCPS(UUID player) {
    List<CPS> cpsSet = playerClicksMap.get(player);
    return cpsSet == null || cpsSet.isEmpty() ? CPS.EMPTY : cpsSet.getLast();
  }

  public void removeLastCPSIfExceedsLimit() {
    for (var cpsSet : playerClicksMap.values()) {
      if (cpsSet.size() > ((Integer) configuration.getConfigOption("max-cps").getValue())) {
        cpsSet.removeFirst();
      }
    }
  }
}

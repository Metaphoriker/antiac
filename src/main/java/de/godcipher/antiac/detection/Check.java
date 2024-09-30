package de.godcipher.antiac.detection;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.service.PlayerFlaggingService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@Slf4j
public abstract class Check<T extends CheckConfiguration> {

  @Getter private final String name;

  private final PlayerFlaggingService playerFlaggingService;
  protected final ClickTracker clickTracker;
  @Getter protected final T configuration;

  @Getter private boolean loaded;

  protected Check(ClickTracker clickTracker, T config) {
    this.name = this.getClass().getSimpleName();
    this.clickTracker = clickTracker;
    this.playerFlaggingService = new PlayerFlaggingService(clickTracker, this);
    this.configuration = config;
    this.configuration.initialize();
  }

  public void handlePlayerQuit(Player player) {}

  protected void onLoad() {}

  protected void onUnload() {}

  public boolean isActivated() {
    return configuration.isActivated();
  }

  public void setActivated(boolean activated) {
    configuration.setActivated(activated);
    configuration.saveConfiguration();
  }

  void load() {
    if (canLoad()) {
      loaded = true;
      onLoad();
      configuration.reloadConfig();
    }
  }

  private boolean canLoad() {
    if (isLoaded() || !isActivated()) {
      log.error("Failed loading {}", name);
      return false;
    }
    return true;
  }

  void unload() {
    if (canUnload()) {
      loaded = false;
      onUnload();
      configuration.saveConfiguration();
    }
  }

  private boolean canUnload() {
    if (!isLoaded()) {
      log.error("Failed unloading {}", name);
      return false;
    }
    return true;
  }

  protected List<CPS> trimList(List<CPS> set, int size) {
    return set.stream().limit(size).collect(Collectors.toList());
  }

  protected final void onFlag(Player player) {
    log.debug("Player: {} - Flagged by {}", player.getName(), name);
    playerFlaggingService.flagPlayer(player);
  }

  public abstract boolean check(Player player);
}

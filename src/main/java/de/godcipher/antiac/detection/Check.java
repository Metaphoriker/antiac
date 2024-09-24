package de.godcipher.antiac.detection;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.service.PlayerFlaggingService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@Slf4j
public abstract class Check {

  @Getter private final String name;
  @Getter private final CheckConfiguration checkConfiguration;

  private final PlayerFlaggingService playerFlaggingService;

  protected final ClickTracker clickTracker;

  @Getter private boolean activated;
  @Getter private boolean loaded;

  protected Check(ClickTracker clickTracker) {
    this.name = this.getClass().getSimpleName();
    this.clickTracker = clickTracker;
    this.checkConfiguration = new CheckConfiguration(name);
    this.playerFlaggingService = new PlayerFlaggingService(clickTracker, this);
    this.activated = checkConfiguration.isActivated();
  }

  // For cleanup when a player quits
  public void handlePlayerQuit(Player player) {}

  public void setActivated(boolean activated) {
    this.activated = activated;
    updateConfigurationOption("activated", activated);
  }

  protected void updateConfigurationOption(String key, boolean value) {
    checkConfiguration.setConfigOption(key, new ConfigurationOption<>(value, getComment(key)));
  }

  private String getComment(String key) {
    return checkConfiguration.getConfigOption(key).getComment();
  }

  void load() {
    if (canLoad()) {
      loaded = true;
      onLoad();
      checkConfiguration.loadConfig();
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
      checkConfiguration.saveConfiguration();
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

  protected abstract void onLoad();

  protected abstract void onUnload();

  public abstract boolean check(Player player);
}

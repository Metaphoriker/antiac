package de.godcipher.antiac;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.util.TimeStampMode;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.CheckRegistry;
import de.godcipher.antiac.detection.checks.AfkClickingCheck;
import de.godcipher.antiac.detection.checks.ClickDelaySpanCheck;
import de.godcipher.antiac.detection.checks.ClickLimitCheck;
import de.godcipher.antiac.detection.checks.DoubleClickCheck;
import de.godcipher.antiac.detection.checks.MomentumCheck;
import de.godcipher.antiac.detection.reliability.TPSChecker;
import de.godcipher.antiac.listener.bukkit.PlayerFlaggedListener;
import de.godcipher.antiac.listener.bukkit.PlayerQuitListener;
import de.godcipher.antiac.listener.protocol.PlayerDiggingPacketListener;
import de.godcipher.antiac.listener.protocol.PlayerInteractWithEntityPacketListener;
import de.godcipher.antiac.scheduler.CheckExecutionScheduler;
import de.godcipher.antiac.value.ClickTracker;
import de.godcipher.antiac.value.ClickType;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
public final class AntiAC extends JavaPlugin {

  @Getter private static AntiAC instance;

  private final Configuration configuration = new Configuration();
  private final TPSChecker tpsChecker = new TPSChecker();

  @Getter private final CheckRegistry checkRegistry = new CheckRegistry();
  @Getter private final ClickTracker clickTracker = new ClickTracker(configuration);

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    instance = this;

    loadConfig();
    initializeBStats();
    startTPSChecker();
    setupPacketEvents();
    registerChecks();
    registerBukkitListener();
    registerPacketListener();
    runCheckExecutionScheduler();
    printRegisteredChecksAmount();
  }

  @Override
  public void onDisable() {}

  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();

    configuration.setupFile("config.yml", null);

    loadConfigValues();
  }

  private void loadConfigValues() {
    configuration.addConfigOption(
        "max-cps", new ConfigurationOption<>(30, "Stores the last x CPS internally to process"));
    configuration.addConfigOption(
        "allowed-clicktypes",
        ConfigurationOption.ofStringList(
            Arrays.stream(ClickType.values()).map(Enum::name).collect(Collectors.toList()),
            "What click types should AntiAC track?"));
    configuration.addConfigOption(
        "log-to-database", new ConfigurationOption<>(false, "Enable logging to database"));
    configuration.addConfigOption("database-url", new ConfigurationOption<>("", "Database URL"));
    configuration.addConfigOption(
        "database-username", new ConfigurationOption<>("", "Database username"));
    configuration.addConfigOption(
        "database-password", new ConfigurationOption<>("", "Database password"));
    configuration.addConfigOption(
        "commands",
        ConfigurationOption.ofStringList(
            List.of("kick %player%", "say %player% got flagged by %check% check!"),
            "Commands to execute when a player gets flagged"));
    configuration.addConfigOption(
        "tps-protection",
        new ConfigurationOption<>(15, "Lowest allowed TPS until the TPS protection kicks in"));
    configuration.addConfigOption(
        "violations", new ConfigurationOption<>(true, "Enable violation-based actions"));
    configuration.addConfigOption(
        "max-violations",
        new ConfigurationOption<>(
            20, "Maximum amount of violations until the player gets flagged"));
    configuration.addConfigOption(
        "clear-violations-after",
        new ConfigurationOption<>(5, "Clear violations of a player after x minutes"));
    configuration.addConfigOption(
        "bedrock-players",
        new ConfigurationOption<>(false, "Whether the server allows bedrock players"));
    configuration.loadConfig();
  }

  private void initializeBStats() {
    BStatsHandler.init(this);
  }

  private void startTPSChecker() {
    tpsChecker.start();
  }

  private void printRegisteredChecksAmount() {
    log.info("Registered {} checks", checkRegistry.getChecks().size());
  }

  private void runCheckExecutionScheduler() {
    getServer()
        .getScheduler()
        .runTaskTimerAsynchronously(
            this, new CheckExecutionScheduler(clickTracker, checkRegistry, tpsChecker), 0, 20);
  }

  private void registerChecks() {
    checkRegistry.registerCheck(new AfkClickingCheck(clickTracker));
    checkRegistry.registerCheck(new ClickDelaySpanCheck(clickTracker));
    checkRegistry.registerCheck(new ClickLimitCheck(clickTracker));
    // checkRegistry.registerCheck(new CPSSpanCheck(clickTracker));
    checkRegistry.registerCheck(new DoubleClickCheck(clickTracker));
    checkRegistry.registerCheck(new MomentumCheck(clickTracker));
  }

  private void registerBukkitListener() {
    getServer().getPluginManager().registerEvents(new PlayerQuitListener(clickTracker), this);
    getServer().getPluginManager().registerEvents(new PlayerFlaggedListener(configuration), this);
  }

  private void setupPacketEvents() {
    PacketEvents.getAPI()
        .getSettings()
        .debug(false)
        .checkForUpdates(true)
        .timeStampMode(TimeStampMode.MILLIS)
        .reEncodeByDefault(true);
    PacketEvents.getAPI().init();
  }

  private void registerPacketListener() {
    EventManager eventManager = PacketEvents.getAPI().getEventManager();
    // eventManager.registerListener(
    //    new PlayerInteractWithBlockPacketListener(clickTracker), PacketListenerPriority.NORMAL);
    // TODO: double click bug
    eventManager.registerListener(
        new PlayerInteractWithEntityPacketListener(clickTracker), PacketListenerPriority.NORMAL);
    eventManager.registerListener(
        new PlayerDiggingPacketListener(clickTracker), PacketListenerPriority.NORMAL);
  }
}

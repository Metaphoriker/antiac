package de.godcipher.antiac;

import co.aikar.commands.PaperCommandManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.util.TimeStampMode;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.click.ClickType;
import de.godcipher.antiac.commands.AntiACCommand;
import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.CheckRegistry;
import de.godcipher.antiac.detection.checks.AFKClickingCheck;
import de.godcipher.antiac.detection.checks.ClickLimitCheck;
import de.godcipher.antiac.detection.checks.ClickPatternConsistencyCheck;
import de.godcipher.antiac.detection.checks.DoubleClickCheck;
import de.godcipher.antiac.detection.checks.MomentumCheck;
import de.godcipher.antiac.detection.checks.ScaledCPSCheck;
import de.godcipher.antiac.detection.reliability.TPSChecker;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import de.godcipher.antiac.hibernate.HibernateUtil;
import de.godcipher.antiac.hibernate.repository.impl.LogEntryRepositoryImpl;
import de.godcipher.antiac.listener.bukkit.PlayerQuitListener;
import de.godcipher.antiac.listener.protocol.PlayerAttackEntityPacketListener;
import de.godcipher.antiac.messages.Messages;
import de.godcipher.antiac.tasks.CheckExecutionTask;
import de.godcipher.antiac.tasks.ClearViolationsTask;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hibernate.Hibernate;

@Slf4j
public final class AntiAC extends JavaPlugin {

  private static final String SPIGOT_RESOURCE_ID = "74933";

  @Getter private static AntiAC instance;

  @Getter
  private final Configuration configuration =
      new Configuration(); // do we really want to share this?

  @Getter
  private final LogEntryRepositoryImpl logEntryRepository =
      new LogEntryRepositoryImpl(); // +1 TODO: use interface

  private final TPSChecker tpsChecker = new TPSChecker(configuration);

  @Getter private final ClickTracker clickTracker = new ClickTracker(configuration);
  @Getter private final ViolationTracker violationTracker = new ViolationTracker();

  @Getter
  private final CheckRegistry checkRegistry = new CheckRegistry(violationTracker, configuration);

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    instance = this;

    loadConfig();
    registerChecks();

    setupHibernate();
    setupMessages();
    setupCommandFramework();
    setupPacketEvents();

    initializeBStats();

    startTPSChecker();

    registerBukkitListener();
    registerPacketListener();

    runTasks();
    printRegisteredChecksAmount();

    runUpdateChecker();
    logEntryRepository.startCacheUpdater();
  }

  @Override
  public void onDisable() {
    logEntryRepository.shutdownCacheUpdater();
    HibernateUtil.shutdown();
  }

  private void runUpdateChecker() {
    new UpdateChecker(this, UpdateCheckSource.SPIGOT, SPIGOT_RESOURCE_ID)
        .checkEveryXHours(12)
        .setColoredConsoleOutput(true)
        .checkNow();
  }

  private void setupHibernate() {
    if (getConfiguration().getConfigOption("logging").asBoolean()) HibernateUtil.setupHibernate();
  }

  private void setupMessages() {
    Messages.setup();
    Messages.migrate();
  }

  private void setupCommandFramework() {
    PaperCommandManager commandManager = new PaperCommandManager(this);
    registerCommandCompletions(commandManager);
    commandManager.registerCommand(
        new AntiACCommand(clickTracker, violationTracker, logEntryRepository, configuration));
  }

  private void stopAllTasks() {
    Bukkit.getScheduler().cancelTasks(this);
  }

  private void registerCommandCompletions(PaperCommandManager commandManager) {
    commandManager
        .getCommandCompletions()
        .registerCompletion(
            "checks",
            completionHandler ->
                checkRegistry.getChecks().stream()
                    .map(Check::getName)
                    .collect(Collectors.toCollection(List::of)));
  }

  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();

    configuration.setupFile("config.yml", null);

    loadConfigValues();
  }

  public void reload() {
    configuration.reloadConfig();
    loadConfigValues();
    Messages.setup();
    clearChecks();
    registerChecks();
    reloadScheduler();
  }

  private void reloadScheduler() {
    log.debug("AntiAC is reloading...");

    new BukkitRunnable() {
      RebootStep step = RebootStep.DEACTIVATING;

      @Override
      public void run() {
        switch (step) {
          case DEACTIVATING:
            step = RebootStep.ACTIVATING;
            Bukkit.getPluginManager().disablePlugin(AntiAC.this);
            break;
          case ACTIVATING:
            stopAllTasks();
            step = null;
            Bukkit.getPluginManager().enablePlugin(AntiAC.this);
            cancel();
            break;
        }
      }
    }.runTaskLater(this, 20 * 5);
  }

  private enum RebootStep {
    ACTIVATING,
    DEACTIVATING,
  }

  private void loadConfigValues() {
    configuration.setConfigOption(
        "cps-storage-limit",
        new ConfigurationOption<>(30, "Stores the last x CPS internally to process"));
    List<String> clickTypes =
        Arrays.stream(ClickType.values()).map(Enum::name).collect(Collectors.toList());
    configuration.setConfigOption(
        "allowed-clicktypes",
        new ConfigurationOption<>(
            clickTypes, "What click types should AntiAC track? " + clickTypes));
    configuration.setConfigOption(
        "modern-feedback", new ConfigurationOption<>(true, "Enable modern feedback"));
    configuration.setConfigOption(
        "logging", new ConfigurationOption<>(false, "Whether to log flagged players"));
    configuration.setConfigOption("database-url", new ConfigurationOption<>("", "Database URL"));
    configuration.setConfigOption(
        "database-username", new ConfigurationOption<>("", "Database username"));
    configuration.setConfigOption(
        "database-password", new ConfigurationOption<>("", "Database password"));
    configuration.setConfigOption(
        "database-driver",
        new ConfigurationOption<>("com.mysql.cj.jdbc.Driver", "Database driver"));
    configuration.setConfigOption(
        "commands",
        ConfigurationOption.ofStringList(
            List.of("kick %player%", "say %player% got flagged by %check% check!"),
            "Commands to execute when a player gets flagged"));
    configuration.setConfigOption(
        "tps-protection",
        new ConfigurationOption<>(15, "Lowest allowed TPS until the TPS protection kicks in"));
    configuration.setConfigOption(
        "violations", new ConfigurationOption<>(true, "Enable violation-based actions"));
    configuration.setConfigOption(
        "max-allowed-violations",
        new ConfigurationOption<>(
            8, "Maximum amount of violations allowed until the player gets flagged"));
    configuration.setConfigOption(
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

  private void runTasks() {
    getServer()
        .getScheduler()
        .runTaskTimerAsynchronously(
            this, new CheckExecutionTask(clickTracker, checkRegistry, tpsChecker), 0, 20);

    // TODO: async needed?
    getServer()
        .getScheduler()
        .runTaskTimerAsynchronously(
            this, new ClearViolationsTask(violationTracker), 0, 20 * 60); // 1 minute
  }

  private void registerChecks() {
    checkRegistry.registerCheck(new AFKClickingCheck(clickTracker));
    checkRegistry.registerCheck(new ClickPatternConsistencyCheck(clickTracker));
    checkRegistry.registerCheck(new ClickLimitCheck(clickTracker));
    checkRegistry.registerCheck(new DoubleClickCheck(clickTracker));
    checkRegistry.registerCheck(new MomentumCheck(clickTracker));
    checkRegistry.registerCheck(new ScaledCPSCheck(clickTracker));
  }

  private void clearChecks() {
    checkRegistry.clearChecks();
  }

  private void registerBukkitListener() {
    getServer()
        .getPluginManager()
        .registerEvents(new PlayerQuitListener(clickTracker, checkRegistry), this);
  }

  private void setupPacketEvents() {
    PacketEvents.getAPI()
        .getSettings()
        .debug(false)
        .checkForUpdates(false)
        .timeStampMode(TimeStampMode.MILLIS)
        .reEncodeByDefault(true);
    PacketEvents.getAPI().init();
  }

  private void registerPacketListener() {
    EventManager eventManager = PacketEvents.getAPI().getEventManager();
    /* TODO: packets are thrown multiple times at once, therefore false positives
       eventManager.registerListener(
           new PlayerInteractWithBlockPacketListener(clickTracker), PacketListenerPriority.NORMAL);
       eventManager.registerListener(
               new PlayerDiggingPacketListener(clickTracker), PacketListenerPriority.NORMAL);
    */
    eventManager.registerListener(
        new PlayerAttackEntityPacketListener(clickTracker), PacketListenerPriority.NORMAL);
  }
}

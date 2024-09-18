package de.godcipher.antiac;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.util.TimeStampMode;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.click.ClickType;
import de.godcipher.antiac.commands.AntiACCommand;
import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.CheckRegistry;
import de.godcipher.antiac.detection.checks.AfkClickingCheck;
import de.godcipher.antiac.detection.checks.ClickDelaySpanCheck;
import de.godcipher.antiac.detection.checks.ClickLimitCheck;
import de.godcipher.antiac.detection.checks.DoubleClickCheck;
import de.godcipher.antiac.detection.checks.MomentumCheck;
import de.godcipher.antiac.detection.reliability.TPSChecker;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import de.godcipher.antiac.listener.bukkit.PlayerFlaggedListener;
import de.godcipher.antiac.listener.bukkit.PlayerQuitListener;
import de.godcipher.antiac.listener.protocol.PlayerDiggingPacketListener;
import de.godcipher.antiac.listener.protocol.PlayerInteractWithEntityPacketListener;
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

@Slf4j
public final class AntiAC extends JavaPlugin {

  @Getter private static AntiAC instance;

  private final Configuration configuration = new Configuration();
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

    setupMessages();
    loadConfig();
    initializeBStats();
    startTPSChecker();
    setupPacketEvents();
    registerChecks();
    registerCommands();
    registerBukkitListener();
    registerPacketListener();
    runTasks();
    printRegisteredChecksAmount();
  }

  @Override
  public void onDisable() {}

  private void setupMessages() {
    Messages.setup();
    Messages.migrate();
  }

  private void registerCommands() {
    Bukkit.getPluginCommand("antiac").setExecutor(new AntiACCommand(clickTracker));
  }

  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();

    configuration.setupFile("config.yml", null);

    loadConfigValues();
  }

  private void loadConfigValues() {
    configuration.addConfigOption(
        "cps-storage-limit",
        new ConfigurationOption<>(30, "Stores the last x CPS internally to process"));
    List<String> clickTypes =
        Arrays.stream(ClickType.values()).map(Enum::name).collect(Collectors.toList());
    configuration.addConfigOption(
        "allowed-clicktypes",
        new ConfigurationOption<>(
            clickTypes, "What click types should AntiAC track? " + clickTypes));
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
        "max-allowed-violations",
        new ConfigurationOption<>(
            8, "Maximum amount of violations allowed until the player gets flagged"));
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

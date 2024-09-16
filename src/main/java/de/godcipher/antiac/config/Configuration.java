package de.godcipher.antiac.config;

import de.godcipher.antiac.AntiAC;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

@Slf4j
public class Configuration {

  private final Map<String, ConfigurationOption<?>> configOptions = new LinkedHashMap<>();
  private File file;
  private FileConfiguration config;

  public void setupFile(String fileName, String filePath) {
    if (filePath != null) {
      File directory = new File(AntiAC.getInstance().getDataFolder(), filePath);
      if (!directory.exists()) {
        directory.mkdirs();
      }
      this.file = new File(directory, fileName);
    } else {
      this.file = new File(AntiAC.getInstance().getDataFolder(), fileName);
    }
    this.config = YamlConfiguration.loadConfiguration(file);
    createFiles();
  }

  public void addConfigOption(String key, ConfigurationOption<?> option) {
    configOptions.put(key, option);
  }

  public void addSpacer() {
    configOptions.put("", new ConfigurationOption<>(null, ""));
  }

  public ConfigurationOption<?> getConfigOption(String key) {
    return configOptions.get(key);
  }

  public void loadConfig() {
    config.options().copyDefaults(true);
    generateAndMergeConfig();
    loadFromConfig();
  }

  private void generateAndMergeConfig() {
    try (PrintWriter writer = new PrintWriter(file)) {
      writer.println("# Config version: " + AntiAC.getInstance().getDescription().getVersion());
      writer.println("# Configuration settings:\n");

      for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
        writeConfigOption(writer, entry.getKey(), entry.getValue());
      }

    } catch (IOException e) {
      log.error("Could not generate configuration file: {}", file.getName(), e);
    }
  }

  private void writeConfigOption(PrintWriter writer, String key, ConfigurationOption<?> option) {
    String comment = option.getComment();
    if (comment != null && !comment.isEmpty()) {
      writer.println("# " + comment);
    }

    Object value = config.contains(key) ? config.get(key) : option.getValue();
    if (!key.isEmpty()) {
      writer.printf("%s: %s%n", key, value);
    }
    writer.println();
  }

  private void loadFromConfig() {
    for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
      String key = entry.getKey();
      ConfigurationOption<?> option = entry.getValue();
      if (config.contains(key)) {
        Object newValue = config.get(key);
        entry.setValue(new ConfigurationOption<>(newValue, option.getComment()));
      }
    }
  }

  public void saveConfiguration() {
    try {
      config.save(file);
    } catch (IOException e) {
      log.error("Could not save configuration file: {}", file.getName(), e);
    }
  }

  private void createFiles() {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        log.error("Could not create configuration file: {}", file.getName(), e);
      }
    }
  }
}

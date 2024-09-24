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
    createDirectoryIfNotExists(filePath);
    this.file = createFile(fileName, filePath);
    this.config = YamlConfiguration.loadConfiguration(file);
    createFileIfNotExists();
  }

  public void setConfigOption(String key, ConfigurationOption<?> option) {
    configOptions.put(key, option);
  }

  public ConfigurationOption<?> getConfigOption(String key) {
    return configOptions.get(key);
  }

  public void reloadConfig() {
    config = YamlConfiguration.loadConfiguration(file);
    loadFromConfig();
  }

  public void loadConfig() {
    config.options().copyDefaults(true);
    generateAndMergeConfig();
    loadFromConfig();
  }

  public void saveConfiguration() {
    try {
      config.save(file);
    } catch (IOException e) {
      log.error("Could not save configuration file: {}", file.getName(), e);
    }
  }

  private void createDirectoryIfNotExists(String filePath) {
    if (filePath != null) {
      File directory = new File(AntiAC.getInstance().getDataFolder(), filePath);
      if (!directory.exists()) {
        directory.mkdirs();
      }
    }
  }

  private File createFile(String fileName, String filePath) {
    if (filePath != null) {
      return new File(new File(AntiAC.getInstance().getDataFolder(), filePath), fileName);
    } else {
      return new File(AntiAC.getInstance().getDataFolder(), fileName);
    }
  }

  private void generateAndMergeConfig() {
    try (PrintWriter writer = new PrintWriter(file)) {
      writeConfigHeader(writer);
      writeConfigOptions(writer);
    } catch (IOException e) {
      log.error("Could not generate configuration file: {}", file.getName(), e);
    }
  }

  private void writeConfigOptions(PrintWriter writer) {
    for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
      writeConfigOption(writer, entry.getKey(), entry.getValue());
    }
  }

  private static void writeConfigHeader(PrintWriter writer) {
    writer.println("# Config version: " + AntiAC.getInstance().getDescription().getVersion());
    writer.println("# Configuration settings:\n");
  }

  private void writeConfigOption(PrintWriter writer, String key, ConfigurationOption<?> option) {
    writeComment(writer, option);
    writeKeyAndValue(writer, key, option);
    writer.println();
  }

  private void writeKeyAndValue(PrintWriter writer, String key, ConfigurationOption<?> option) {
    Object value = config.contains(key) ? config.get(key) : option.getValue();
    if (!key.isEmpty()) {
      writer.printf("%s: %s%n", key, value);
    }
  }

  private static void writeComment(PrintWriter writer, ConfigurationOption<?> option) {
    String comment = option.getComment();
    if (comment != null && !comment.isEmpty()) {
      writer.println("# " + comment);
    }
  }

  private void loadFromConfig() {
    for (Map.Entry<String, ConfigurationOption<?>> entry : configOptions.entrySet()) {
      String key = entry.getKey();
      ConfigurationOption<?> option = entry.getValue();
      setNewValue(entry, key, option);
    }
  }

  private void setNewValue(
      Map.Entry<String, ConfigurationOption<?>> entry, String key, ConfigurationOption<?> option) {
    if (config.contains(key)) {
      Object newValue = config.get(key);
      entry.setValue(new ConfigurationOption<>(newValue, option.getComment()));
    }
  }

  private void createFileIfNotExists() {
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        log.error("Could not create configuration file: {}", file.getName(), e);
      }
    }
  }
}

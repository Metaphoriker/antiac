package de.godcipher.antiac.messages;

import de.godcipher.antiac.AntiAC;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Messages {

  private static final Properties PROPERTIES = new Properties();
  private static String propertiesFile;

  public static void setup() {
    propertiesFile = AntiAC.getInstance().getDataFolder() + File.separator + "messages.properties";
    createPropertiesFileIfNotExists();
    loadPropertiesFromFile();
    saveProperties();
  }

  private static void createPropertiesFileIfNotExists() {
    File file = new File(propertiesFile);
    if (!file.exists()) {
      try {
        ensureFile(file);
        Properties defaultProperties = loadMessagePropertiesFromResources();
        if (defaultProperties != null) {
          try (FileOutputStream out = new FileOutputStream(file)) {
            defaultProperties.store(out, null);
            log.info("Stored properties to file: {}", file.getAbsolutePath());
            PROPERTIES.putAll(defaultProperties);
          }
        }
      } catch (IOException e) {
        log.error("Could not create properties file: {}", propertiesFile, e);
      }
    }
  }

  private static Properties loadMessagePropertiesFromResources() {
    Properties tempProperties = new Properties();
    try (InputStream defaultPropertiesStream =
                 Messages.class.getResourceAsStream("/messages.properties")) {
      if (defaultPropertiesStream == null) {
        log.error("Resource /messages.properties not found.");
        return null;
      }
      tempProperties.load(defaultPropertiesStream);
      log.info("Loaded properties from resource: {}", tempProperties);
    } catch (IOException e) {
      log.error("Could not load messages.properties", e);
    }
    return tempProperties;
  }

  private static void ensureFile(File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    if (file.createNewFile()) {
      log.info("Created new properties file: {}", file.getAbsolutePath());
    }
  }

  private static void loadPropertiesFromFile() {
    File file = new File(propertiesFile);
    if (file.exists()) {
      try (FileInputStream in = new FileInputStream(propertiesFile)) {
        PROPERTIES.load(in);
        log.info("Loaded properties from file: {}", PROPERTIES);
      } catch (IOException e) {
        log.error("Could not load properties file: {}", propertiesFile, e);
      }
    } else {
      log.warn("Properties file does not exist: {}", propertiesFile);
    }
  }

  private static void saveProperties() {
    log.info("Saving properties to file: {}", propertiesFile);
    try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
      PROPERTIES.store(out, null);
      log.info("Saved properties to file: {}", propertiesFile);
    } catch (IOException e) {
      log.error("Could not save properties file: {}", propertiesFile, e);
    }
  }

  public static String getString(String key) {
    return PROPERTIES.getProperty(key);
  }

  public static void migrate() {
    Properties newProperties = loadMessagePropertiesFromResources();
    if (newProperties != null) {
      migrateProperties(newProperties);
      saveProperties();
    }
  }

  private static void migrateProperties(Properties newProperties) {
    PROPERTIES
            .keySet()
            .removeIf(key -> !newProperties.containsKey(key));
    newProperties.forEach(PROPERTIES::putIfAbsent);
  }
}
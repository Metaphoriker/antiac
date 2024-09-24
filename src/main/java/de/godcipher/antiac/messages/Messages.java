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
    loadProperties();
  }

  private static void createPropertiesFileIfNotExists() {
    File file = new File(propertiesFile);
    if (!file.exists()) {
      try {
        ensureFile(file);
        loadMessagePropertiesFromResources(PROPERTIES, file);
      } catch (IOException e) {
        log.error("Could not create properties file: {}", propertiesFile, e);
      }
    }
  }

  private static void loadMessagePropertiesFromResources(Properties properties, File file) {
    try (InputStream defaultPropertiesStream =
            Messages.class.getResourceAsStream("/messages.properties");
        FileOutputStream out = new FileOutputStream(file)) {
      if (defaultPropertiesStream != null) {
        PROPERTIES.load(defaultPropertiesStream);
        PROPERTIES.store(out, null);
      }
    } catch (IOException e) {
      log.error("Could not load messages.properties", e);
    }
  }

  private static void ensureFile(File file) throws IOException {
    file.getParentFile().mkdirs();
    file.createNewFile();
  }

  private static void loadProperties() {
    try (FileInputStream in = new FileInputStream(propertiesFile)) {
      PROPERTIES.load(in);
    } catch (IOException e) {
      log.error("Could not load properties file: {}", propertiesFile, e);
    }
  }

  private static void saveProperties() {
    try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
      PROPERTIES.store(out, null);
    } catch (IOException e) {
      log.error("Could not save properties file: {}", propertiesFile, e);
    }
  }

  public static String getString(String key) {
    return PROPERTIES.getProperty(key);
  }

  public static void migrate() {
    File file = new File(propertiesFile);
    Properties newProperties = new Properties();
    loadMessagePropertiesFromResources(newProperties, file);
    migrateProperties(newProperties);
    saveProperties();
  }

  private static void migrateProperties(Properties newProperties) {
    PROPERTIES.keySet().removeIf(key -> !newProperties.containsKey(key));
    newProperties.forEach((key, value) -> PROPERTIES.putIfAbsent(key, value));
  }
}

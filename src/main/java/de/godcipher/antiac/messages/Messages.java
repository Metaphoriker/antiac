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
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (InputStream defaultPropertiesStream =
                Messages.class.getResourceAsStream("/messages.properties");
            FileOutputStream out = new FileOutputStream(file)) {
          if (defaultPropertiesStream != null) {
            PROPERTIES.load(defaultPropertiesStream);
            PROPERTIES.store(out, null);
          }
        }
      } catch (IOException e) {
        log.error("Could not create properties file: {}", propertiesFile, e);
      }
    }
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

  public static void setString(String key, String value) {
    PROPERTIES.setProperty(key, value);
    saveProperties();
  }

  public static void removeString(String key) {
    PROPERTIES.remove(key);
    saveProperties();
  }

  public static void migrate() {
    Properties newProperties = new Properties();
    try (InputStream defaultPropertiesStream =
        Messages.class.getResourceAsStream("/messages.properties")) {
      if (defaultPropertiesStream != null) {
        newProperties.load(defaultPropertiesStream);
      }
    } catch (IOException e) {
      log.error("Could not load default properties file", e);
    }

    // Remove keys that are not in the new properties
    PROPERTIES.keySet().removeIf(key -> !newProperties.containsKey(key));

    // Add or update keys from the new properties
    newProperties.forEach((key, value) -> PROPERTIES.putIfAbsent(key, value));

    saveProperties();
  }
}

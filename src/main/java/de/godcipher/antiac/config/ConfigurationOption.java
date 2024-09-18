package de.godcipher.antiac.config;

import java.util.List;
import lombok.Value;

@Value
public class ConfigurationOption<T> {

  public static ConfigurationOption<Integer> ofInteger(int value, String comment) {
    return new ConfigurationOption<>(value, comment);
  }

  public static ConfigurationOption<Double> ofDouble(double value, String comment) {
    return new ConfigurationOption<>(value, comment);
  }

  public static ConfigurationOption<Boolean> ofBoolean(boolean value, String comment) {
    return new ConfigurationOption<>(value, comment);
  }

  public static ConfigurationOption<List<String>> ofStringList(List<String> value, String comment) {
    return new ConfigurationOption<>(value, comment);
  }

  T value;
  String comment;
}

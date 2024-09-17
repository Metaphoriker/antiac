package de.godcipher.antiac.click;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class Click {

  public static final Click EMPTY = new Click(-1, -1);

  long time; // When the click was made
  long delay; // Delay between this click and the last one
}

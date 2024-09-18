package de.godcipher.antiac.detection.violation;

import java.time.Instant;
import lombok.Getter;

@Getter
public class Violation {

  private int count = 0;
  private Instant lastModified = Instant.now();

  public void incrementCount() {
    this.count++;
    this.lastModified = Instant.now();
  }

  public void resetCount() {
    this.count = 0;
    this.lastModified = Instant.now();
  }
}

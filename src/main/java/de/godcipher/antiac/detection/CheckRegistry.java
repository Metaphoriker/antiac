package de.godcipher.antiac.detection;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class CheckRegistry {

  private final Set<Check> checks = new HashSet<>();

  public void registerCheck(Check check) {
    checks.add(check);
    check.load();
  }

  public void unregisterCheck(Check check) {
    checks.remove(check);
    check.unload();
  }

  public void performChecks(Player player) {
    checks.stream()
        .filter(check -> check.isActivated() && check.isLoaded())
        .forEach(
            check -> {
              if (check.check(player)) {
                check.onFlag(player);
              }
            });
  }
}

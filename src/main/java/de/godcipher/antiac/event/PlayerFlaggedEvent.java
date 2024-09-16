package de.godcipher.antiac.event;

import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.value.CPS;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = false)
public class PlayerFlaggedEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  Player player;
  List<CPS> cpsHistory;
  Check check;

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}

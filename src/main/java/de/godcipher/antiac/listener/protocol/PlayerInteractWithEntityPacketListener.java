package de.godcipher.antiac.listener.protocol;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import de.godcipher.antiac.value.ClickTracker;
import org.bukkit.entity.Player;

public class PlayerInteractWithEntityPacketListener extends BasePlayerPacketListener {

  public PlayerInteractWithEntityPacketListener(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
      Player player = (Player) event.getPlayer();
      handleClick(player);
    }
  }
}

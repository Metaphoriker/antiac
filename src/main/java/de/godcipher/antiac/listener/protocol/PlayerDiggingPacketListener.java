package de.godcipher.antiac.listener.protocol;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import de.godcipher.antiac.click.ClickTracker;
import org.bukkit.entity.Player;

public class PlayerDiggingPacketListener extends BasePlayerPacketListener {

  public PlayerDiggingPacketListener(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
      Player player = (Player) event.getPlayer();
      handleClick(player);
    }
  }
}

package de.godcipher.antiac.listener.protocol;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.click.ClickType;
import org.bukkit.entity.Player;

public class PlayerAttackEntityPacketListener extends BasePlayerPacketListener {

  public PlayerAttackEntityPacketListener(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
      WrapperPlayClientInteractEntity interactEntityPacket =
          new WrapperPlayClientInteractEntity(event);
      if (!isAttack(interactEntityPacket.getAction())) return;

      Player player = (Player) event.getPlayer();
      handleClick(player, ClickType.ATTACK);
    }
  }

  private boolean isAttack(WrapperPlayClientInteractEntity.InteractAction action) {
    return action == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
  }
}

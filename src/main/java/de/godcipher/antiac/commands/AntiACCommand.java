package de.godcipher.antiac.commands;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.messages.Messages;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class AntiACCommand implements TabExecutor {

  private final List<String> subCommands = List.of("check", "cancel");
  private final Map<UUID, UUID> playerChecks = new HashMap<>();
  private final ClickTracker clickTracker;

  @Override
  public boolean onCommand(
      @NotNull CommandSender commandSender,
      @NotNull Command command,
      @NotNull String s,
      @NotNull String[] args) {
    if (!(commandSender instanceof Player)) {
      commandSender.sendMessage(Messages.getString("command.only_player"));
      return true;
    }

    Player player = (Player) commandSender;

    if (args.length == 0) {
      player.sendMessage(Messages.getString("command.specify_subcommand"));
      return true;
    }

    String subCommand = args[0].toLowerCase();
    return switch (subCommand) {
      case "check" -> handleCheckCommand(player, args);
      case "cancel" -> handleCancelCommand(player);
      default -> {
        player.sendMessage(
            MessageFormat.format(
                Messages.getString("command.unknown_subcommand"), String.join(", ", subCommands)));
        yield true;
      }
    };
  }

  private boolean handleCheckCommand(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(Messages.getString("command.check.specify_player"));
      return true;
    }

    Player target = player.getServer().getPlayer(args[1]);
    if (target == null) {
      player.sendMessage(Messages.getString("command.check.player_not_found"));
      return true;
    }

    playerChecks.put(player.getUniqueId(), target.getUniqueId());
    player.sendMessage(
        MessageFormat.format(Messages.getString("command.check.now_checking"), target.getName()));
    startCheckTask(player, target);
    return true;
  }

  private void startCheckTask(Player player, Player target) {
    new BukkitRunnable() {
      int maxCPS;

      @Override
      public void run() {
        if (playerChecks.containsKey(player.getUniqueId())) {
          CPS cps = clickTracker.getLatestCPS(target.getUniqueId());
          maxCPS = Math.max(maxCPS, cps.getCPS());
          player
              .spigot()
              .sendMessage(
                  ChatMessageType.ACTION_BAR,
                  TextComponent.fromLegacyText("§e" + cps + " | §6§l" + maxCPS));
        } else {
          cancel();
        }
      }
    }.runTaskTimer(AntiAC.getInstance(), 0, 2);
  }

  private boolean handleCancelCommand(Player player) {
    if (!playerChecks.containsKey(player.getUniqueId())) {
      player.sendMessage(Messages.getString("command.cancel.not_checking"));
      return true;
    }

    playerChecks.remove(player.getUniqueId());
    player.sendMessage(Messages.getString("command.cancel.canceled"));
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
      @NotNull CommandSender commandSender,
      @NotNull Command command,
      @NotNull String s,
      @NotNull String[] strings) {
    return subCommands;
  }
}

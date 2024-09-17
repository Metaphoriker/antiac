package de.godcipher.antiac.commands;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
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

  private final List<String> subCommands = List.of("check");
  private final Map<UUID, UUID> playerChecks = new HashMap<>();

  private final ClickTracker clickTracker;

  @Override
  public boolean onCommand(
      @NotNull CommandSender commandSender,
      @NotNull Command command,
      @NotNull String s,
      @NotNull String[] args) {
    if (!(commandSender instanceof Player)) {
      commandSender.sendMessage("This command can only be executed by a player.");
      return true;
    }

    Player player = (Player) commandSender;

    if (args.length == 0) {
      player.sendMessage("Please specify a subcommand.");
      return true;
    }

    if (!subCommands.contains(args[0].toLowerCase())) {
      player.sendMessage(
          "Unknown subcommand. Available subcommands: " + String.join(", ", subCommands));
      return true;
    }

    if (args[0].equalsIgnoreCase("check")) {
      if (args.length < 2) {
        player.sendMessage("Please specify a player to check.");
        return true;
      }

      Player target = player.getServer().getPlayer(args[1]);
      if (target == null) {
        player.sendMessage("Player not found.");
        return true;
      }

      playerChecks.put(player.getUniqueId(), target.getUniqueId());
      player.sendMessage("You are now checking " + target.getName() + ".");

      // TODO: per player?
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
      return true;
    }

    if (args[0].equalsIgnoreCase("cancel")) {
      if (!playerChecks.containsKey(player.getUniqueId())) {
        player.sendMessage("You are not currently checking any player.");
        return true;
      }

      playerChecks.remove(player.getUniqueId());
      player.sendMessage("You have canceled the check.");
      return true;
    }

    return false;
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

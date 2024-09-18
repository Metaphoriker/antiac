package de.godcipher.antiac.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import de.godcipher.antiac.messages.Colors;
import de.godcipher.antiac.messages.Messages;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
@CommandAlias("antiac")
public class AntiACCommand extends BaseCommand {

  private static final String ERROR_TITLE = "Error";
  private static final String SUCCESS_TITLE = "✔";

  private final Map<UUID, UUID> playerChecks = new HashMap<>();
  private final ClickTracker clickTracker;
  private final ViolationTracker violationTracker;

  @Default
  @Description("Specify a subcommand")
  public void onDefault(Player player) {
    sendTitle(
        player, Colors.ERROR_COLOR, ERROR_TITLE, Messages.getString("command.specify_subcommand"));
  }

  @CatchUnknown
  @Description("Unknown subcommand")
  public void onUnknown(Player player) {
    sendTitle(
        player, Colors.ERROR_COLOR, ERROR_TITLE, Messages.getString("command.unknown_subcommand"));
  }

  @Subcommand("check")
  @CommandCompletion("@players")
  @CommandPermission("antiac.check")
  @Description("Check a player's CPS")
  public void onCheck(Player player, @Optional String targetName) {
    if (targetName == null) {
      sendTitle(
          player,
          Colors.ERROR_COLOR,
          ERROR_TITLE,
          Messages.getString("command.check.specify_player"));
      return;
    }

    Player target = player.getServer().getPlayer(targetName);
    if (target == null) {
      sendTitle(
          player,
          Colors.ERROR_COLOR,
          ERROR_TITLE,
          Messages.getString("command.check.player_not_found"));
      return;
    }

    playerChecks.put(player.getUniqueId(), target.getUniqueId());
    sendTitle(player, Colors.SUCCESS_COLOR, SUCCESS_TITLE, "");
    startCheckTask(player, target);
  }

  @Subcommand("cancel")
  @CommandPermission("antiac.check")
  @Description("Cancel the current check")
  public void onCancel(Player player) {
    if (!playerChecks.containsKey(player.getUniqueId())) {
      sendTitle(
          player,
          Colors.ERROR_COLOR,
          ERROR_TITLE,
          Messages.getString("command.cancel.not_checking"));
      return;
    }

    playerChecks.remove(player.getUniqueId());
    sendTitle(player, Colors.SUCCESS_COLOR, SUCCESS_TITLE, "");
  }

  private void startCheckTask(Player player, Player target) {
    new BukkitRunnable() {
      int maxCPS;

      @Override
      public void run() {
        if (playerChecks.containsKey(player.getUniqueId())) {
          CPS cps = clickTracker.getLatestCPS(target.getUniqueId());
          CPS previousCPS = getPreviousCPS(target.getUniqueId());
          if (previousCPS.isEmpty()) {
            maxCPS = 0;
          }

          maxCPS = Math.max(maxCPS, cps.getCPS());

          String cpsPart = Colors.BRASS_YELLOW_COLOR + "" + cps;
          String separator = Colors.SEPARATOR_COLOR + " | ";
          String maxCpsPart = Colors.COPPER_ORANGE_COLOR + "" + maxCPS;
          String violationPart =
              Colors.SLATE_GRAY_COLOR
                  + "Violations: "
                  + Colors.ROSEWOOD_RED_COLOR
                  + violationTracker.getViolationCount(target.getUniqueId());

          StringBuilder message = new StringBuilder();
          message
              .append(cpsPart)
              .append(separator)
              .append(maxCpsPart)
              .append(Colors.SEPARATOR_COLOR)
              .append(" - ")
              .append(violationPart);

          player
              .spigot()
              .sendMessage(
                  ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.toString()));
        } else {
          cancel();
        }
      }
    }.runTaskTimer(AntiAC.getInstance(), 0, 2);
  }

  private CPS getPreviousCPS(UUID playerId) {
    return clickTracker.getCPSList(playerId).stream()
        .skip(Math.max(0, clickTracker.getCPSList(playerId).size() - 2))
        .findFirst()
        .orElse(CPS.EMPTY);
  }

  private void sendTitle(Player player, ChatColor chatColor, String title, String subtitle) {
    player.sendTitle(chatColor + title, "§7" + subtitle, 10, 70, 20);
  }
}

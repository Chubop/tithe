package bsh.tithe.commands;

import bsh.tithe.Tithe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static bsh.tithe.Tithe.EXCLAMATION_MSG;
import static bsh.tithe.Tithe.getThroneCount;

public class TitheCommand implements CommandExecutor {

    private final Tithe plugin;

    public TitheCommand(Tithe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("tithe.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("save")) {
                plugin.saveRulersFile();
                Tithe.savePlayersFile();
                sender.sendMessage(ChatColor.GREEN + "Configuration files have been saved.");
                return true;
            } else if (args[0].equalsIgnoreCase("wipe")) {
                plugin.resetRulers();
                plugin.resetPlayers();
                sender.sendMessage(ChatColor.GREEN + "Rulers and players have been reset.");
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                if (args.length == 2) {
                    String playerName = args[1];
                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

                    if (targetPlayer != null && targetPlayer.hasPlayedBefore()) {
                        UUID targetPlayerId = targetPlayer.getUniqueId();
                        int throneCount = getThroneCount(targetPlayerId); // Replace with the method you use to get the throne count
                        sender.sendMessage(ChatColor.YELLOW + playerName + ChatColor.WHITE + " has " + ChatColor.YELLOW + throneCount + ChatColor.WHITE + " throne(s).");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                    }
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("settax")) {
                if (args.length == 2) {
                    Integer taxRate;
                    try {
                        taxRate = Integer.parseInt(args[1].replace("%", ""));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Error: Invalid tax rate.");
                        return true;
                    }
                    if (taxRate != null) {
                        if (taxRate < 0 || taxRate > 100) {
                            sender.sendMessage(ChatColor.RED + "Tax rate must be in between 0% and 100%.");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "As you wish, my liege.");
                            Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.YELLOW + ChatColor.BOLD + sender.getName() + ChatColor.RESET + "" + ChatColor.WHITE + " has set the tax rate to " + ChatColor.YELLOW + args[1] + "%");
                            float newTax = ((float) Integer.parseInt(args[1]) / 100);
                            Bukkit.broadcastMessage(String.valueOf(newTax));
                            Tithe.setTaxRate(newTax);
                            return true;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
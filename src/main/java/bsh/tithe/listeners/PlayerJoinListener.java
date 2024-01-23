package bsh.tithe.listeners;

import bsh.tithe.Tithe;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private Tithe plugin = Tithe.getPlugin();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        String playerName = Bukkit.getOfflinePlayer(playerId).getName();
        if (!plugin.getPlayersConfig().contains("players." + playerId.toString())) {
            plugin.getPlayersConfig().set("players." + playerId.toString() + ".throneCount", 0);
            plugin.getPlayersConfig().set("players." + playerId.toString() + ".displayName", playerName);
            Tithe.savePlayersFile();
        }
        if(Tithe.bb != null){
            Tithe.givePlayerBossBar(event.getPlayer());
        }
    }
}
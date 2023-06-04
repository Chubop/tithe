package bsh.tithe.events;

import bsh.tithe.Tithe;
import bsh.tithe.entity.Ruler;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

public class ThroneClaimEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    public static Map<UUID, BukkitTask> playerTimers = Tithe.playerTimers;
    private UUID playerId;
    private Player player;
    public String country;
    private String EXCLAMATION_MSG = ChatColor.RESET + "" + ChatColor.RED +
            ChatColor.BOLD + "" + ChatColor.UNDERLINE + "" + "!" + ChatColor.RESET + " ";

    public ThroneClaimEvent(UUID playerId, String country) {
        this.playerId = playerId;
        this.country = country;
        this.player = Bukkit.getPlayer(this.playerId);

        // send player claim message
        Ruler r = new Ruler(this.playerId, this.player.getName());
        if(!r.ownsCountry(country)){
            this.player.sendMessage(EXCLAMATION_MSG + ChatColor.GREEN + "Claiming " + this.country + "...");
        }
    }

    public void broadcastThroneClaimMessage(){
        Bukkit.broadcastMessage(
            EXCLAMATION_MSG +
            EXCLAMATION_MSG +
            ChatColor.YELLOW +
            this.player.getName() +
            ChatColor.WHITE +
            " is now claiming "
            + this.country + " " +
            EXCLAMATION_MSG
            + EXCLAMATION_MSG
        );
    }

    public void broadcastConqueredMessage(Player player) {
        Map<String, Ruler> currRulers = Tithe.getRulers();
        if(!currRulers.containsKey(this.getCountry())){
            Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " has claimed " + ChatColor.YELLOW + this.getCountry() + ChatColor.WHITE + "!");
        }
        else{
            String lastRulerName = currRulers.get(country).getUsername(); // previous ruler before we update the map
            Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " has conquered " + ChatColor.YELLOW + this.getCountry() + ChatColor.WHITE + " from " + ChatColor.YELLOW + lastRulerName);
        }
    }

    public void promotePlayer(Ruler ruler){
        ruler.setRuler(this.getCountry());
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }


    @Override
    public void setCancelled(boolean cancel) {

    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getCountry() {
        return country;
    }
}

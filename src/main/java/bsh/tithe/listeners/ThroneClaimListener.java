package bsh.tithe.listeners;

import io.th0rgal.oraxen.api.events.OraxenFurnitureInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class ThroneClaimListener implements Listener {

    public String getThroneName(String itemID){
        if(itemID.equals("royal_throne")){
            return "Royal Throne";
        }
        if(itemID.equals("clockwork_throne")){
            return "Clockwork Throne";
        }
        if(itemID.equals("tribal_throne")){
            return "Tribal Throne";
        }
        return null;
    }

    @EventHandler
    public void onPlayerSit(OraxenFurnitureInteractEvent e){
        String itemName = e.getMechanic().getItemID().toString();
        Bukkit.broadcastMessage(ChatColor.RESET + "" + ChatColor.YELLOW + "" + ChatColor.BOLD + e.getPlayer().getName() + " is now claiming the " + getThroneName(itemName) + " of England!");
        Bukkit.broadcastMessage(itemName);
    }
}

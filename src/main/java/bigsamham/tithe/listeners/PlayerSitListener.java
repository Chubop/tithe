package bigsamham.tithe.listeners;

import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;

public class PlayerSitListener implements Listener {
    @EventHandler
    public void onPlayerSit(EntityMountEvent event){
        Player p = (Player) event.getEntity();
        Block b = p.getLocation().getBlock();
        if(OraxenFurniture.isFurniture(b)){
            Bukkit.broadcastMessage(p.getDisplayName() + " is claiming a throne!");
        }
    }
}

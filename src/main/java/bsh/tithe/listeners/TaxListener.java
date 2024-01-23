package bsh.tithe.listeners;

import bsh.tithe.Tithe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TaxListener implements Listener {

    public ArrayList<Material> taxableMats = new ArrayList<>(Arrays.asList(
            Material.DIAMOND,
            Material.COAL,
            Material.WHEAT,
            Material.CARROT,
            Material.MELON_SLICE,
            Material.BEETROOT
    ));

    public Material legacyToNormal(Material m) {
        String materialName = m.toString();
        switch(materialName) {
            case "LEGACY_BEETROOT":
                materialName = "BEETROOT";
                break;
            case "LEGACY_CROPS":
                materialName = "WHEAT";
                break;
            case "LEGACY_CARROT_ITEM":
                materialName = "CARROT";
                break;
            case "LEGACY_MELON":
                materialName = "MELON_SLICE";
                break;
            case "LEGACY_COAL":
                materialName = "COAL";
                break;
            case "LEGACY_DIAMOND":
                materialName = "DIAMOND";
                break;

        }
        return Material.valueOf(materialName);
    }


    @EventHandler
    public void onBlockBreak(BlockDropItemEvent e){
        float TAXRATE = Tithe.getTaxRate();
        OfflinePlayer ruler;
        try {
            ruler = Bukkit.getOfflinePlayer(Tithe.getTopRuler());
        }
        catch (IllegalArgumentException iae){
            ruler = null;
        }
        if(!e.getItems().isEmpty() && ruler != null && TAXRATE != 0 ){
            ItemStack is = e.getItems().get(0).getItemStack();
            Material m = legacyToNormal(is.getData().getItemType());

            if(taxableMats.contains(m)){
                is.setType(m);
                int amountDropped = is.getAmount();
                Player p = e.getPlayer();
                if(amountDropped > 0){ // if it's an actual item
                    int chanceOfTax = new Random().nextInt(10);
                    if(chanceOfTax <= TAXRATE * 10){
                        ruler.getPlayer().getEnderChest().addItem(is);
                        is.setAmount(0);
                        p.sendMessage(Tithe.EXCLAMATION_MSG + ChatColor.GRAY + "Your " + m.toString().toLowerCase() + " has been taxed by Monarch " + ruler.getName() + " at the current rate of " + ChatColor.RED + TAXRATE * 100  + "%");
                    }
                }
            }
        }
    }
}

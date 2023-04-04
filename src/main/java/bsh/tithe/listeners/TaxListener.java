package bsh.tithe.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class TaxListener implements Listener {

    private static double TAX_RATE = 0.5;
    private static int DIAMOND_COFFERS = 0;
    public Material[] taxableMats = {
            Material.DIAMOND,
            Material.COAL,
            Material.RAW_IRON,
            Material.RAW_GOLD,
    };

    public Material legacyToNormal(Material m){
        if(m.toString().equals("LEGACY_DIAMOND")){
            return Material.DIAMOND;
        }
        return m;
    }

    @EventHandler
    public void onBlockBreak(BlockDropItemEvent e){
        double taxRate = 0.5;
        ItemStack is = e.getItems().get(0).getItemStack();
        Material m = legacyToNormal(is.getData().getItemType());
        is.setType(m);
        int amountDropped = is.getAmount();

        Player p = e.getPlayer();
        if(amountDropped > 0){ // if it's an actual item
            for(int i = 0; i < taxableMats.length; i++){
                if(m.equals(taxableMats[i])){
                    int chanceOfTax = new Random().nextInt(10);
                    if(chanceOfTax >= taxRate * 10){
                        is.setAmount(0);
                        DIAMOND_COFFERS += amountDropped;
                        p.sendMessage("Your " + m.toString().toLowerCase() + " has been taxed by King BigSamHam at the current rate of " + String.valueOf(taxRate * 100) + "%");
                        Bukkit.broadcastMessage("Diamond Coffers: " + String.valueOf(DIAMOND_COFFERS));
                    }
                }
            }
        }
    }
}

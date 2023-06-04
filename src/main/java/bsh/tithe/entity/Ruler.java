package bsh.tithe.entity;
import bsh.tithe.Tithe;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static bsh.tithe.Tithe.decrementPlayerThroneCount;

public class Ruler {
    private UUID uuid;
    private String username;
    private Boolean isMonarch;

    public Ruler(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.isMonarch = false;
    }

    private ArrayList<String> getRulingCountries(){
        ArrayList<String> countries = new ArrayList<>();
        Map<String, Ruler> rulers = Tithe.getRulers();
        for(String country : rulers.keySet()){
            Ruler r = rulers.get(country);
            if(r.getUsername().equals(this.getUsername())){
                countries.add(country);
            }
        }
        return countries;
    }

    public Boolean ownsCountry(String country){
        ArrayList<String> countries = this.getRulingCountries();
        if(countries.contains(country)){
            return true;
        }
        return false;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return username;
    }

    private void givePlayerCrown(UUID playerId) {
        Player player = (Player) Bukkit.getOfflinePlayer(playerId);
        ItemBuilder crownBuilder = OraxenItems.getItemById("crown");
        String crownDisplayName = ChatColor.RESET + "Royal Crown";
        crownBuilder.setDisplayName(crownDisplayName); // set the crown name per specific nation conquered
        player.getInventory().addItem(crownBuilder.build());
    }

    public void setRuler(String country){
        Tithe.getRulers().put(country, this);
        Tithe.incrementPlayerThroneCount(this.getUuid());
        this.givePlayerCrown(this.getUuid());
        this.updateThroneCount();
    }


    private void updateThroneCount(){
        for(String country : Tithe.getRulers().keySet()){
            Ruler r = Tithe.getRulers().get(country);
            if(r.equals(this)){ // comparing usernames
                Tithe.rulers.put(country, this);
            }
        }
    }

    public Integer getThroneCount(){
        return Tithe.getThroneCount(this.getUuid());
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Ruler){
            Ruler r = (Ruler) obj;
            if (this.getUsername().equals(r.getUsername())){
                return true;
            }
        }
        return false;
    }

}
package bsh.tithe;

import bsh.tithe.commands.TitheCommand;
import bsh.tithe.commands.TitheTabCompleter;
import bsh.tithe.entity.Ruler;
import bsh.tithe.listeners.PlayerJoinListener;
import bsh.tithe.listeners.TaxListener;
import bsh.tithe.listeners.ThroneClaimListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// file creation
import org.bukkit.scheduler.BukkitTask;


public class Tithe extends JavaPlugin implements Listener {

    private static Tithe plugin;
    private static ProtocolManager manager;

    public static Map<String, Ruler> rulers = new HashMap<>();
    public static Map<UUID, Integer> playerThroneCounts = new HashMap<>();

    public static float getTaxRate() {
        return taxRate;
    }

    public static void setTaxRate(float taxRate) {
        Tithe.taxRate = taxRate;
    }

    public static float taxRate = 0;


    public static Map<String, Ruler> getRulers() {
        return rulers;
    }

    public static Map<UUID, Integer> getPlayerThroneCounts() {
        return playerThroneCounts;
    }

    public static LuckPerms luckPermsApi;

    public static File rulersFile;
    private static FileConfiguration rulersConfig;

    public static File playersFile;
    public static FileConfiguration playersConfig;

    public static Map<UUID, BukkitTask> playerTimers = new HashMap<>();

    public static String EXCLAMATION_MSG = ChatColor.RESET + "" + ChatColor.RED +
            ChatColor.BOLD + "" + ChatColor.UNDERLINE + "" + "!" + ChatColor.RESET + " ";

    public static Ruler getRuler(UUID id){
        for(Ruler r : rulers.values()){
            if(r.getUuid().equals(id)){
                return r;
            }
        }
        return null;
    }

    public static Ruler getRulerOfNation(String country){
        return rulers.get(country);
    }

    public static boolean isPlayerRuler(UUID id){
        for(Ruler r : rulers.values()){
            if(r.getUuid().equals(id)){
                return true;
            }
        }
        return false;
    }

    // RULERS FILES
    private void createRulersFile() {
        rulersFile = new File(getDataFolder(), "rulers.yml");
        if (!rulersFile.exists()) {
            rulersFile.getParentFile().mkdirs();
            saveResource("rulers.yml", false);
        }
        rulersConfig = YamlConfiguration.loadConfiguration(rulersFile);
    }

    public static void saveRulersFile() {
        for (Map.Entry<String, Ruler> entry : rulers.entrySet()) {
            String country = entry.getKey();
            Ruler ruler = entry.getValue();
            String path = country + ".";
            rulersConfig.set(path + "uuid", ruler.getUuid().toString());
            rulersConfig.set(path + "username", ruler.getUsername());
        }
        try {
            rulersConfig.save(rulersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRulersFromFile() {
        for (String country : rulersConfig.getKeys(false)) {
            String path = country + ".";
            UUID uuid = UUID.fromString(rulersConfig.getString(path + "uuid"));
            String username = rulersConfig.getString(path + "username");
            Ruler ruler = new Ruler(uuid, username);
            rulers.put(country, ruler);
        }
    }

    public void resetRulers() {
        rulers.clear();
        saveRulersFile(); // You might want to save the empty map to the file as well.
    }

    public void resetPlayers(){
        playerThroneCounts.replaceAll((uuid, count) -> 0);
        saveRulersFile();
        savePlayersFile();
    }

    // END RULERS


    // PLAYERS FILE
    private void createPlayersFile() {
        playersFile = new File(getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            playersFile.getParentFile().mkdirs();
            saveResource("players.yml", false);
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
    }

    private void loadPlayersFile(){
        ConfigurationSection playersSection = getPlayersConfig().getConfigurationSection("players");
        if (playersSection != null) {
            for (String key : playersSection.getKeys(false)) {
                UUID playerId = UUID.fromString(key);
                int throneCount = getPlayersConfig().getInt("players." + key + ".throneCount");
                playerThroneCounts.put(playerId, throneCount);
            }
        }
    }

    public static void savePlayersFile() {
        for (Map.Entry<UUID, Integer> entry : playerThroneCounts.entrySet()) {
            UUID playerId = entry.getKey();
            int throneCount = entry.getValue();
            playersConfig.set("players." + playerId + ".throneCount", throneCount);
        }
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayersConfig() {
        return playersConfig;
    }

    public static void incrementPlayerThroneCount(UUID playerId){
        int currentThroneCount = playerThroneCounts.getOrDefault(playerId, 0);
        playerThroneCounts.put(playerId, currentThroneCount + 1);
    }

    public static void decrementPlayerThroneCount(UUID playerId){
        int currentThroneCount = playerThroneCounts.getOrDefault(playerId, 0);
        if(currentThroneCount != 0){
            playerThroneCounts.put(playerId, currentThroneCount - 1);
        }
    }

    public static Integer getThroneCount(UUID playerId){
        return playerThroneCounts.getOrDefault(playerId, 0);
    }

    public static UUID getTopRuler() {
        for (Map.Entry<UUID, Integer> entry : playerThroneCounts.entrySet()) {
            if (entry.getValue() >= 3) {
                return entry.getKey();
            }
        }
        return null;
    }


    // END PLAYERS FILE

    // BEGIN MONARCH
    public static void updateTopRulerPermission() {
        UUID topRuler = getTopRuler();
        if(topRuler != null){
            Bukkit.broadcastMessage("top ruler: " + topRuler.toString());
            OfflinePlayer player = Bukkit.getOfflinePlayer(topRuler);
            PermissionAttachment attachment = player.getPlayer().addAttachment(Tithe.getPlugin());
            Integer throneCount = getThroneCount(topRuler);
            Bukkit.broadcastMessage("Top Ruler " + player.getName() + " has " + throneCount.toString() + " thrones.");
            Boolean hasPermission = attachment.getPermissions().getOrDefault("tithe.taxes", false);

            if (throneCount >= 3) {
                attachment.setPermission("tithe.taxes", true);
                Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + player.getName() + ChatColor.WHITE + "" +
                        " is now the " + ChatColor.RED + "Monarch of Europe"
                        + ChatColor.WHITE + " and may now levy taxes.");
            }
        }
    }


    @Override
    public void onEnable() {
        plugin = this;
        manager = ProtocolLibrary.getProtocolManager();
        getLogger().info("Tithe is enabled.");
        getServer().getPluginManager().registerEvents(new ThroneClaimListener(), this);
        getServer().getPluginManager().registerEvents(new TaxListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        createRulersFile();
        createPlayersFile();

        loadRulersFromFile();
        loadPlayersFile();

        luckPermsApi = LuckPermsProvider.get();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("tithe").setExecutor(new TitheCommand(this));
        getCommand("tithe").setTabCompleter(new TitheTabCompleter());
    }

    public static Tithe getPlugin(){
        return plugin;
    }

    public static ProtocolManager getManager() { return manager; }

    @Override
    public void onDisable() {
        getLogger().info("Saving Tithe configurations...");
        saveRulersFile();
        savePlayersFile();
        getLogger().info("Tithe is disabled.");
    }
}

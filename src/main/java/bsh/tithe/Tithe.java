package bsh.tithe;

import bsh.tithe.commands.SleepCommand;
import bsh.tithe.commands.Test;
import bsh.tithe.listeners.TaxListener;
import bsh.tithe.listeners.ThroneClaimListener;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Tithe extends JavaPlugin implements Listener {

    private static Tithe plugin;
    private static ProtocolManager manager;
    @Override
    public void onEnable() {
        plugin = this;
        manager = ProtocolLibrary.getProtocolManager();
        getLogger().info("Tithe is enabled.");
        getServer().getPluginManager().registerEvents(new ThroneClaimListener(), this);
        getServer().getPluginManager().registerEvents(new TaxListener(), this);

        this.getCommand("test").setExecutor(new Test());
        this.getCommand("knockout").setExecutor(new SleepCommand());

    }

    public static Tithe getPlugin(){
        return plugin;
    }

    public static ProtocolManager getManager() { return manager; }
    @Override
    public void onDisable() {
        getLogger().info("Tithe is disabled.");
    }
}

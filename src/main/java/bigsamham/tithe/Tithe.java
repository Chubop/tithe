package bigsamham.tithe;

import bigsamham.tithe.commands.Test;
import bigsamham.tithe.listeners.PlayerSitListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Tithe extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Tithe is enabled.");
        getServer().getPluginManager().registerEvents(new PlayerSitListener(), this);
        this.getCommand("test").setExecutor(new Test());
    }
    @Override
    public void onDisable() {
        getLogger().info("Tithe is disabled.");
    }
}

package bsh.tithe.commands;

import bsh.tithe.Tithe;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class SleepCommand implements CommandExecutor {

    private HashMap<Player, Boolean> sleeping = new HashMap<Player, Boolean>();
    ProtocolManager manager = Tithe.getManager();

    public static void layPlayer(Player p) {
        // Without api : '/displayer player <player> setEntityPose SLEEPING setNameVisible false'
        // disguises
        PlayerDisguise disguise = new PlayerDisguise(p.getName());
        disguise.setSelfDisguiseVisible(true);
        disguise.getWatcher().setSleeping(true);
        disguise.setNameVisible(false);
        DisguiseAPI.setActionBarShown(p, false);
        DisguiseAPI.disguiseEntity(p, disguise);

        // immobilize player
        Collection<PotionEffect> imoEffects = new HashSet<>();
        imoEffects.add(PotionEffectType.DARKNESS.createEffect(Integer.MAX_VALUE, 1)); // DARKNESS
        imoEffects.add(PotionEffectType.SLOW.createEffect(Integer.MAX_VALUE, 10)); // HIGH SLOWNESS
        imoEffects.add(PotionEffectType.JUMP.createEffect(Integer.MAX_VALUE, 128)); // NO JUMPING
        p.addPotionEffects(imoEffects);

    }

    public static void unDisguise(Player p) {
        DisguiseAPI.setActionBarShown(p, true);
        DisguiseAPI.undisguiseToAll(p);
        p.removePotionEffect(PotionEffectType.DARKNESS);
        p.removePotionEffect(PotionEffectType.SLOW);
        p.removePotionEffect(PotionEffectType.JUMP);

        // Without api : dispatch the command '/undisplayer <player>'
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player p = ((Player) sender).getPlayer();
            if(args.length >= 1){
                p = Bukkit.getPlayer(args[0]);
            }
            if(!sleeping.containsKey(p)){
                layPlayer(p);
                sleeping.put(p, Boolean.TRUE);
            }
            else if(sleeping.containsKey(p)){
                unDisguise(p);
                sleeping.remove(p);
            }
        }
        return true;
    }
}

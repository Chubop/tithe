package bsh.tithe.listeners;
import bsh.tithe.Tithe;
import bsh.tithe.entity.Ruler;
import bsh.tithe.events.ThroneClaimEvent;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenFurnitureInteractEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static bsh.tithe.Tithe.*;


public class ThroneClaimListener implements Listener {
    private final Integer CONQUER_TIMER = 20 * 10;
    private final Map<UUID, BukkitTask> playerTimers = Tithe.playerTimers;
    private String EXCLAMATION_MSG = ChatColor.RESET + "" + ChatColor.RED +
            ChatColor.BOLD + "" + ChatColor.UNDERLINE + "" + "!" + ChatColor.RESET + " ";

    private void setPlayerSuffix(Player player, String suffix) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            // Remove existing suffix
            user.data().clear(node -> node instanceof MetaNode && ((MetaNode) node).getKey().equalsIgnoreCase("suffix"));

            // Add new suffix
            MetaNode suffixNode = MetaNode.builder("suffix", suffix).build();
            user.data().add(suffixNode);

            // Save changes to the user
            luckPerms.getUserManager().saveUser(user);
        }
    }

    public void addPlayerToRulerGroup(UUID playerUUID) {
        LuckPerms api = luckPermsApi;

        // Load user into memory and perform action
        api.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            Group group = api.getGroupManager().getGroup("ruler");

            if (group != null) {
                InheritanceNode groupNode = InheritanceNode.builder(group).build();
                user.data().add(groupNode);
                api.getUserManager().saveUser(user);
            }
        });
    }

    public void removePlayerFromRulerGroup(UUID playerUUID) {
        LuckPerms api = luckPermsApi;

        // Load user into memory and perform action
        api.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            Group group = api.getGroupManager().getGroup("ruler");

            if (group != null) {
                InheritanceNode groupNode = InheritanceNode.builder(group).build();
                user.data().remove(groupNode);
                api.getUserManager().saveUser(user);
            }
        });
    }



    public String getThroneName(String itemID) {
        switch (itemID) {
            case "royal_throne":
            case "clockwork_throne":
            case "tribal_throne":
                return "Royal Throne";
            default:
                return null;
        }
    }

    public String getThroneCountry(Material underBlock) {
        switch (underBlock) {
            case COBBLESTONE:
                return "England";
            case RED_WOOL:
                return "France";
            case CUT_SANDSTONE:
                return "The Ottoman Empire";
            case DARK_OAK_SLAB:
                return "The Holy Roman Empire";
            case STRIPPED_SPRUCE_WOOD:
                return "Spain";
            default:
                return null;
        }
    }


    @EventHandler
    public void onPlayerSit(OraxenFurnitureInteractEvent e) {
        Player player = e.getPlayer();
        UUID playerId = player.getUniqueId();
        String itemName = e.getMechanic().getItemID().toString();

        // If a player is sitting on a throne
        if (getThroneName(itemName) != (null)) {
            // Cancel any existing timer for this player
            this.cancelExistingTimer(playerId);

            Block underBlock = e.getBlock().getRelative(BlockFace.DOWN);
            Material mat = underBlock.getBlockData().getMaterial();

            // get the country of the throne depending on the block underneath the throne
            String country = getThroneCountry(mat);

            // fire the custom ThroneClaim event
            ThroneClaimEvent event = new ThroneClaimEvent(
                    playerId,
                    country
            );

            Bukkit.getPluginManager().callEvent(event);

            // Broadcast to the world that the player is claiming the throne
            Ruler r = new Ruler(playerId, player.getName());
            if(!r.ownsCountry(country)){
                ItemStack offHandItem = player.getInventory().getItemInOffHand();

                if (offHandItem.getType() == Material.GOLD_INGOT && offHandItem.getAmount() >= 8) {
                    offHandItem.setAmount(offHandItem.getAmount() - 24);
                    player.getInventory().setItemInOffHand(offHandItem);
                    player.updateInventory();
                    event.broadcastThroneClaimMessage();
                } else {
                    player.sendMessage(ChatColor.RED + "You must have 8 gold ingots in your " + ChatColor.BOLD + "" + ChatColor.WHITE + "offhand slot" + ChatColor.RESET + "" + ChatColor.RED + " to claim a throne!");
                    e.setCancelled(true);
                    return;
                }
            }

            // Start a 60-second timer
            BukkitTask timer = createTimerTask(playerId, event).runTaskTimer(Tithe.getPlugin(), 0, CONQUER_TIMER); // ten seconds per

            // Store the timer task in the map
            playerTimers.put(playerId, timer);
        }
    }

    private void cancelExistingTimer(UUID playerId) {
        if (playerTimers.containsKey(playerId)) {
            playerTimers.get(playerId).cancel();
            playerTimers.remove(playerId);
        }
    }


    private BukkitRunnable createTimerTask(UUID playerId, ThroneClaimEvent event) {
        return new BukkitRunnable() {
            Player player = Bukkit.getPlayer(playerId);
            int count = 0;
            double initialHealth = player.getHealth();

            @Override
            public void run() {
                // Check if player is still online
                if (player == null || !player.isOnline()) {
                    playerTimers.remove(playerId);
                    this.cancel();
                    return;
                }

                // Check if player has taken damage
                if (player.getHealth() < initialHealth) {
                    player.sendMessage(ChatColor.RED + "You have taken damage. Throne claim interrupted.");
                    playerTimers.remove(playerId);
                    this.cancel();
                    return;
                }

                // if player already owns this throne, just let them sit
                Ruler currentRuler = Tithe.getRulerOfNation(event.getCountry());
                if (currentRuler != null && currentRuler.getUuid().equals(playerId)) {
                    player.sendMessage(ChatColor.GREEN + "Heavy lies the crown.");
                    this.cancel();
                    return;
                }

                // check if player leaves the throne before claiming finishes
                if (!player.isInsideVehicle()) {
                    playerTimers.remove(playerId);
                    this.cancel();
                    return;
                }

                // loading screen
                if (count < 6) {
                    String loadingBar = "++++++".substring(0, count) + "------".substring(count);
                    player.sendTitle(ChatColor.YELLOW + loadingBar, ChatColor.GREEN + "Conquering...", 0, CONQUER_TIMER, 0);
                    event.broadcastThroneClaimProgressMessage(count);
                    count++;
                } else {
                    // once the loading is finished, update the rulers
                    event.broadcastConqueredMessage(player); // broadcast conquered message
                    Ruler oldRuler = Tithe.getRulers().get(event.country); // get old Ruler of now conquered throne

                    this.cancel(); // end timer
                    playerTimers.remove(playerId); // end the timer for the current conqueror

                    // Add or replace the player's entry in the rulers map
                    Ruler newRuler;
                    if (Tithe.isPlayerRuler(playerId)) { // if conqueror already exists
                        newRuler = Tithe.getRuler(playerId);
                    } else { // if conqueror is brand new to the game of thrones
                        newRuler = new Ruler(playerId, Bukkit.getOfflinePlayer(playerId).getName());
                    }

                    // crown the new ruler by replacing the map entry
                    event.promotePlayer(newRuler);

                    if (oldRuler != null) {
                        UUID oldRulerUUID = oldRuler.getUuid();

                        // Assume you have LuckPerms API already set up as luckPermsApi
                        User oldRulerUser = Tithe.luckPermsApi.getUserManager().getUser(oldRulerUUID);
                        if (oldRulerUser != null) {
                            if (oldRuler.getThroneCount().equals(3)) {
                                Node node = Node.builder("tithe.taxes").value(false).build();
                                Node essentialsNode = Node.builder("essentials.enderchest").value(false).build();
                                oldRulerUser.data().add(node);
                                oldRulerUser.data().add(essentialsNode);
                                Tithe.luckPermsApi.getUserManager().saveUser(oldRulerUser);  // Save the user data
                                Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.BOLD + ChatColor.YELLOW
                                        + oldRuler.getUsername() + ChatColor.WHITE
                                        + " has lost majority dominion and can no longer levy taxes.");
                                event.removeMonarchPermissions(oldRuler.getUuid());
                            }

                        }

                        Tithe.decrementPlayerThroneCount(oldRulerUUID);
                    }

                    Tithe.createBossBar();
                    if (newRuler.getThroneCount() >= 3){
                        event.addMonarchPermissions(newRuler.getUuid());
                        Bukkit.broadcastMessage(EXCLAMATION_MSG + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + player.getName() + ChatColor.WHITE + "" +
                                " is now the " + ChatColor.RED + "Monarch of Europe"
                                + ChatColor.WHITE + " and may now levy taxes.");
                        player.sendMessage(ChatColor.GREEN + "Welcome, Leader of Europe!");
                        player.sendMessage(ChatColor.YELLOW + " - Use /enderchest to view your royal coffers.");
                        player.sendMessage(ChatColor.YELLOW + " - Use /tithe settax 50% to set the GLOBAL tax rate.");
                        player.sendMessage(ChatColor.GREEN + "Watch out for inva");
                        Tithe.setBossBarColor(BarColor.RED);
                        Tithe.setBossBarMessage("Hail " + newRuler.getUsername() + "!");
                    }

                    else if(Tithe.getRuler(Tithe.getTopRuler()) == null){
                        Tithe.setBossBarColor(BarColor.PURPLE);
                        Tithe.setBossBarMessage("No current Monarch.");
                    }
                    Tithe.updateBossBarForAll();
                    addPlayerToRulerGroup(newRuler.getUuid());
                    if(oldRuler != null){
                        if(oldRuler.getThroneCount() == 0){
                            removePlayerFromRulerGroup(oldRuler.getUuid());
                        }
                    }
                }
            }
        };
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();

        // Cancel the timer if the player leaves the server
        if (playerTimers.containsKey(playerId)) {
            playerTimers.get(playerId).cancel();
            playerTimers.remove(playerId);
        }
    }
}

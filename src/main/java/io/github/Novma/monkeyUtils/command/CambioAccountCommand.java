package io.github.Novma.monkeyUtils.command;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import io.github.Novma.monkeyUtils.MonkeyUtils;
import io.github.Novma.monkeyUtils.log.Log;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CambioAccountCommand implements CommandExecutor {

    //todo aggiugere log esito positivo

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        // Check command
        if (!command.getName().equalsIgnoreCase("cambioaccount")) return true;

        //todo controllo permessi

        // Check args amount
        if (strings.length != 2) return false;

        // Get first player
        OfflinePlayer from = Bukkit.getPlayerExact(strings[0]);
        // Check if player online
        if (from == null) {
            commandSender.sendRichMessage("<red>Player " + strings[0] + "<red> non online");
            return true;
        }

        // Get second player
        OfflinePlayer to = Bukkit.getPlayerExact(strings[1]);
        // Check if existing player
        if (to == null) {
            commandSender.sendRichMessage("<red>Player " + strings[1] + "<red> non online");
            return true;
        }

        // Check if same player
        if (from.getUniqueId().equals(to.getUniqueId())) {
            commandSender.sendRichMessage("<red>Hai scritto due volte lo stesso player");
            return true;
        }

        // Get UUID of both players
        UUID ufrom = from.getUniqueId();
        UUID uto = to.getUniqueId();

        // Change permissions - LuckPerms
        luckPerms(ufrom, uto);

        // Change island properties - SuperiorSkyblock
        superiorSkyblock(ufrom, uto);

        // Change currency - Vault
        vault(from, to);

        // Change quarrys owners - Quarry
        quarry();

        // Change daily quests - ODailyQuests
        oDailyQuests(from, to);

        // Change regions membership / owner
//        worldGuard(ufrom, uto);

        return true;
    }

    // LuckPerms - permissions
    private void luckPerms(UUID ufrom, UUID uto) {
        try {
            // Get LuckPerms
            LuckPerms lp = (LuckPerms) Bukkit.getPluginManager().getPlugin("LuckPerms");
            // Get users
            CompletableFuture<User> futureFrom = Objects.requireNonNull(lp).getUserManager().loadUser(ufrom);
            CompletableFuture<User> futureTo = lp.getUserManager().loadUser(uto);
            // Wait futures
            futureFrom.thenCombineAsync(futureTo, (from, to) -> {
                // Clear permissions to
                to.data().clear();

                // Copy nodes from - to
                for (Node node : from.data().toCollection()) {
                    to.data().add(node);
                }

                // Set primary group
                to.setPrimaryGroup(from.getPrimaryGroup());

                return to;
            }).thenAcceptAsync(to -> lp.getUserManager().saveUser(to)); // Save user data
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di luckperms ha riscontrato dei problemi---");
            Log.severe(e.toString());
        }
    }

    // SuperiorSkyblock //todo controlla se è stato trasferito tutto il necessario
    private void superiorSkyblock(UUID ufrom, UUID uto) {
        try {
            // Run async
            Bukkit.getScheduler().runTaskAsynchronously(MonkeyUtils.getInstance(), () -> {
                // Get users
                SuperiorPlayer from = SuperiorSkyblockAPI.getPlayer(ufrom);
                SuperiorPlayer to = SuperiorSkyblockAPI.getPlayer(uto);

                // Run sync
                Bukkit.getScheduler().runTask(MonkeyUtils.getInstance(), () -> {
                    // Merge players (missions and player data)
                    to.merge(from);

                    // Get island
                    Island is = from.getIsland();
                    // Check if player is owner
                    if (is.getOwner().getUniqueId().compareTo(ufrom) == 0) {
                        // Transfer island ownership
                        is.transferIsland(to);
                    } else {
                        // Replace player
                        is.addMember(to, from.getPlayerRole());
                        is.banMember(from);
                    }
                });
            });
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di superiorskyblock ha riscontrato dei problemi---");
            Log.severe(e.toString());
        }
    }

    // Vault
    private void vault(OfflinePlayer from, OfflinePlayer to) {
        try {
            // Get economy
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) { // Check if null
                throw new Exception("Nessun plugin di economy");
            }
            Economy econ = rsp.getProvider(); // Set economy

            // Clear balance - to
            econ.withdrawPlayer(to, econ.getBalance(to));
            // Get balance - from
            double bal = econ.getBalance(from);
            // Set balance - to
            econ.depositPlayer(to, bal);
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di vault ha riscontrato dei problemi---");
            Log.severe(e.toString());
        }
    }

    // Quarry
    private void quarry() {

    }

    // ODailyQuests
    private void oDailyQuests(OfflinePlayer from, OfflinePlayer to) {
        try {
            // Get instance
            ODailyQuests odq = ODailyQuests.INSTANCE;

            // Get active quests
            Map<String, PlayerQuests> active = QuestsManager.getActiveQuests();
            // Get quests old player
            PlayerQuests quests = active.get(from.getName());
            // Add same quests to new player
            active.put(to.getName(), quests);
            // Save to database
            odq.getDatabaseManager().saveProgressionForPlayer(to.getName(), to.getUniqueId().toString(), quests);

        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di ODailyQuests ha riscontrato dei problemi---");
            Log.severe(e.toString());
        }
    }

    // WorldGuard //todo chiedere cosa hanno fatto con warldguard
//    private void worldGuard(UUID ufrom, UUID uto) {
//        try {
//            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//            RegionManager regions = container.get(world);
//            if (regions != null) {
//                return regions.getRegion("spawn");
//            } else {
//                // The world has no region support or region data failed to load
//            }
//        } catch (Exception e) {
//            // Log if an error occurred
//            Log.severe("---Il trasferimento di vault ha riscontrato dei problemi---");
//            Log.severe(e.toString());
//        }
//    }


}

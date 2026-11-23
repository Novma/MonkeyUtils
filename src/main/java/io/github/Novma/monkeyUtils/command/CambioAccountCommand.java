package io.github.Novma.monkeyUtils.command;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.ordwen.odailyquests.ODailyQuests;
import com.ordwen.odailyquests.quests.player.PlayerQuests;
import com.ordwen.odailyquests.quests.player.QuestsManager;
import com.ordwen.odailyquests.quests.player.progression.Progression;
import com.ordwen.odailyquests.quests.types.AbstractQuest;
import io.github.Novma.monkeyUtils.MonkeyUtils;
import io.github.Novma.monkeyUtils.log.FileLogger;
import io.github.Novma.monkeyUtils.log.Log;
import it.herohd.monkeyCore.MonkeyCore;
import it.herohd.monkeyCore.modules.harvester.HarvesterPlayer;
import it.herohd.monkeyCore.modules.skills.SkillPlayer;
import it.herohd.monkeyCore.modules.skills.SkillType;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.managers.PlayerDataManager;
import pk.ajneb97.model.PlayerDataKit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CambioAccountCommand implements CommandExecutor {
    private final Plugin plugin;
    //todo aggiugere log

    // Constructor
    public CambioAccountCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        // Check args amount
        if (strings.length != 2) return false;

        // Get first player
        Player from = Bukkit.getPlayerExact(strings[0]);
        // Check if player online
        if (from == null) {
            commandSender.sendRichMessage("<red>Player " + strings[0] + "<red> non online");
            return true;
        }
        if (!from.isOnline()) {
            commandSender.sendRichMessage("<red>Player " + strings[0] + "<red> non online");
            return true;
        }

        // Get second player
        Player to = Bukkit.getPlayerExact(strings[1]);
        // Check if existing player
        if (to == null) {
            commandSender.sendRichMessage("<red>Player " + strings[1] + "<red> non online");
            return true;
        }
        if (!to.isOnline()) {
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

        // Create log file
        FileLogger.setFile(from.getName(), to.getName());
        // Log UUIDs
        List<String> toLog = new ArrayList<>();
        toLog.add("from: " + ufrom.toString());
        toLog.add("to: " + uto.toString() + "\n");
        FileLogger.log(toLog);

        // Change permissions - LuckPerms
        luckPerms(ufrom, uto);

        // Change island properties - SuperiorSkyblock
        superiorSkyblock(ufrom, uto);

        // Change currency - Vault
        vault(from, to);

        // Change daily quests - ODailyQuests
        oDailyQuests(from, to);

        // Change player points - PlayerPoints
        playerPoints(ufrom, uto);

        // Change player kits - PlayerKits2
        playerKits(from, to);

        // Change inventory
        inventory(from, to);

        // Change player vaults - PlayerVaultsX
        playerVaults(ufrom, uto);

        // Change enderchest
        enderchest(from, to);

        // Change monkey skill / harvester - MonkeyCore
        monkeyCore(ufrom, uto);

        return true;
    }

    // LuckPerms - permissions
    private void luckPerms(UUID ufrom, UUID uto) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("LuckPerms");
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
                    // Log
                    toLog.add(node.getKey() + " - " + node.getValue());
                }

                // Set primary group
                to.setPrimaryGroup(from.getPrimaryGroup());
                // Log
                toLog.add("PRIMARY GROUP: " + from.getPrimaryGroup() + "\n");
                return to;
            }).thenAcceptAsync(to -> lp.getUserManager().saveUser(to)); // Save user data
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di luckperms ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // SuperiorSkyblock //todo controlla se è stato trasferito tutto il necessario
    private void superiorSkyblock(UUID ufrom, UUID uto) {
        List<String> toLog = new ArrayList<>();
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
                    // Log
                    toLog.add(to.getName() + " è adesso " + to.getPlayerRole().getName() + " di " + is.getName() + "\n");
                });
            });
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di superiorskyblock ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // Vault
    private void vault(OfflinePlayer from, OfflinePlayer to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("Vault");
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
            // Log
            toLog.add(bal + "\n");
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di vault ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // ODailyQuests
    private void oDailyQuests(OfflinePlayer from, OfflinePlayer to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Get instance
            ODailyQuests odq = ODailyQuests.INSTANCE;

            // Get active quests
            Map<String, PlayerQuests> active = QuestsManager.getActiveQuests();
            // Get quests from
            PlayerQuests quests = active.get(from.getName());
            // Log
            Map<AbstractQuest, Progression> q = quests.getQuests();
            for (Map.Entry<AbstractQuest, Progression> e : q.entrySet()) {
                toLog.add(e.getKey().getQuestName());
                toLog.add(e.getValue().getAdvancement() + "\n");
            }
            // Add same quests to
            active.put(to.getName(), quests);
            // Save to database
            odq.getDatabaseManager().saveProgressionForPlayer(to.getName(), to.getUniqueId().toString(), quests);

        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di ODailyQuests ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // PlayerPoints
    private void playerPoints(UUID from, UUID to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("PlayerPoints");
            // Get API
            PlayerPointsAPI api = PlayerPoints.getInstance().getAPI();

            // Give points to
            api.give(to, api.look(from));

            // Reset points from
            api.reset(from);
            // Log
            toLog.add(api.look(to) + "\n");
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di ODailyQuests ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // PlayerKits2
    private void playerKits(Player from, Player to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("PlayerKits2");
            // Get plugin
            PlayerKits2 pk = PlayerKitsAPI.getPlugin();

            // Get PlayerDataManager
            PlayerDataManager pdm = pk.getPlayerDataManager();

            // Log
            ArrayList<PlayerDataKit> kits = pdm.getPlayer(from, false).getKits();
            for (PlayerDataKit kit : kits) {
                // Get name from every kit
                toLog.add(kit.getName());
            }

            // Pipeline async
            CompletableFuture.runAsync(() -> {
                // Save current data
                pdm.manageLeave(from);
                pdm.manageLeave(to);
            }).thenRunAsync(() -> {
                // Get files
                File fileFrom = new File(pk.getDataFolder(), ""); // todo aggiungi percorsi file
                File fileTo = new File(pk.getDataFolder(), "");

                // Copy file
                try {
                    Files.copy(fileFrom.toPath(), fileTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File copiato con successo!");
                } catch (IOException e) {
                    // Log
                    toLog.add(e.toString());
                }
            }).thenRunAsync(() -> {
                // Load data
                pdm.manageJoin(from);
                pdm.manageJoin(to);
            });
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di PlayerKits2 ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // Inventory
    private void inventory(Player from, Player to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("Inventario");

            // Get inventories
            PlayerInventory fromInv = from.getInventory();
            PlayerInventory toInv = to.getInventory();

            // Set contents (0-35)
            ItemStack[] copy = fromInv.getContents();
            toInv.setContents(copy);
            // Log
            for (ItemStack item : copy)
                toLog.add(item.displayName().toString());

            // Set armor
            copy = fromInv.getArmorContents();
            toInv.setArmorContents(copy);
            // Log
            for (ItemStack item : copy)
                toLog.add(item.displayName().toString());

            // Set offhand
            ItemStack temp = fromInv.getItemInOffHand();
            toInv.setItemInOffHand(temp);
            // Log
            toLog.add(temp.displayName().toString() + "\n");

            // Update inventory
            to.updateInventory();
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento dell'inventario ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // PlayerVaultsX
    private void playerVaults(UUID from, UUID to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("PlayerVaults");

            // Get instance
            VaultManager vm = VaultManager.getInstance();

            // Get vaults numbers
            Set<Integer> numbers = vm.getVaultNumbers(from.toString());

            // For every pv
            for (Integer n : numbers) {
                // Get pv
                Inventory inv = vm.getVault(from.toString(), n);
                // Set pv
                vm.saveVault(inv, to.toString(), n);
                // Log
                for (ItemStack item : inv.getContents())
                    toLog.add(item.displayName().toString());
            }
            toLog.add("");
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di playerVaults ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // Enderchest
    private void enderchest(Player from, Player to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("Enderchest");

            // Get inventories
            Inventory fromInv = from.getEnderChest();
            Inventory toInv = to.getEnderChest();

            // Set contents (0-35)
            ItemStack[] copy = fromInv.getContents();
            toInv.setContents(copy);
            // Log
            for (ItemStack item : copy)
                toLog.add(item.displayName().toString());
            toLog.add("");
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento dell'enderchest ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }

    // MonkeyCore
    private void monkeyCore(UUID from, UUID to) {
        List<String> toLog = new ArrayList<>();
        try {
            // Log
            toLog.add("MonkeyCore");

            // Get plugin
            MonkeyCore pl = MonkeyCore.getInstance();

            // Get SkillPlayers
            SkillPlayer sfrom = pl.getSkills().getManager().getPlayer(from);
            SkillPlayer sto = pl.getSkills().getManager().getPlayer(to);

            // Get HarvesterPlayer
            HarvesterPlayer hfrom = pl.getHarvester().getManager().getPlayer(from);
            HarvesterPlayer hto = pl.getHarvester().getManager().getPlayer(to);

            // Get skills to
            Map<SkillType, Integer> skills = sto.getAllSkills();
            // Reset skills to
            skills.clear();
            // Copy skills
            for (Map.Entry<SkillType, Integer> e : sfrom.getAllSkills().entrySet()) {
                skills.put(e.getKey(), e.getValue());
                // Log
                toLog.add(e.getKey().getConfigKey() + ", " + e.getKey().getDisplayName() + " - " + e.getValue());
            }

            // Copy harvester
            hto.setLevel(hfrom.getLevel());
            hto.setPoints(hfrom.getPoints());
            // Log
            toLog.add("\nLevel: " + hfrom.getLevel());
            toLog.add("Points: " + hfrom.getPoints() + "\n");

            // Set dirty
            sto.setDirty(true);
            hto.setDirty(true);

            // Save player
            pl.getSkills().getManager().savePlayer(sto);
            pl.getHarvester().getManager().savePlayer(hto);
        } catch (Exception e) {
            // Log if an error occurred
            Log.severe("---Il trasferimento di MonkeyCore ha riscontrato dei problemi---");
            Log.severe(e.toString());
            toLog.add(e + "\n");
        }
        // Send log
        FileLogger.log(toLog);
    }
}

package io.github.Novma.monkeyUtils;

import io.github.Novma.monkeyUtils.command.CambioAccountCommand;
import io.github.Novma.monkeyUtils.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MonkeyUtils extends JavaPlugin {
    private static MonkeyUtils instance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        // Check plugins
        if (check()) {
            // Set command executor
            Objects.requireNonNull(getCommand("cambioaccount")).setExecutor(new CambioAccountCommand());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Check all necessary plugins
    private boolean check() {
        // Check LuckPerms
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            Log.severe("---LuckPerms non caricato---");
            return false;
        }
        // Check SuperiorSkyblock
        if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") == null) {
            Log.severe("---SuperiorSkyblock2 non caricato---");
            return false;
        }
        // Check Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            Log.severe("---Vault non caricato---");
            return false;
        }
        // Check WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            Log.severe("---WorldGuard non caricato---");
            return false;
        }
        // Check Quarry
        if (Bukkit.getPluginManager().getPlugin("Quarry") == null) {
            Log.severe("---Quarry non caricato---");
            return false;
        }
        // Check ODailyQuests
        if (Bukkit.getPluginManager().getPlugin("ODailyQuests") == null) {
            Log.severe("---ODailyQuests non caricato---");
            return false;
        }

        return true;
    }

    // Get an instance of this plugin
    public static MonkeyUtils getInstance() {
        return instance;
    }
}

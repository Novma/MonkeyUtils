package io.github.Novma.monkeyUtils;

import io.github.Novma.monkeyUtils.command.CambioAccountCommand;
import io.github.Novma.monkeyUtils.log.FileLogger;
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
            Objects.requireNonNull(getCommand("cambioaccount")).setExecutor(new CambioAccountCommand(this));
        }

        // Setup FileLogger
        FileLogger.setup(this);
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
        // Check ODailyQuests
        if (Bukkit.getPluginManager().getPlugin("ODailyQuests") == null) {
            Log.severe("---ODailyQuests non caricato---");
            return false;
        }
        // Check PlayerPoints
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            Log.severe("---PlayerPoints non caricato---");
            return false;
        }
        // Check PlayerKits2
        if (Bukkit.getPluginManager().getPlugin("PlayerKits2") == null) {
            Log.severe("---PlayerKits2 non caricato---");
            return false;
        }
        // Check PlayerVaultsX
        if (Bukkit.getPluginManager().getPlugin("PlayerVaultsX") == null) {
            Log.severe("---PlayerVaultsX non caricato---");
            return false;
        }
        // Check MonkeyCore
        if (Bukkit.getPluginManager().getPlugin("MonkeyCore") == null) {
            Log.severe("---MonkeyCore non caricato---");
            return false;
        }

        return true;
    }

    // Get an instance of this plugin
    public static MonkeyUtils getInstance() {
        return instance;
    }
}

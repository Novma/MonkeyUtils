package io.github.Novma.monkeyUtils.log;

import io.github.Novma.monkeyUtils.MonkeyUtils;

import java.util.logging.Level;

public class Log {
    private static MonkeyUtils plugin;

    // Set this plugin instance
    public static void setPlugin(MonkeyUtils plugin) {
        Log.plugin = plugin;
    }

    // Send a message to log if in debug mode
    public static void debug(Level level, String msg) {
        if (plugin.getConfig().getBoolean("debug-mode")) {
            plugin.getLogger().log(level, "[DEBUG] " + msg);
        }
    }

    // Send a message to log with level info
    public static void info(String msg) {
        plugin.getLogger().info(msg);
    }

    // Send a message to log with level warning
    public static void warning(String msg) {
        plugin.getLogger().warning(msg);
    }

    // Send a message to log with level severe
    public static void severe(String msg) {
        plugin.getLogger().severe(msg);
    }

    // Send a message to log
    public static void log(Level level, String msg) {
        plugin.getLogger().log(level, msg);
    }
}

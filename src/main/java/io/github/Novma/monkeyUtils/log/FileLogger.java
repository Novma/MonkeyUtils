package io.github.Novma.monkeyUtils.log;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.logging.*;

public class FileLogger {
    private static final Logger logger = Logger.getLogger("transferLogger");
    private static File logsFolder;
    private static FileHandler fh;
    private static Plugin plugin;

    // Setup the logger
    public static void setup(Plugin plugin) {
        FileLogger.plugin = plugin;
        logger.setUseParentHandlers(false);

        // Set logsFolder
        logsFolder = new File(plugin.getDataFolder(), "logs");
        // Check if logs folder exists
        if (!logsFolder.exists()) {
            // Create logs folder
            logsFolder.mkdirs();
        }
    }

    // Set a new file for the logger
    public static void setFile(String from, String to) {
        try {
            // Create log file
            File logFile = new File(logsFolder, from + "_" + to + "_" + Instant.now().toString());
            // Create file handler
            fh = new FileHandler(logFile.getAbsolutePath());
            // Set formatter
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + System.lineSeparator();
                }
            });
            // Add handler
            logger.addHandler(fh);
        } catch (IOException e) {
            Log.severe(e.toString());
        }
    }

    // Log in the file
    public static void log(List<String> log) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String msg : log) {
                logger.info(msg);
            }
        });
    }

    // Close file and erase file handler
    public static void close() {
        fh.close();
        logger.removeHandler(fh);
    }
}

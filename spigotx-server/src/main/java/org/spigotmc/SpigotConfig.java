package org.spigotmc;

import co.aikar.timings.Timings;
import co.aikar.timings.TimingsManager;
import com.minexd.spigot.config.SharedConfig;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.server.AttributeRanged;
import net.minecraft.server.GenericAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class SpigotConfig {

    public static YamlConfiguration config;
    static Map<String, Command> commands;

    public static void init() {
        config = SharedConfig.config;
        commands = SharedConfig.commands;
        SharedConfig.readConfig( SpigotConfig.class, null );
    }

    private static boolean getBoolean(String path, boolean def) {
        path = "spigot." + path;
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    private static int getInt(String path, int def) {
        path = "spigot." + path;
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }

    private static <T> List getList(String path, T def) {
        path = "spigot." + path;
        config.addDefault(path, def);
        return (List<T>) config.getList(path, config.getList(path));
    }

    private static String getString(String path, String def) {
        path = "spigot." + path;
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }

    private static double getDouble(String path, double def) {
        path = "spigot." + path;
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    public static boolean logCommands;

    private static void logCommands() {
        logCommands = getBoolean("commands.log", true);
    }

    public static int tabComplete;

    private static void tabComplete() {
        tabComplete = getInt("commands.tab-complete", 0);
    }

    public static String whitelistMessage;
    public static String unknownCommandMessage;
    public static String serverFullMessage;
    public static String outdatedClientMessage = "Outdated client! Please use {0}";
    public static String outdatedServerMessage = "Outdated server! I\'m still on {0}";

    private static String transform(String s) {
        return ChatColor.translateAlternateColorCodes('&', s).replaceAll("\\n", "\n");
    }

    private static void messages() {
        whitelistMessage = transform(getString("messages.whitelist", "You are not whitelisted."));
        unknownCommandMessage = transform(getString("messages.unknown-command", "Unknown command."));
        serverFullMessage = transform(getString("messages.server-full", "The server is full."));
        outdatedClientMessage = transform(getString("messages.outdated-client", outdatedClientMessage));
        outdatedServerMessage = transform(getString("messages.outdated-server", outdatedServerMessage));
    }

    public static int timeoutTime = 60;
    public static boolean restartOnCrash = true;
    public static String restartScript = "./start.sh";
    public static String restartMessage;

    private static void watchdog() {
        timeoutTime = getInt("settings.timeout-time", timeoutTime);
        restartOnCrash = getBoolean("settings.restart-on-crash", restartOnCrash);
        restartScript = getString("settings.restart-script", restartScript);
        restartMessage = transform(getString("messages.restart", "Server is restarting"));
        commands.put("restart", new RestartCommand("restart"));
        WatchdogThread.doStart(timeoutTime, restartOnCrash);
    }

    public static boolean bungee;

    private static void bungee() {
        bungee = getBoolean("settings.bungeecord", false);
    }

    private static void timings() {
        boolean timings = getBoolean("timings.enabled", true);
        boolean verboseTimings = getBoolean("timings.verbose", true);
        TimingsManager.privacy = getBoolean("timings.server-name-privacy", false);
        TimingsManager.hiddenConfigs = getList("timings.hidden-config-entries", Arrays.asList("database", "settings.bungeecord-addresses"));
        int timingHistoryInterval = getInt("timings.history-interval", 300);
        int timingHistoryLength = getInt("timings.history-length", 3600);


        Timings.setVerboseTimingsEnabled(verboseTimings);
        Timings.setTimingsEnabled(timings);
        Timings.setHistoryInterval(timingHistoryInterval * 20);
        Timings.setHistoryLength(timingHistoryLength * 20);
    }

    private static void nettyThreads() {
        int count = getInt("settings.netty-threads", 5);
        System.setProperty("io.netty.eventLoopThreads", Integer.toString(count));
    }

    public static boolean lateBind;

    private static void lateBind() {
        lateBind = getBoolean("settings.late-bind", false);
    }

    public static boolean disableStatSaving;
    public static TObjectIntHashMap<String> forcedStats = new TObjectIntHashMap<>();

    private static void stats() {
        disableStatSaving = getBoolean("stats.disable-saving", false);

        if ( !config.contains( "spigot.stats.forced-stats" ) ) {
            config.createSection( "spigot.stats.forced-stats" );
        }

        ConfigurationSection section = config.getConfigurationSection( "spigot.stats.forced-stats" );

        for (String name : section.getKeys(true)) {
            if (section.isInt(name)) {
                forcedStats.put(name, section.getInt(name));
            }
        }
    }

    private static void tpsCommand() {
        commands.put("tps", new TicksPerSecondCommand("tps"));
    }

    public static int playerSample;

    private static void playerSample() {
        playerSample = getInt("settings.sample-count", 12);
    }

    public static int playerShuffle;

    private static void playerShuffle() {
        playerShuffle = getInt("settings.player-shuffle", 0);
    }

    public static List<String> spamExclusions;

    private static void spamExclusions() {
        spamExclusions = getList("commands.spam-exclusions", Arrays.asList(new String[]{"/skill"}));
    }

    public static boolean silentCommandBlocks;

    private static void silentCommandBlocks() {
        silentCommandBlocks = getBoolean("commands.silent-commandblock-console", false);
    }

    public static boolean filterCreativeItems;

    private static void filterCreativeItems() {
        filterCreativeItems = getBoolean("settings.filter-creative-items", true);
    }

    public static Set<String> replaceCommands;

    private static void replaceCommands() {
        replaceCommands = new HashSet<>((List<String>) getList("commands.replace-commands", Arrays.asList("setblock", "summon", "testforblock", "tellraw")));
    }

    public static int userCacheCap;

    private static void userCacheCap() {
        userCacheCap = getInt("settings.user-cache-size", 1000);
    }

    public static boolean saveUserCacheOnStopOnly;

    private static void saveUserCacheOnStopOnly() {
        saveUserCacheOnStopOnly = getBoolean("settings.save-user-cache-on-stop-only", false);
    }

    public static int intCacheLimit;

    private static void intCacheLimit() {
        intCacheLimit = getInt("settings.int-cache-limit", 1024);
    }

    public static double movedWronglyThreshold;

    private static void movedWronglyThreshold() {
        movedWronglyThreshold = getDouble("settings.moved-wrongly-threshold", 0.0625D);
    }

    public static double movedTooQuicklyThreshold;

    private static void movedTooQuicklyThreshold() {
        movedTooQuicklyThreshold = getDouble("settings.moved-too-quickly-threshold", 100.0D);
    }

    public static double maxHealth = 2048;
    public static double movementSpeed = 2048;
    public static double attackDamage = 2048;

    private static void attributeMaxes() {
        maxHealth = getDouble("settings.attribute.maxHealth.max", maxHealth);
        ((AttributeRanged) GenericAttributes.maxHealth).b = maxHealth;
        movementSpeed = getDouble("settings.attribute.movementSpeed.max", movementSpeed);
        ((AttributeRanged) GenericAttributes.MOVEMENT_SPEED).b = movementSpeed;
        attackDamage = getDouble("settings.attribute.attackDamage.max", attackDamage);
        ((AttributeRanged) GenericAttributes.ATTACK_DAMAGE).b = attackDamage;
    }

    public static boolean debug;

    private static void debug() {
        debug = getBoolean("settings.debug", false);

        if (debug && !LogManager.getRootLogger().isTraceEnabled()) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration conf = ctx.getConfiguration();
            conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(org.apache.logging.log4j.Level.ALL);
            ctx.updateLoggers(conf);
        }
    }

}

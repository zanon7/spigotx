package org.spigotmc;

import com.minexd.spigot.config.SharedConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class SpigotWorldConfig {

    private static YamlConfiguration config;
    private static boolean verbose;

    public static void init() {
        config = SharedConfig.config;
        verbose = getBoolean( "verbose", true );
        SharedConfig.readConfig( SpigotWorldConfig.class, null );
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault( "spigot.world-settings." + path, def );
        return config.getBoolean( "spigot.world-settings." + path, config.getBoolean( "spigot.world-settings." + path ) );
    }

    private static double getDouble(String path, double def) {
        config.addDefault( "spigot.world-settings." + path, def );
        return config.getDouble( "spigot.world-settings." + path, config.getDouble( "spigot.world-settings." + path ) );
    }

    private static int getInt(String path, int def) {
        config.addDefault( "spigot.world-settings." + path, def );
        return config.getInt( "spigot.world-settings." + path, config.getInt( "spigot.world-settings." + path ) );
    }

    private static  <T> List getList(String path, T def) {
        config.addDefault( "spigot.world-settings." + path, def );
        return (List<T>) config.getList( "spigot.world-settings." + path, config.getList( "spigot.world-settings." + path ) );
    }

    private static String getString(String path, String def) {
        config.addDefault( "spigot.world-settings." + path, def );
        return config.getString( "spigot.world-settings." + path, config.getString( "spigot.world-settings." + path ) );
    }

    public static int chunksPerTick;
    public static boolean clearChunksOnTick;
    private static void chunksPerTick() {
        chunksPerTick = getInt("chunks-per-tick", 650);
        clearChunksOnTick = getBoolean("clear-tick-list", false);
    }

    // Crop growth rates
    public static int cactusModifier;
    public static int caneModifier;
    public static int melonModifier;
    public static int mushroomModifier;
    public static int pumpkinModifier;
    public static int saplingModifier;
    public static int wheatModifier;
    public static int wartModifier;
    private static int getAndValidateGrowth(String crop) {
        int modifier = getInt("growth." + crop.toLowerCase() + "-modifier", 100);

        if (modifier == 0) {
            modifier = 100;
        }

        return modifier;
    }

    private static void growthModifiers() {
        cactusModifier = getAndValidateGrowth("Cactus");
        caneModifier = getAndValidateGrowth("Cane");
        melonModifier = getAndValidateGrowth("Melon");
        mushroomModifier = getAndValidateGrowth("Mushroom");
        pumpkinModifier = getAndValidateGrowth("Pumpkin");
        saplingModifier = getAndValidateGrowth("Sapling");
        wheatModifier = getAndValidateGrowth("Wheat");
        wartModifier = getAndValidateGrowth("NetherWart");
    }

    public static double itemMerge;
    private static void itemMerge() {
        itemMerge = getDouble("merge-radius.item", 2.5);
    }

    public static double expMerge;
    private static void expMerge() {
        expMerge = getDouble("merge-radius.exp", 3.0);
    }

    public static int viewDistance;
    private static void viewDistance() {
        viewDistance = getInt("view-distance", Bukkit.getViewDistance());
    }

    public static byte mobSpawnRange;
    private static void mobSpawnRange() {
        mobSpawnRange = (byte) getInt("mob-spawn-range", 4);
    }

    public static int animalActivationRange = 32;
    public static int monsterActivationRange = 32;
    public static int miscActivationRange = 16;
    private static void activationRange() {
        animalActivationRange = getInt("entity-activation-range.animals", animalActivationRange);
        monsterActivationRange = getInt("entity-activation-range.monsters", monsterActivationRange);
        miscActivationRange = getInt("entity-activation-range.misc", miscActivationRange);
    }

    public static int playerTrackingRange = 48;
    public static int animalTrackingRange = 48;
    public static int monsterTrackingRange = 48;
    public static int miscTrackingRange = 32;
    public static int otherTrackingRange = 64;
    private static void trackingRange() {
        playerTrackingRange = getInt("entity-tracking-range.players", playerTrackingRange);
        animalTrackingRange = getInt("entity-tracking-range.animals", animalTrackingRange);
        monsterTrackingRange = getInt("entity-tracking-range.monsters", monsterTrackingRange);
        miscTrackingRange = getInt("entity-tracking-range.misc", miscTrackingRange);
        otherTrackingRange = getInt("entity-tracking-range.other", otherTrackingRange);
    }

    public static int hopperTransfer;
    public static int hopperCheck;
    public static int hopperAmount;
    private static void hoppers() {
        // Set the tick delay between hopper item movements
        hopperTransfer = getInt("ticks-per.hopper-transfer", 8);
        // Set the tick delay between checking for items after the associated
        // container is empty. Default to the hopperTransfer value to prevent
        // hopper sorting machines from becoming out of sync.
        hopperCheck = getInt("ticks-per.hopper-check", hopperTransfer);
        hopperAmount = getInt("hopper-amount", 1);
    }

    public static boolean randomLightUpdates;
    private static void lightUpdates() {
        randomLightUpdates = getBoolean("random-light-updates", false);
    }

    public static boolean saveStructureInfo;
    private static void structureInfo() {
        saveStructureInfo = getBoolean("save-structure-info", true);
    }

    public static int itemDespawnRate;
    private static void itemDespawnRate() {
        itemDespawnRate = getInt("item-despawn-rate", 6000);
    }

    public static int arrowDespawnRate;
    private static void arrowDespawnRate() {
        arrowDespawnRate = getInt("arrow-despawn-rate", 1200);
    }

    public static int engineMode;
    public static List<Integer> hiddenBlocks;
    public static List<Integer> replaceBlocks;

    public static boolean zombieAggressiveTowardsVillager;
    private static void zombieAggressiveTowardsVillager() {
        zombieAggressiveTowardsVillager = getBoolean("zombie-aggressive-towards-villager", true);
    }

    public static boolean nerfSpawnerMobs;
    private static void nerfSpawnerMobs() {
        nerfSpawnerMobs = getBoolean("nerf-spawner-mobs", false);
    }

    public static boolean enableZombiePigmenPortalSpawns;
    private static void enableZombiePigmenPortalSpawns() {
        enableZombiePigmenPortalSpawns = getBoolean("enable-zombie-pigmen-portal-spawns", true);
    }

    public static int maxBulkChunk;
    private static void bulkChunkCount() {
        maxBulkChunk = getInt("max-bulk-chunks", 10);
    }

    public static int maxCollisionsPerEntity;
    private static void maxEntityCollision() {
        maxCollisionsPerEntity = getInt("max-entity-collisions", 8);
    }

    public static int dragonDeathSoundRadius;
    private static void keepDragonDeathPerWorld() {
        dragonDeathSoundRadius = getInt("dragon-death-sound-radius", 0);
    }

    public static int witherSpawnSoundRadius;
    private static void witherSpawnSoundRadius() {
        witherSpawnSoundRadius = getInt("wither-spawn-sound-radius", 0);
    }

    public static int villageSeed;
    public static int largeFeatureSeed;
    private static void initWorldGenSeeds() {
        villageSeed = getInt("seed-village", 10387312);
        largeFeatureSeed = getInt("seed-feature", 14357617);
    }

    public static float walkExhaustion;
    public static float sprintExhaustion;
    public static float combatExhaustion;
    public static float regenExhaustion;
    private static void initHunger() {
        walkExhaustion = (float) getDouble("hunger.walk-exhaustion", 0.2);
        sprintExhaustion = (float) getDouble("hunger.sprint-exhaustion", 0.8);
        combatExhaustion = (float) getDouble("hunger.combat-exhaustion", 0.3);
        regenExhaustion = (float) getDouble("hunger.regen-exhaustion", 3);
    }

    public static int currentPrimedTnt = 0;
    public static int maxTntTicksPerTick;
    private static void maxTntPerTick() {
        maxTntTicksPerTick = getInt("max-tnt-per-tick", 100);
    }

    public static int hangingTickFrequency;
    private static void hangingTickFrequency() {
        hangingTickFrequency = getInt("hanging-tick-frequency", 100);
    }

    public static int tileMaxTickTime;
    public static int entityMaxTickTime;
    private static void maxTickTimes() {
        tileMaxTickTime = getInt("max-tick-time.tile", 50);
        entityMaxTickTime = getInt("max-tick-time.entity", 50);
    }

}

package org.github.paperspigot;

import com.minexd.spigot.config.SharedConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class PaperSpigotWorldConfig {

    private static YamlConfiguration config;
    private static boolean verbose;

    public static void init() {
        config = SharedConfig.config;
        verbose = getBoolean( "verbose", true );
        SharedConfig.readConfig( PaperSpigotWorldConfig.class, null );
    }

    private static boolean getBoolean(String path, boolean def) {
        config.addDefault( "paper.world-settings." + path, def );
        return config.getBoolean( "paper.world-settings." + path, config.getBoolean( "paper.world-settings." + path ) );
    }

    private static double getDouble(String path, double def) {
        config.addDefault( "paper.world-settings." + path, def );
        return config.getDouble( "paper.world-settings." + path, config.getDouble( "paper.world-settings." + path ) );
    }

    private static int getInt(String path, int def) {
        config.addDefault( "paper.world-settings." + path, def );
        return config.getInt( "paper.world-settings." + path, config.getInt( "paper.world-settings." + path ) );
    }

    private static float getFloat(String path, float def) {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble( path, (double) def );
    }

    private static <T> List getList(String path, T def) {
        config.addDefault( "paper.world-settings." + path, def );
        return (List<T>) config.getList( "paper.world-settings." + path, config.getList( "paper.world-settings." + path ) );
    }

    private static String getString(String path, String def) {
        config.addDefault( "paper.world-settings." + path, def );
        return config.getString( "paper.world-settings." + path, config.getString( "paper.world-settings." + path ) );
    }

    public static boolean allowUndeadHorseLeashing;
    private static void allowUndeadHorseLeashing() {
        allowUndeadHorseLeashing = getBoolean( "allow-undead-horse-leashing", false );
    }

    public static double squidMinSpawnHeight;
    public static double squidMaxSpawnHeight;
    private static void squidSpawnHeight() {
        squidMinSpawnHeight = getDouble( "squid-spawn-height.minimum", 45.0D );
        squidMaxSpawnHeight = getDouble( "squid-spawn-height.maximum", 63.0D );
    }

    public static float playerBlockingDamageMultiplier;
    private static void playerBlockingDamageMultiplier() {
        playerBlockingDamageMultiplier = getFloat( "player-blocking-damage-multiplier", 0.5F );
    }

    public static int cactusMaxHeight;
    public static int reedMaxHeight;
    private static void blockGrowthHeight() {
        cactusMaxHeight = getInt( "max-growth-height.cactus", 3 );
        reedMaxHeight = getInt( "max-growth-height.reeds", 3 );
    }

    public static int fishingMinTicks;
    public static int fishingMaxTicks;
    private static void fishingTickRange() {
        fishingMinTicks = getInt( "fishing-time-range.MinimumTicks", 100 );
        fishingMaxTicks = getInt( "fishing-time-range.MaximumTicks", 900 );
    }

    public static float blockBreakExhaustion;
    public static float playerSwimmingExhaustion;
    private static void exhaustionValues() {
        blockBreakExhaustion = getFloat( "player-exhaustion.block-break", 0.025F );
        playerSwimmingExhaustion = getFloat( "player-exhaustion.swimming", 0.015F );
    }

    public static int softDespawnDistance;
    public static int hardDespawnDistance;
    private static void despawnDistances() {
        softDespawnDistance = getInt("despawn-ranges.soft", 32);
        hardDespawnDistance = getInt("despawn-ranges.hard", 128);

        if (softDespawnDistance > hardDespawnDistance) {
            softDespawnDistance = hardDespawnDistance;
        }

        softDespawnDistance = softDespawnDistance * softDespawnDistance;
        hardDespawnDistance = hardDespawnDistance * hardDespawnDistance;
    }

    public static boolean keepSpawnInMemory;
    private static void keepSpawnInMemory() {
        keepSpawnInMemory = getBoolean( "keep-spawn-loaded", true);
    }

    public static int fallingBlockHeightNerf;
    private static void fallingBlockheightNerf() {
        fallingBlockHeightNerf = getInt( "falling-block-height-nerf", 0);
    }

    public static int tntEntityHeightNerf;
    private static void tntEntityHeightNerf() {
        tntEntityHeightNerf = getInt( "tnt-entity-height-nerf", 0);
    }

    public static int waterOverLavaFlowSpeed;
    private static void waterOverLavaFlowSpeed() {
        waterOverLavaFlowSpeed = getInt( "water-over-lava-flow-speed", 5 );
    }

    public static boolean removeInvalidMobSpawnerTEs;
    private static void removeInvalidMobSpawnerTEs() {
        removeInvalidMobSpawnerTEs = getBoolean( "remove-invalid-mob-spawner-tile-entities", true );
    }

    public static boolean removeUnloadedEnderPearls;
    public static boolean removeUnloadedTNTEntities;
    public static boolean removeUnloadedFallingBlocks;
    private static void removeUnloaded() {
        removeUnloadedEnderPearls = getBoolean( "remove-unloaded.enderpearls", true );
        removeUnloadedTNTEntities = getBoolean( "remove-unloaded.tnt-entities", true );
        removeUnloadedFallingBlocks = getBoolean( "remove-unloaded.falling-blocks", true );
    }

    public static boolean boatsDropBoats;
    public static boolean disablePlayerCrits;
    public static boolean disableChestCatDetection;
    private static void mechanicsChanges() {
        boatsDropBoats = getBoolean( "game-mechanics.boats-drop-boats", false );
        disablePlayerCrits = getBoolean( "game-mechanics.disable-player-crits", false );
        disableChestCatDetection = getBoolean( "game-mechanics.disable-chest-cat-detection", false );
    }

    public static boolean netherVoidTopDamage;
    private static void nethervoidTopDamage() {
        netherVoidTopDamage = getBoolean( "nether-ceiling-void-damage", false );
    }

    public static int tickNextTickCap;
    public static boolean tickNextTickListCapIgnoresRedstone;
    private static void tickNextTickCap() {
        tickNextTickCap = getInt( "tick-next-tick-list-cap", 10000 ); // Higher values will be friendlier to vanilla style mechanics (to a point) but may hurt performance
        tickNextTickListCapIgnoresRedstone = getBoolean( "tick-next-tick-list-cap-ignores-redstone", false ); // Redstone TickNextTicks will always bypass the preceding cap.
    }

    public static boolean useAsyncLighting;
    private static void useAsyncLighting() {
        useAsyncLighting = getBoolean( "use-async-lighting", true );
    }

    public static boolean disableEndCredits;
    private static void disableEndCredits() {
        disableEndCredits = getBoolean( "game-mechanics.disable-end-credits", false );
    }

    public static boolean loadUnloadedEnderPearls;
    public static boolean loadUnloadedTNTEntities;
    public static boolean loadUnloadedFallingBlocks;
    private static void loadUnloaded() {
        loadUnloadedEnderPearls = getBoolean( "load-chunks.enderpearls", false );
        loadUnloadedTNTEntities = getBoolean( "load-chunks.tnt-entities", false );
        loadUnloadedFallingBlocks = getBoolean( "load-chunks.falling-blocks", false );
    }

    public static boolean generateCanyon;
    public static boolean generateCaves;
    public static boolean generateDungeon;
    public static boolean generateFortress;
    public static boolean generateMineshaft;
    public static boolean generateMonument;
    public static boolean generateStronghold;
    public static boolean generateTemple;
    public static boolean generateVillage;
    public static boolean generateFlatBedrock;
    private static void generatorSettings() {
        generateCanyon = getBoolean( "generator-settings.canyon", true );
        generateCaves = getBoolean( "generator-settings.caves", true );
        generateDungeon = getBoolean( "generator-settings.dungeon", true );
        generateFortress = getBoolean( "generator-settings.fortress", true );
        generateMineshaft = getBoolean( "generator-settings.mineshaft", true );
        generateMonument = getBoolean( "generator-settings.monument", true );
        generateStronghold = getBoolean( "generator-settings.stronghold", true );
        generateTemple = getBoolean( "generator-settings.temple", true );
        generateVillage = getBoolean( "generator-settings.village", true );
        generateFlatBedrock = getBoolean( "generator-settings.flat-bedrock", false );
    }

    public static boolean fixCannons;
    private static void fixCannons() {
        fixCannons = getBoolean( "fix-cannons", false );
    }

    public static boolean fallingBlocksCollideWithSigns;
    private static void fallingBlocksCollideWithSigns() {
        fallingBlocksCollideWithSigns = getBoolean( "falling-blocks-collide-with-signs", false );
    }

    public static boolean optimizeExplosions;
    private static void optimizeExplosions() {
        optimizeExplosions = getBoolean( "optimize-explosions", true );
    }

    public static boolean fastDrainLava;
    public static boolean fastDrainWater;
    private static void fastDraining() {
        fastDrainLava = getBoolean( "fast-drain.lava", false );
        fastDrainWater = getBoolean( "fast-drain.water", false );
    }

    public static int lavaFlowSpeedNormal;
    public static int lavaFlowSpeedNether;
    private static void lavaFlowSpeed() {
        lavaFlowSpeedNormal = getInt( "lava-flow-speed.normal", 30 );
        lavaFlowSpeedNether = getInt( "lava-flow-speed.nether", 10 );
    }

    public static boolean disableExplosionKnockback;
    private static void disableExplosionKnockback() {
        disableExplosionKnockback = getBoolean( "disable-explosion-knockback", false );
    }

    public static boolean disableThunder;
    private static void disableThunder() {
        disableThunder = getBoolean( "disable-thunder", true );
    }

    public static boolean disableIceAndSnow;
    private static void disableIceAndSnow() {
        disableIceAndSnow = getBoolean( "disable-ice-and-snow", true );
    }

    public static boolean disableMoodSounds;
    private static void disableMoodSounds() {
        disableMoodSounds = getBoolean( "disable-mood-sounds", false );
    }

    public static int mobSpawnerTickRate;
    private static void mobSpawnerTickRate() {
        mobSpawnerTickRate = getInt( "mob-spawner-tick-rate", 1 );
    }

    public static boolean cacheChunkMaps;
    private static void cacheChunkMaps() {
        cacheChunkMaps = getBoolean( "cache-chunk-maps", false );
    }

    public static int containerUpdateTickRate;
    private static void containerUpdateTickRate() {
        containerUpdateTickRate = getInt( "container-update-tick-rate", 1 );
    }

    public static float tntExplosionVolume;
    private static void tntExplosionVolume() {
        tntExplosionVolume = getFloat( "tnt-explosion-volume", 4.0F );
    }

    public static boolean useHopperCheck;
    private static void useHopperCheck() {
        useHopperCheck = getBoolean( "use-hopper-check", false );
    }

    public static boolean allChunksAreSlimeChunks;
    private static void allChunksAreSlimeChunks() {
        allChunksAreSlimeChunks = getBoolean( "all-chunks-are-slime-chunks", false );
    }

    public static boolean allowBlockLocationTabCompletion;
    private static void allowBlockLocationTabCompletion() {
        allowBlockLocationTabCompletion = getBoolean( "allow-block-location-tab-completion", true );
    }

    public static int portalSearchRadius;
    private static void portalSearchRadius() {
        portalSearchRadius = getInt("portal-search-radius", 128);
    }

    public static boolean disableTeleportationSuffocationCheck;
    private static void disableTeleportationSuffocationCheck() {
        disableTeleportationSuffocationCheck = getBoolean("disable-teleportation-suffocation-check", false);
    }
}

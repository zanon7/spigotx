package org.github.paperspigot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.minexd.spigot.config.SharedConfig;
import net.minecraft.server.Items;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

public class PaperSpigotConfig
{
    public static YamlConfiguration config;
    static Map<String, Command> commands;

    public static void init()
    {
        config = SharedConfig.config;
        commands = SharedConfig.commands;
        SharedConfig.readConfig( PaperSpigotConfig.class, null );
    }

    private static boolean getBoolean(String path, boolean def)
    {
        path = "paper." + path;
        config.addDefault( path, def );
        return config.getBoolean( path, config.getBoolean( path ) );
    }

    private static double getDouble(String path, double def)
    {
        path = "paper." + path;
        config.addDefault( path, def );
        return config.getDouble( path, config.getDouble( path ) );
    }

    private static int getInt(String path, int def)
    {
        path = "paper." + path;
        config.addDefault( path, def );
        return config.getInt( path, config.getInt( path ) );
    }

    private static <T> List getList(String path, T def)
    {
        path = "paper." + path;
        config.addDefault( path, def );
        return (List<T>) config.getList( path, config.getList( path ) );
    }

    private static String getString(String path, String def)
    {
        path = "paper." + path;
        config.addDefault( path, def );
        return config.getString( path, config.getString( path ) );
    }

    public static double babyZombieMovementSpeed;
    private static void babyZombieMovementSpeed()
    {
        babyZombieMovementSpeed = getDouble( "settings.baby-zombie-movement-speed", 0.5D ); // Player moves at 0.1F, for reference
    }

    public static boolean interactLimitEnabled;
    private static void interactLimitEnabled()
    {
        interactLimitEnabled = getBoolean( "settings.limit-player-interactions", true );
    }

    public static double strengthEffectModifier;
    public static double weaknessEffectModifier;
    private static void effectModifiers()
    {
        strengthEffectModifier = getDouble( "effect-modifiers.strength", 1.3D );
        weaknessEffectModifier = getDouble( "effect-modifiers.weakness", -0.5D );
    }

    public static Set<Integer> dataValueAllowedItems;
    private static void dataValueAllowedItems()
    {
        dataValueAllowedItems = new HashSet<Integer>( getList( "data-value-allowed-items", Collections.emptyList() ) );
    }

    public static boolean stackableLavaBuckets;
    public static boolean stackableWaterBuckets;
    public static boolean stackableMilkBuckets;
    private static void stackableBuckets()
    {
        stackableLavaBuckets = getBoolean( "stackable-buckets.lava", false );
        stackableWaterBuckets = getBoolean( "stackable-buckets.water", false );
        stackableMilkBuckets = getBoolean( "stackable-buckets.milk", false );

        Field maxStack;

        try {
            maxStack = Material.class.getDeclaredField("maxStack");
            maxStack.setAccessible(true);

            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(maxStack, maxStack.getModifiers() & ~Modifier.FINAL);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            if (stackableLavaBuckets) {
                maxStack.set(Material.LAVA_BUCKET, Material.BUCKET.getMaxStackSize());
                Items.LAVA_BUCKET.c(Material.BUCKET.getMaxStackSize());
            }

            if (stackableWaterBuckets) {
                maxStack.set(Material.WATER_BUCKET, Material.BUCKET.getMaxStackSize());
                Items.WATER_BUCKET.c(Material.BUCKET.getMaxStackSize());
            }

            if (stackableMilkBuckets) {
                maxStack.set(Material.MILK_BUCKET, Material.BUCKET.getMaxStackSize());
                Items.MILK_BUCKET.c(Material.BUCKET.getMaxStackSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean warnForExcessiveVelocity;
    private static void excessiveVelocityWarning()
    {
        warnForExcessiveVelocity = getBoolean("warnWhenSettingExcessiveVelocity", true);
    }
}

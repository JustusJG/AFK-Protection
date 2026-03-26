package me.Gizzarduhh.afkProtection.hook;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.Gizzarduhh.afkProtection.AFKProtection;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private static AFKProtection plugin;

    public static BooleanFlag AFK_ZONE = new BooleanFlag("afk-zone");
    public static StateFlag AFK = new StateFlag("afk", true);

    public static void init(AFKProtection plugin) {
        WorldGuardHook.plugin = plugin;

        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(AFK_ZONE);
            registry.register(AFK);
        } catch (FlagConflictException ignored) {
            System.out.println(ignored);
        }
    }

    public static boolean afkZone(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(localPlayer.getLocation());

        return Boolean.TRUE.equals(set.queryValue(localPlayer, AFK_ZONE));
    }

    public static boolean afkAllowed(Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(localPlayer.getLocation());

        return set.testState(localPlayer, AFK);
    }
}

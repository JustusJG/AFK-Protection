package me.Gizzarduhh.afkProtection;

import me.Gizzarduhh.afkProtection.hook.LuckPermsAPI;
import me.Gizzarduhh.afkProtection.hook.PlaceholderAPIExpansion;
import me.Gizzarduhh.afkProtection.hook.WorldGuardHook;
import me.Gizzarduhh.afkProtection.listener.PlayerListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public final class AFKProtection extends JavaPlugin {
    public final List<UUID> afkPlayerList = new ArrayList<>();
    public final Map<UUID, Long> countdowns = new HashMap<>();

    public long countdownStart = 120000;
    public long kickThreshold = -1;
    private long lastUpdateInMilliseconds = 0;

    private LuckPermsAPI luckPermsAPI;
    private Configuration config;

    private boolean worldGuard = false;

    public long countdown(Player player) {
        return countdowns.getOrDefault(player.getUniqueId(), countdownStart);
    }

    public void resetCountdown(Player player) {
        boolean afkZone = worldGuard && WorldGuardHook.afkZone(player);

        if (!afkZone) {
            countdowns.put(player.getUniqueId(), countdownStart);
        } else {
            countdowns.put(player.getUniqueId(), -1L);
        }
    }

    @Override
    public void onLoad() {
        try {
            WorldGuardHook.init(this);
            worldGuard = true;
        } catch (NoClassDefFoundError ignored) {}
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();

        // LuckPerms API
        if (getServer().getPluginManager().isPluginEnabled("LuckPerms"))
            luckPermsAPI = new LuckPermsAPI(this);

        // PlaceholderAPI
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPIExpansion placeholderAPIExpansion = new PlaceholderAPIExpansion(this);
            placeholderAPIExpansion.register();
        }

        // Listener and Timer
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        countdownStart = config.getLong("afk.timer", 120) * 1000;
        kickThreshold = config.getLong("afk.kickTimer", 120) * 1000;
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Date date = new Date();
            long nowMillisecondsTimestamp = date.getTime();
            long deltaMilliseconds = nowMillisecondsTimestamp - lastUpdateInMilliseconds;

            this.getServer().getOnlinePlayers().forEach((player -> {
                if (player.hasPermission("afkprotection.bypass"))
                    return;
                if (worldGuard && !WorldGuardHook.afkAllowed(player)) {
                    this.setAfkStatus(player, false);
                    return;
                }
                UUID playerUUID = player.getUniqueId();
                long oldCountdown = countdown(player);
                long newCountdown = oldCountdown - deltaMilliseconds;

                countdowns.put(playerUUID, newCountdown);

                boolean afkZone = worldGuard && WorldGuardHook.afkZone(player);

                if (afkZone) {
                    countdowns.put(playerUUID, -1L);
                }

                if (kickThreshold >= 0 && newCountdown <= Math.negateExact(kickThreshold)) {
                    if (player.hasPermission("afkprotection.bypass.kick") || afkZone) {
                        countdowns.put(playerUUID, -1L);
                    } else {
                        this.kick(player);
                    }
                } else if (newCountdown <= 0) {
                    if (player.hasPermission("afkprotection.bypass.afk")) {
                        countdowns.put(playerUUID, countdownStart);
                    } else {
                        this.setAfkStatus(player, true);
                    }
                } else {
                    this.setAfkStatus(player, false);
                }
            }));
            lastUpdateInMilliseconds = nowMillisecondsTimestamp;
        }, 20, 20);

        // Command
        PluginCommand afkCommand = getCommand("afk");
        if (afkCommand != null) {
            afkCommand.setExecutor((commandSender, command, s, strings) -> {
                if (!(commandSender instanceof Player player))
                    return false;
                if (worldGuard && !WorldGuardHook.afkAllowed(player))
                    return true;
                if (config.getLong("afk.kickTimer") != 0) {
                    setAfkStatus(player, true);
                } else {
                    kick(player);
                }
                return true;
            });
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getOnlinePlayers().forEach(this::cleanup);
    }

    public void cleanup(Player player) {
        afkPlayerList.remove(player.getUniqueId());
        countdowns.remove(player.getUniqueId());
        removeLuckPermsTags(player);
    }

    public void addLuckPermsTags(Player player) {
        if (luckPermsAPI != null)
            luckPermsAPI.addTags(player);
    }

    public void removeLuckPermsTags(Player player) {
        if (luckPermsAPI != null)
            luckPermsAPI.removeTags(player);
    }

    public void broadcastMessageIfNotBlank(String message) {
        if (!message.isBlank()) {
            getServer().broadcast(
                    Component
                            .text(message)
                            .color(NamedTextColor.YELLOW)
            );
        }
    }

    public void broadcastAFKStatus(Player player) {
        if (isAFK(player)) {
            broadcastMessageIfNotBlank(config.getString("messages.+afk", "")
                    .replace("%player%", player.getName()));
        } else {
            broadcastMessageIfNotBlank(config.getString("messages.-afk", "")
                    .replace("%player%", player.getName()));
        }
    }

    public void kick(Player player) {
        String kickMessage = config.getString("messages.kickMessage", "");
        afkPlayerList.remove(player.getUniqueId());
        resetCountdown(player);
        if (!kickMessage.isBlank()) {
            player.kick(
                    Component
                            .text(kickMessage)
                            .color(NamedTextColor.YELLOW)
            );
        } else {
            player.kick();
        }
    }

    public boolean isAFK(Player player) {
        return afkPlayerList.contains(player.getUniqueId());
    }

    public void setAfkStatus(Player player, boolean status) {
        if (status == isAFK(player)) {
            return;
        }
        if (status) {
            if (config.getLong("afk.kickTimer") != 0) {
                if (countdowns.getOrDefault(player.getUniqueId(), 1L) > 0L)
                    countdowns.put(player.getUniqueId(), -1L);
                afkPlayerList.add(player.getUniqueId());
                addLuckPermsTags(player);
                broadcastAFKStatus(player);
                player.setSleepingIgnored(true);
            } else {
                kick(player);
            }
        } else {
            afkPlayerList.remove(player.getUniqueId());
            removeLuckPermsTags(player);
            broadcastAFKStatus(player);
            player.setSleepingIgnored(false);
        }
    }
}


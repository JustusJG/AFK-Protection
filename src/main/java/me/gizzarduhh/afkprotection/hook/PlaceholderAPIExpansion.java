package me.Gizzarduhh.afkProtection.hook;

import me.Gizzarduhh.afkProtection.AFKProtection;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    private AFKProtection plugin;

    public PlaceholderAPIExpansion(AFKProtection plugin) {
        this.plugin = plugin;
    }

    @Nullable
    private Player getPlayer(OfflinePlayer offlinePlayer, String playerName) {
        Player player = null;
        if (offlinePlayer.isOnline()) {
            player = offlinePlayer.getPlayer();
        }
        if (!playerName.isEmpty()) {
            player = plugin.getServer().getPlayer(playerName);
        }
        return player;
    }

    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        List<String> paramsList = new ArrayList<>(List.of(params.split("_")));
        String operation = paramsList.removeFirst();

        if (operation.equals("prefix") || operation.equals("suffix")) {
            return switch (params) {
                case "prefix" -> plugin.getConfig().getString("prefix.value", "");
                case "suffix" -> plugin.getConfig().getString("suffix.value", "");
                default -> null;
            };
        }

        if (operation.equals("isAFK")
                || operation.equals("countdown")
                || operation.equals("countdownSeconds")) {
            String playerName = "";
            if (!paramsList.isEmpty()) {
                playerName = paramsList.getFirst();
            }
            Player player = getPlayer(offlinePlayer, playerName);
            if (player == null) {
                return null;
            }

            if (operation.equals("isAFK")) {
                return this.plugin.isAFK(player) ?
                        plugin.getConfig().getString("placeholder.trueValue", "true"):
                        plugin.getConfig().getString("placeholder.falseValue", "false");
            }
            if (operation.equals("countdown")) {
                return Long.toString(plugin.countdown(player));
            }
            // divide time by 1000, because timer shall return seconds and not milliseconds
            return Integer.toString((int) (plugin.countdown(player) / 1000));
        }

        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "afkprotection";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
}

package me.Gizzarduhh.afkProtection.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.Gizzarduhh.afkProtection.AFKProtection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final AFKProtection plugin;

    public PlayerListener(AFKProtection plugin)
    {this.plugin = plugin;}

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && plugin.isAFK(player))
            event.setCancelled(true);
    }

    @EventHandler
    void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && plugin.isAFK(player))
            event.setCancelled(true);
    }

    @EventHandler
    void onPlayerChat(AsyncChatEvent event){
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event){
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    void onPlayerInput(PlayerInputEvent event){
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event){
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            plugin.resetCountdown(player);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        plugin.resetCountdown(event.getPlayer());
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event){
        plugin.cleanup(event.getPlayer());
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event){
        plugin.cleanup(event.getPlayer());
    }

    @EventHandler
    void onPlayerKick(PlayerKickEvent event){
        Player player = event.getPlayer();
        String kickBroadcast = plugin.getConfig().getString("messages.kickBroadcast", "");
        if (!kickBroadcast.isBlank()) {
            kickBroadcast = kickBroadcast.replace("%player%", player.getName());
            event.leaveMessage(
                    Component
                            .text(kickBroadcast)
                            .color(NamedTextColor.YELLOW)
            );
        }
        plugin.cleanup(player);
    }
}

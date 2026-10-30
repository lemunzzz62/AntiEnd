package org.bl4nk.antiEnd.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PortalFillListener implements Listener {
    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getMaterial().equals(Material.ENDER_EYE)
                && event.getClickedBlock().getType().equals(Material.END_PORTAL_FRAME)) {

            event.getPlayer().sendMessage(Component.text("The end is").color(TextColor.color(0xdf3fdf))
                    .append(Component.text(" OFF-LIMITS!").color(TextColor.color(0xdf3fdf))
                            .decorate(TextDecoration.BOLD)));

            event.setCancelled(true);
        }
    }
}

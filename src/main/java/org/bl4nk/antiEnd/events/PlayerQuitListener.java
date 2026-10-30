package org.bl4nk.antiEnd.events;

import fr.mrmicky.fastboard.adventure.FastBoard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bl4nk.antiEnd.AntiEnd.boards;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        FastBoard board = boards.remove(event.getPlayer());
        if (board != null) board.delete();
    }
}

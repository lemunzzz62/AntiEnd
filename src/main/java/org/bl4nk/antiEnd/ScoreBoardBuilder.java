package org.bl4nk.antiEnd;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;


import static org.bl4nk.antiEnd.AntiEnd.*;

public class ScoreBoardBuilder {
    static final String SKULL = "☠ ";
    static final String DIAMOND = "♦ ";
    static final String HOURGLASS = "⌛ ";
    static final String PLUG = "\uD83D\uDD0C ";

    static final String[] ARROWS = {"↑ ", "→ ", "↓ ", "← "};

    public static void updateTabFooter(Player player) {
        Runtime runtime = Runtime.getRuntime();

        long maxMb = runtime.maxMemory() / 1024 / 1024;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

        TextColor color = TextColor.color(0x3fdf3f);

        if (usedMb >= maxMb * 0.85) {
            color = TextColor.color(0xdf3f3f);
        } else if (usedMb >= maxMb * 0.65) {
            color = TextColor.color(0xdfdf3f);
        }

        player.sendPlayerListFooter((Component.text("Memory: ").color(NamedTextColor.GRAY)
                .append(Component.text(usedMb + "MB").color(color))
                .append(Component.text("/").color(NamedTextColor.GRAY))
                .append(Component.text(maxMb + "MB").color(TextColor.color(0x3fdf3f)))));
    }

    public static void updateScoreboard(Player player, FastBoard board) {
        int deaths = player.getStatistic(Statistic.DEATHS);
        AntiEnd.Waypoint waypoint = waypoints.get(player.getUniqueId());

        String symbol = DIAMOND;
        String name = "None";
        String loc = "";
        TextColor color = TextColor.color(0x3fdf3f);


        if (waypoint != null) {
            name = waypoint.name() + ": ";
            loc = waypoint.x() + " " + waypoint.y() + " " + waypoint.z();
            color = switch (waypoint.dimension()) {
                case NETHER -> TextColor.color(0xdf3f3f);
                case THE_END -> TextColor.color(0xdf3fdf);
                default -> TextColor.color(0x3fdf3f);
            };

            if (player.getWorld().getEnvironment().equals(waypoint.dimension())) {
                double dx = waypoint.x() - player.getX();
                double dz = waypoint.z() - player.getZ();

                double len = Math.sqrt(dx * dx + dz * dz);

                dx /= len;
                dz /= len;

                double yaw = Math.toDegrees(Math.atan2(-dx, dz));
                yaw -= player.getYaw();

                yaw = ((yaw % 360) + 540) % 360 - 180;

                int sector = Math.floorMod(Math.round(yaw / 90.0), 4);
                symbol = ARROWS[sector];

                if (player.getLocation().distance(new Location(player.getWorld(), waypoint.x(), waypoint.y(), waypoint.z())) <= 10) {
                    symbol = DIAMOND;
                }
            }
        }

        String playtime = expandDuration(player.getStatistic(Statistic.TOTAL_WORLD_TIME));

        board.updateLines(Component.text(SKULL).color(TextColor.color(0xdf3f3f))
                        .append(Component.text("Deaths: ").color(NamedTextColor.WHITE)
                                .append(Component.text(deaths).color(TextColor.color(0xdf3f3f)))),

                Component.empty(),

                Component.text(symbol).color(TextColor.color(0x3fdf3f))
                        .append(Component.text(name).color(NamedTextColor.WHITE))
                        .append(Component.text(loc).color(color)),

                Component.empty(),

                Component.text(HOURGLASS).color(TextColor.color(0x3fdfdf))
                        .append(Component.text("Playtime: ").color(NamedTextColor.WHITE)
                                .append(Component.text(playtime).color(TextColor.color(0x3fdfdf)))),

                Component.empty(),

                Component.text(PLUG).color(TextColor.color(0xdfdf3f))
                        .append(Component.text("IP: ").color(NamedTextColor.WHITE))
                        .append(Component.text(IpNotShownTo.contains(player.getUniqueId()) ? "**.***.**.***" : "99.244.22.138")).color(TextColor.color(0xdfdf3f)),

                Component.text(Bukkit.getCurrentTick() % 300 <= 50  && !IpNotShownTo.contains(player.getUniqueId())? "/toggleip: hides IP" : "").color(TextColor.color(0xdfdfdf)));
    }
}

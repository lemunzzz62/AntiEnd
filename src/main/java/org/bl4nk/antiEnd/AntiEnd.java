package org.bl4nk.antiEnd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bl4nk.antiEnd.events.PlayerJoinListener;
import org.bl4nk.antiEnd.events.PlayerQuitListener;
import org.bl4nk.antiEnd.events.PortalFillListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public final class AntiEnd extends JavaPlugin {
    final Logger LOGGER = getLogger();
    final PluginManager PM = getServer().getPluginManager();

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Type WAYPOINTS_TYPE = new TypeToken<Map<UUID, Waypoint>>() {}.getType();

    public static Map<UUID, Waypoint> waypoints = new HashMap<>();
    public static Set<UUID> IpNotShownTo = new HashSet<>();
    public static Set<UUID> scoreboardNotShownTo = new HashSet<>();
    
    public static Map<Player, FastBoard> boards = new HashMap<>();

    private Path waypointsFile() {
        return getDataPath().resolve("waypoints.json");
    }

    private Path IpNotShownToFile() {
        return getDataPath().resolve("hidden-ips.txt");
    }

    private Path scoreboardNotShownToFile() {
        return getDataPath().resolve("hidden-scoreboards.txt");
    }

    private void saveServerData() {
        try {
            Files.createDirectories(getDataPath());
            Files.writeString(waypointsFile(), GSON.toJson(waypoints));
        } catch (IOException e) {
            LOGGER.severe("Couldn't save waypoints: " + e.getMessage());
        }

        try {
            Files.createDirectories(getDataPath());
            Files.write(IpNotShownToFile(), IpNotShownTo.stream().map(UUID::toString).toList());
        } catch (IOException e) {
            LOGGER.severe("Couldn't save IP invisibilities: " + e.getMessage());
        }

        try {
            Files.createDirectories(getDataPath());
            Files.write(scoreboardNotShownToFile(), scoreboardNotShownTo.stream().map(UUID::toString).toList());
        } catch (IOException e) {
            LOGGER.severe("Couldn't save scoreboard invisibilities: " + e.getMessage());
        }
    }

    private void loadServerData() {
        if (Files.exists(waypointsFile())) {
            try {
                Map<UUID, Waypoint> load = GSON.fromJson(Files.readString(waypointsFile()), WAYPOINTS_TYPE);
                if (load != null) {
                    waypoints.putAll(load);
                }
            } catch (IOException | JsonParseException e) {
                LOGGER.severe("Couldn't load waypoints: " + e.getMessage());
            }
        }

        if (Files.exists(IpNotShownToFile())) {
            try {
                for (String line : Files.readAllLines(IpNotShownToFile())) {
                    if (!line.isBlank()) {
                        IpNotShownTo.add(UUID.fromString(line.trim()));
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.severe("Couldn't load IP visibilities: " + e.getMessage());
            }
        }

        if (Files.exists(scoreboardNotShownToFile())) {
            try {
                for (String line : Files.readAllLines(scoreboardNotShownToFile())) {
                    if (!line.isBlank()) {
                        scoreboardNotShownTo.add(UUID.fromString(line.trim()));
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.severe("Couldn't load scoreboard visibilities: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void onEnable() {
        LOGGER.info("AntiEnd: ENABLED");
        loadServerData();

        // PM.registerEvents(new PortalFillListener(), this);
        PM.registerEvents(new PlayerJoinListener(), this);
        PM.registerEvents(new PlayerQuitListener(), this);

         Bukkit.getScheduler().runTaskTimer(this, () -> {
             for (FastBoard board : boards.values()) {
                 Player player = board.getPlayer();

                 ScoreBoardBuilder.updateScoreboard(player, board);
                 ScoreBoardBuilder.updateTabFooter(player);
             }
         }, 0L, 5L);

        this.registerCommand(
                "setwaypoint",
                (source, args) -> {
                    if (source.getSender() instanceof Player player) {
                        if (args.length > 0) {
                            String name = String.join(" ", args);

                            if (name.length() > 16) {
                                name = name.substring(0, 16);
                            }

                            Location loc = player.getLocation().toBlockLocation();
                            waypoints.put(player.getUniqueId(), new Waypoint(name, player.getWorld().getEnvironment(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                            player.sendMessage(Component.text("Saved waypoint '" + name + "'!").color(TextColor.color(0x3fdf3f)));
                        } else {
                            player.sendMessage(Component.text("You need to include a name for the waypoint!").color(TextColor.color(0xdf3f3f)));
                        }
                        saveServerData();
                    }
                }
        );

        this.registerCommand(
                "clearwaypoint",
                (source, args) -> {
                    if (source.getSender() instanceof Player player) {
                        waypoints.remove(player.getUniqueId());
                        player.sendMessage(Component.text("Removed your waypoint from the scoreboard!").color(TextColor.color(0xdfdf3f)));
                        saveServerData();
                    }
                }
        );

        this.registerCommand(
                "toggleip",
                (source, args) -> {
                    if (source.getSender() instanceof Player player) {
                        if (IpNotShownTo.contains(player.getUniqueId())) {
                            IpNotShownTo.remove(player.getUniqueId());
                            player.sendMessage(Component.text("Now showing the server IP in scoreboard!").color(TextColor.color(0xdf3f3f)));
                        } else {
                            IpNotShownTo.add(player.getUniqueId());
                            player.sendMessage(Component.text("No longer showing the server IP in scoreboard!").color(TextColor.color(0x3fdf3f)));
                        }
                        saveServerData();
                    }
                }
        );

        this.registerCommand(
                "togglescoreboard",
                (source, args) -> {
                    if (source.getSender() instanceof Player player) {
                        if (boards.containsKey(player)) {
                            boards.remove(player).delete();
                            player.sendMessage(Component.text("No longer showing the scoreboard!").color(TextColor.color(0xdf3f3f)));
                            scoreboardNotShownTo.add(player.getUniqueId());
                        } else {
                            boards.put(player, new FastBoard(player));
                            player.sendMessage(Component.text("Now showing the scoreboard!").color(TextColor.color(0x3fdf3f)));
                            scoreboardNotShownTo.remove(player.getUniqueId());
                        }
                        saveServerData();
                    }
                }
        );
    }

    @Override
    public void onDisable() {
        saveServerData();

        LOGGER.info("AntiEnd: DISABLED");
    }

    public record Waypoint(String name, World.Environment dimension, int x, int y, int z) {}

    static final long TICKS_PER_DAY = 20L * 60L * 60L * 24L; // 1 728 000
    static final long TICKS_PER_HOUR = 20L * 60L * 60L; // 72 000
    static final long TICKS_PER_MINUTE = 20L * 60L; // 1 200

    public static String expandDuration(int ticks) {
        String str = "";

        int days = Math.toIntExact(Math.floorDiv(ticks, TICKS_PER_DAY));
        int hours = Math.toIntExact(Math.floorDiv(ticks, TICKS_PER_HOUR)) % 24;
        int mins = Math.toIntExact(Math.floorDiv(ticks, TICKS_PER_MINUTE)) % 60;

        if (days > 0) str = str.concat(days + "d ");
        if (hours > 0) str = str.concat(hours + "h ");
        if (mins > 0) str = str.concat(mins + "m");

        return str.isEmpty() ? "0m" : str;
    }
}

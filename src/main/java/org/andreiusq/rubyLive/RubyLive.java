package org.andreiusq.rubyLive;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.util.*;

public final class RubyLive extends JavaPlugin implements TabExecutor {

    private Player liveController; // Player currently in control of live mode
    private final Map<UUID, Long> liveCooldowns = new HashMap<>(); // Map to store cooldowns for /live command
    private static final long COOLDOWN_TIME = 15 * 60 * 1000; // 15 minutes in milliseconds
    private BukkitTask teleportTask; // Task for periodically teleporting the spectator
    private FileConfiguration config; // Configuration file

    @Override
    public void onEnable() {
        // Save default config if not present
        saveDefaultConfig();
        config = getConfig();

        // Register commands
        this.getCommand("startlive").setExecutor(this);
        this.getCommand("live").setExecutor(this);
        this.getCommand("stoplive").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Stop any ongoing teleport task when the plugin is disabled
        if (teleportTask != null && !teleportTask.isCancelled()) {
            teleportTask.cancel();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "startlive":
                return handleStartLive(player);
            case "live":
                return handleLive(player);
            case "stoplive":
                return handleStopLive(player);
            default:
                return false;
        }
    }

    private boolean handleStartLive(Player player) {
        // Check for permission to use /startlive
        if (!player.hasPermission("rubylive.startlive")) {
            player.sendMessage(getConfigMessage("startlive_no_permission"));
            return true;
        }

        if (liveController != null) {
            player.sendMessage(getConfigMessage("startlive_already_active"));
            return true;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(player); // Remove the command executor from the list

        if (players.isEmpty()) {
            player.sendMessage(getConfigMessage("startlive_no_players"));
            return true;
        }

        // Select a random player
        Player randomPlayer = players.get(new Random().nextInt(players.size()));

        // Set the player who executed the command as the live controller
        liveController = player;
        player.setGameMode(GameMode.SPECTATOR);
        startFollowing(player, randomPlayer); // Start following the target player

        // Announce to the server
        Bukkit.broadcastMessage(formatMessage(getConfigMessage("startlive_broadcast"), player, randomPlayer));

        return true;
    }

    private boolean handleLive(Player player) {
        // Check if the player is on cooldown
        if (isOnCooldown(player)) {
            long timeLeft = (liveCooldowns.get(player.getUniqueId()) + COOLDOWN_TIME - System.currentTimeMillis()) / 1000;
            String cooldownMessage = getConfigMessage("live_on_cooldown")
                    .replace("{minutes}", String.valueOf(timeLeft / 60))
                    .replace("{seconds}", String.valueOf(timeLeft % 60));
            player.sendMessage(cooldownMessage);
            return true;
        }

        if (liveController == null || !liveController.isOnline()) {
            player.sendMessage(getConfigMessage("live_no_controller"));
            return true;
        }

        // Move the live controller to follow the player who used /live
        startFollowing(liveController, player);

        // Set cooldown
        liveCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Announce to the server
        Bukkit.broadcastMessage(formatMessage(getConfigMessage("live_broadcast"), player, liveController));

        return true;
    }

    private boolean handleStopLive(Player player) {
        if (liveController == null || !liveController.equals(player)) {
            player.sendMessage(getConfigMessage("stoplive_no_control"));
            return true;
        }

        // Stop the teleport task when live is stopped
        if (teleportTask != null && !teleportTask.isCancelled()) {
            teleportTask.cancel();
        }

        // Reset the live controller and set the player back to their previous game mode
        liveController.setGameMode(GameMode.SURVIVAL); // You can change this to the player's original mode if needed
        liveController = null;

        // Announce to the server
        Bukkit.broadcastMessage(formatMessage(getConfigMessage("stoplive_broadcast"), player, null));

        return true;
    }

    private void startFollowing(Player spectator, Player target) {
        // Stop any existing teleport task to prevent conflicts
        if (teleportTask != null && !teleportTask.isCancelled()) {
            teleportTask.cancel();
        }

        // Display an action bar message to the spectator indicating who they are following
        sendActionBar(spectator, formatMessage(getConfigMessage("action_bar_message"), spectator, target));

        // Start a repeating task to keep the spectator character near the target
        teleportTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Continuously teleport the spectator's character near the target player
            Location targetLocation = target.getLocation();
            Location spectatorLocation = targetLocation.clone().add(0, 5, 0); // Adjust location above the target
            spectator.teleport(spectatorLocation);

            // Update the action bar message every second to remind who is being followed
            sendActionBar(spectator, formatMessage(getConfigMessage("action_bar_message"), spectator, target));
        }, 0L, 20L); // Repeat every 20 ticks (1 second)
    }

    private boolean isOnCooldown(Player player) {
        // Check if the player has used /live recently
        return liveCooldowns.containsKey(player.getUniqueId()) &&
                (System.currentTimeMillis() - liveCooldowns.get(player.getUniqueId()) < COOLDOWN_TIME);
    }

    private void sendActionBar(Player player, String message) {
        try {
            // Reflection to access NMS classes and methods
            Class<?> chatComponentTextClass = Class.forName("net.minecraft.network.chat.Component");
            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");

            // Create chat component with the message
            Object chatComponent = chatComponentTextClass.getDeclaredMethod("literal", String.class).invoke(null, message);

            // Create the packet with the chat component
            Constructor<?> packetConstructor = packetPlayOutChatClass.getConstructor(chatComponentTextClass);
            Object packet = packetConstructor.newInstance(chatComponent);

            // Send packet to the player
            sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            // Access the player's connection and send the packet
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("connection").get(handle);
            connection.getClass().getMethod("send", packet.getClass()).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getConfigMessage(String path) {
        // Get the message from the config and translate color codes
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages." + path, ""));
    }

    private String formatMessage(String message, Player player, Player target) {
        // Replace placeholders with player and target names
        return message.replace("{player}", player.getName())
                .replace("{target}", target != null ? target.getName() : "")
                .replace("{controller}", liveController != null ? liveController.getName() : "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>(); // No tab completion needed for these commands
    }
}

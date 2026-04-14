package frqme.isa.dev.data;

import frqme.isa.dev.trial.Trial;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Trial plugin;
    private final File dataFolder;
    private final Map<UUID, Boolean> scoreboardEnabled = new HashMap<>();

    public PlayerDataManager(Trial plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public boolean isScoreboardEnabled(Player player) {
        UUID uuid = player.getUniqueId();

        if (!scoreboardEnabled.containsKey(uuid)) {
            loadPlayerData(player);
        }

        return scoreboardEnabled.getOrDefault(uuid, true); // Default: enabled
    }

    public void setScoreboardEnabled(Player player, boolean enabled) {
        scoreboardEnabled.put(player.getUniqueId(), enabled);
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");

        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            scoreboardEnabled.put(uuid, config.getBoolean("scoreboard-enabled", true));
        } else {
            scoreboardEnabled.put(uuid, true);
        }
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");

        FileConfiguration config = new YamlConfiguration();
        config.set("scoreboard-enabled", scoreboardEnabled.getOrDefault(uuid, true));

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player data for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : scoreboardEnabled.keySet()) {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");
            FileConfiguration config = new YamlConfiguration();
            config.set("scoreboard-enabled", scoreboardEnabled.get(uuid));

            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save player data for " + uuid + ": " + e.getMessage());
            }
        }
    }
}
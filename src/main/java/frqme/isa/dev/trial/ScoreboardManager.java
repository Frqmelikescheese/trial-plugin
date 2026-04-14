package frqme.isa.dev.trial;

import frqme.isa.dev.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final Trial plugin;
    private final PlayerDataManager playerDataManager;

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    private final Map<UUID, Map<String, String>> lastValues = new HashMap<>();
    private final Map<UUID, Long> lastCoordUpdate = new HashMap<>();

    private int currentTitleFrame = 0;
    private int titleTicks = 0;

    private final DecimalFormat kdFormat = new DecimalFormat("0.00");

    public ScoreboardManager(Trial plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    public void createScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("sidebar", "dummy", getCurrentTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerScoreboards.put(uuid, scoreboard);
        playerObjectives.put(uuid, objective);
        lastValues.put(uuid, new HashMap<>());
        lastCoordUpdate.put(uuid, 0L);

        if (playerDataManager.isScoreboardEnabled(player)) {
            player.setScoreboard(scoreboard);
        }
    }

    public void removeScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        playerScoreboards.remove(uuid);
        playerObjectives.remove(uuid);
        lastValues.remove(uuid);
        lastCoordUpdate.remove(uuid);

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void toggleScoreboard(Player player) {
        boolean enabled = playerDataManager.isScoreboardEnabled(player);
        playerDataManager.setScoreboardEnabled(player, !enabled);

        if (!enabled) {
            Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
            if (scoreboard != null) {
                player.setScoreboard(scoreboard);
            }
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void updateAll() {
        updateTitleAnimation();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (playerDataManager.isScoreboardEnabled(player)) {
                updateScoreboard(player);
            }
        }
    }

    private void updateTitleAnimation() {
        int frameInterval = plugin.getConfig().getInt("title-animation.frame-interval", 10);
        titleTicks++;

        if (titleTicks >= frameInterval) {
            titleTicks = 0;
            var frames = plugin.getConfig().getStringList("title-animation.frames");
            if (!frames.isEmpty()) {
                currentTitleFrame = (currentTitleFrame + 1) % frames.size();

                String newTitle = getCurrentTitle();
                for (Objective objective : playerObjectives.values()) {
                    objective.setDisplayName(newTitle);
                }
            }
        }
    }

    private String getCurrentTitle() {
        var frames = plugin.getConfig().getStringList("title-animation.frames");
        if (frames.isEmpty()) {
            return "§6§lSERVER";
        }
        return colorize(frames.get(currentTitleFrame % frames.size()));
    }

    private void updateScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        Objective objective = playerObjectives.get(uuid);
        Scoreboard scoreboard = playerScoreboards.get(uuid);

        if (objective == null || scoreboard == null) {
            return;
        }

        Map<String, String> lastPlayerValues = lastValues.get(uuid);
        int line = 15; // Start from top

        updateLine(player, scoreboard, objective, lastPlayerValues, "online", getOnlinePlayersLine(), line--);

        if (isLineEnabled("health")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "health", getHealthLine(player), line--);
        }

        if (isLineEnabled("food")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "food", getFoodLine(player), line--);
        }

        if (isLineEnabled("kills")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "kills", getKillsLine(player), line--);
        }

        if (isLineEnabled("deaths")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "deaths", getDeathsLine(player), line--);
        }

        if (isLineEnabled("kd")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "kd", getKDLine(player), line--);
        }

        if (isLineEnabled("world")) {
            updateLine(player, scoreboard, objective, lastPlayerValues, "world", getWorldLine(player), line--);
        }

        if (isLineEnabled("coordinates")) {
            updateCoordinatesLine(player, scoreboard, objective, lastPlayerValues, line--);
        }
    }

    private void updateLine(Player player, Scoreboard scoreboard, Objective objective,
                            Map<String, String> lastPlayerValues, String key, String value, int score) {
        String lastValue = lastPlayerValues.get(key);

        if (lastValue == null || !lastValue.equals(value)) {
            if (lastValue != null) {
                scoreboard.resetScores(lastValue);
            }

            objective.getScore(value).setScore(score);
            lastPlayerValues.put(key, value);
        }
    }

    private void updateCoordinatesLine(Player player, Scoreboard scoreboard, Objective objective,
                                       Map<String, String> lastPlayerValues, int score) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastUpdate = lastCoordUpdate.get(uuid);

        int coordRefreshInterval = plugin.getConfig().getInt("coordinate-refresh-interval", 5) * 1000;

        if (now - lastUpdate >= coordRefreshInterval) {
            lastCoordUpdate.put(uuid, now);
            String value = getCoordinatesLine(player);
            updateLine(player, scoreboard, objective, lastPlayerValues, "coordinates", value, score);
        }
    }

    private boolean isLineEnabled(String line) {
        return plugin.getConfig().getBoolean("lines." + line + ".enabled", true);
    }

    private String getTemplate(String line) {
        return colorize(plugin.getConfig().getString("lines." + line + ".template", ""));
    }

    private String getOnlinePlayersLine() {
        int online = Bukkit.getOnlinePlayers().size();
        return getTemplate("online").replace("{online}", String.valueOf(online));
    }

    private String getHealthLine(Player player) {
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();

        int filledHearts = (int) Math.ceil(health / 2.0);
        int totalHearts = (int) Math.ceil(maxHealth / 2.0);
        int emptyHearts = totalHearts - filledHearts;

        StringBuilder hearts = new StringBuilder();
        hearts.append("§c");
        for (int i = 0; i < filledHearts; i++) {
            hearts.append("❤");
        }
        hearts.append("§7");
        for (int i = 0; i < emptyHearts; i++) {
            hearts.append("❤");
        }

        return getTemplate("health").replace("{hearts}", hearts.toString());
    }

    private String getFoodLine(Player player) {
        int food = player.getFoodLevel();
        return getTemplate("food").replace("{food}", String.valueOf(food));
    }

    private String getKillsLine(Player player) {
        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        return getTemplate("kills").replace("{kills}", String.valueOf(kills));
    }

    private String getDeathsLine(Player player) {
        int deaths = player.getStatistic(Statistic.DEATHS);
        return getTemplate("deaths").replace("{deaths}", String.valueOf(deaths));
    }

    private String getKDLine(Player player) {
        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);

        String kdValue;
        if (deaths == 0) {
            kdValue = "N/A";
        } else {
            double kd = (double) kills / deaths;
            kdValue = kdFormat.format(kd);
        }

        return getTemplate("kd").replace("{kd}", kdValue);
    }

    private String getWorldLine(Player player) {
        String worldName = player.getWorld().getName();
        String displayName = plugin.getConfig().getString("world-aliases." + worldName, worldName);
        return getTemplate("world").replace("{world}", displayName);
    }

    private String getCoordinatesLine(Player player) {
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        return getTemplate("coordinates")
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));
    }

    public void reloadAllScoreboards() {
        currentTitleFrame = 0;
        titleTicks = 0;

        for (Map<String, String> values : lastValues.values()) {
            values.clear();
        }

        for (UUID uuid : lastCoordUpdate.keySet()) {
            lastCoordUpdate.put(uuid, 0L);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
            createScoreboard(player);
        }
    }

    private String colorize(String text) {
        return text.replace("&", "§");
    }
}
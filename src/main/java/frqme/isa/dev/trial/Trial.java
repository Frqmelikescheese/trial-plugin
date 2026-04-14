package frqme.isa.dev.trial;

import frqme.isa.dev.commands.ScoreboardReloadCommand;
import frqme.isa.dev.commands.ScoreboardToggleCommand;
import frqme.isa.dev.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Trial extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;
    private PlayerDataManager playerDataManager;
    private ScoreboardUpdateTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerDataManager = new PlayerDataManager(this);
        scoreboardManager = new ScoreboardManager(this, playerDataManager);

        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("scoreboard").setExecutor(new ScoreboardToggleCommand(scoreboardManager, playerDataManager));
        getCommand("sb").setExecutor(new ScoreboardReloadCommand(this, scoreboardManager));

        startUpdateTask();

        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboardManager.createScoreboard(player);
        }

        getLogger().info("Trial plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        playerDataManager.saveAll();

        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboardManager.removeScoreboard(player);
        }

        getLogger().info("Trial plugin disabled successfully!");
    }

    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        int updateInterval = getConfig().getInt("update-interval", 20);
        updateTask = new ScoreboardUpdateTask(this, scoreboardManager);
        updateTask.runTaskTimer(this, 0L, updateInterval);
    }

    public void reload() {
        reloadConfig();
        scoreboardManager.reloadAllScoreboards();
        startUpdateTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.createScoreboard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.removeScoreboard(player);
        playerDataManager.savePlayerData(player);
    }
}
package frqme.isa.dev.trial;

import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdateTask extends BukkitRunnable {

    private final Trial plugin;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardUpdateTask(Trial plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        scoreboardManager.updateAll();
    }
}
package frqme.isa.dev.commands;

import frqme.isa.dev.trial.ScoreboardManager;
import frqme.isa.dev.data.PlayerDataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreboardToggleCommand implements CommandExecutor {

    private final ScoreboardManager scoreboardManager;
    private final PlayerDataManager playerDataManager;

    public ScoreboardToggleCommand(ScoreboardManager scoreboardManager, PlayerDataManager playerDataManager) {
        this.scoreboardManager = scoreboardManager;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            scoreboardManager.toggleScoreboard(player);

            boolean enabled = playerDataManager.isScoreboardEnabled(player);
            if (enabled) {
                player.sendMessage("§aScoreboard enabled.");
            } else {
                player.sendMessage("§cScoreboard disabled.");
            }

            return true;
        }

        player.sendMessage("§cUsage: /scoreboard toggle");
        return true;
    }
}
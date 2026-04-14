package frqme.isa.dev.commands;

import frqme.isa.dev.trial.ScoreboardManager;
import frqme.isa.dev.trial.Trial;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ScoreboardReloadCommand implements CommandExecutor {

    private final Trial plugin;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardReloadCommand(Trial plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("sb.reload")) {
                sender.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            plugin.reload();
            sender.sendMessage("§aConfiguration reloaded and scoreboards rebuilt!");
            return true;
        }

        sender.sendMessage("§cUsage: /sb reload");
        return true;
    }
}
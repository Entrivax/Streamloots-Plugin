package fr.entrivax.streamloots;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class StreamlootsCommandExecutor implements CommandExecutor {
    StreamlootsClientManager _streamlootsManager;

    public StreamlootsCommandExecutor(StreamlootsClientManager streamlootsManager) {
        this._streamlootsManager = streamlootsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (args.length >= 1) {
            if (!sender.hasPermission("streamloots.reload")) {
                sender.sendMessage(Component.text(command.getPermissionMessage()).color(TextColor.color(0xff0000)));
                return true;
            }
            if (args[0].equals("reload")) {
                this._streamlootsManager.load();
                sender.sendMessage(Component.text("Config reloaded"));
                return true;
            }
        }

        return false;
    }
    
}

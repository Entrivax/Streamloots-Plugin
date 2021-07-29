package fr.entrivax.streamlootsbase.commands;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;

public class StreamlootsCardChatCommandCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _command;
    private boolean _cancelled = false;
    public StreamlootsCardChatCommandCommand(JavaPlugin plugin, String command) {
        this._plugin = plugin;
        this._command = command;
    }

    @Override
    public Runnable run(Runnable next) {
        Bukkit.getScheduler().runTask(_plugin, new Runnable(){
            @Override
            public void run() {
                if (_cancelled) {
                    return;
                }
                Server server = _plugin.getServer();
                server.dispatchCommand(server.getConsoleSender(), _command);
                next.run();
            }
        });
        return () -> {
            _cancelled = true;
        };
    }
}

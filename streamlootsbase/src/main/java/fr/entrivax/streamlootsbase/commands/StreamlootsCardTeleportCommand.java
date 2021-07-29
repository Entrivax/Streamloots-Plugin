package fr.entrivax.streamlootsbase.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamlootsbase.PlayersHelper;
import fr.entrivax.streamlootsbase.Position;
import fr.entrivax.streamlootscore.commands.IStreamlootsCardCommand;

public class StreamlootsCardTeleportCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private boolean _cancelled = false;
    private Position _position;

    public StreamlootsCardTeleportCommand(JavaPlugin plugin, String applyOn, Position position) {
        this._plugin = plugin;
        this._applyOn = applyOn;
        this._position = position;
    }

    @Override
    public Runnable run(Runnable next) {
        List<Player> players = PlayersHelper.getTargetedPlayers(_plugin.getServer(), _applyOn);
        Bukkit.getScheduler().runTask(_plugin, new Runnable(){
            @Override
            public void run() {
                if (_cancelled) {
                    return;
                }
                for (int i = 0; i < players.size(); i++) {
                    Player player = players.get(i);
                    Location location = PlayersHelper.getLocationFromPlayer(_position, player);
                    player.teleport(location);
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}

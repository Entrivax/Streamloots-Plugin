package fr.entrivax.streamlootsbase.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootsbase.PlayersHelper;
import fr.entrivax.streamlootsbase.Position;

public class StreamlootsCardPlaySoundCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private String _sound;
    private Position _position;
    private boolean _cancelled = false;
    public StreamlootsCardPlaySoundCommand(JavaPlugin plugin, String applyOn, String sound, Position position) {
        this._plugin = plugin;
        this._applyOn = applyOn;
        this._sound = sound;
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
                    Location soundLocation = PlayersHelper.getLocationFromPlayer(_position, player);
                    player.playSound(soundLocation, _sound, 1, 1);
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}
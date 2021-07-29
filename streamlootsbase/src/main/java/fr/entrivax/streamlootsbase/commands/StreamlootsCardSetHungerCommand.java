package fr.entrivax.streamlootsbase.commands;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamlootscore.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootsbase.PlayersHelper;

public class StreamlootsCardSetHungerCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private Integer _amount;
    private Logger _logger;
    private boolean _cancelled = false;
    public StreamlootsCardSetHungerCommand(JavaPlugin plugin, String applyOn, Integer amount, Logger logger) {
        this._plugin = plugin;
        this._applyOn = applyOn;
        this._logger = logger;
        this._amount = amount;
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
                    player.setFoodLevel(_amount);
                    _logger.log(Level.INFO, "Player " + player.getName() + " hunger set to " + _amount);
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}
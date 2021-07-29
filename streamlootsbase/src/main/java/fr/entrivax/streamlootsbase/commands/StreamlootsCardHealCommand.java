package fr.entrivax.streamlootsbase.commands;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamlootscore.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootsbase.PlayersHelper;

public class StreamlootsCardHealCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private Integer _amount;
    private Logger _logger;
    private boolean _cancelled = false;
    public StreamlootsCardHealCommand(JavaPlugin plugin, String applyOn, Integer amount, Logger logger) {
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
                    double currentHealth = player.getHealth();
                    double health = Math.max(0, Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), currentHealth + _amount));
                    player.setHealth(health);
                    _logger.log(Level.INFO, "Player " + player.getName() + " healed (" + Math.round(currentHealth) + " -> " + Math.round(health));
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}
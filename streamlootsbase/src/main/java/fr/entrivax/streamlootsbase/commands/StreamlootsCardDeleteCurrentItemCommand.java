package fr.entrivax.streamlootsbase.commands;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootsbase.PlayersHelper;

public class StreamlootsCardDeleteCurrentItemCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private Logger _logger;
    private boolean _cancelled = false;
    public StreamlootsCardDeleteCurrentItemCommand(JavaPlugin plugin, String applyOn, Logger logger) {
        this._plugin = plugin;
        this._applyOn = applyOn;
        this._logger = logger;
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
                    PlayerInventory pInv = player.getInventory();
                    ItemStack itemToDrop = pInv.getItemInMainHand();
                    if (itemToDrop.getType() != Material.AIR) {
                        pInv.setItemInMainHand(new ItemStack(Material.AIR));
                        _logger.log(Level.INFO, "Delete items in hand of " + player.getName());
                    }
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}

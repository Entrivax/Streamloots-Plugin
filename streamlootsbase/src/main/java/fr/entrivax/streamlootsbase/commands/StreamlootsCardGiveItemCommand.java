package fr.entrivax.streamlootsbase.commands;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootsbase.PlayersHelper;

public class StreamlootsCardGiveItemCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private String _applyOn;
    private String _item;
    private Integer _amount;
    private Logger _logger;
    private boolean _cancelled = false;
    public StreamlootsCardGiveItemCommand(JavaPlugin plugin, String applyOn, String item, Integer amount, Logger logger) {
        this._plugin = plugin;
        this._applyOn = applyOn;
        this._logger = logger;
        this._amount = amount;
        this._item = item;
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
                    Material material = Material.getMaterial(_item);
                    if (material == null || !material.isItem()) {
                        _logger.log(Level.WARNING, "Invalid material " + _item);
                        break;
                    }
                    ItemStack is = new ItemStack(material, _amount == null || _amount <= 0 ? 1 : _amount);
                    HashMap<Integer, ItemStack> overflowItems = player.getInventory().addItem(is);
                    overflowItems.forEach((index, itemStack) -> {
                        player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                    });
                }
                next.run();
            }
        });

        return () -> {
            _cancelled = true;
        };
    }
}
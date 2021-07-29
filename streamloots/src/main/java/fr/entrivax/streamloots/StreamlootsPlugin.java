package fr.entrivax.streamloots;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardRegistry;
import fr.entrivax.streamloots.commands.StreamlootsCardRegistry;

public class StreamlootsPlugin extends JavaPlugin {
    private Logger logger;
    private StreamlootsClientManager _streamlootsManager;
    private StreamlootsCardRegistry _cardRegistry;

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
        this.saveDefaultConfig();

        FileConfiguration config = this.getConfig();
        logger = getLogger();
        _cardRegistry = new StreamlootsCardRegistry();
        _streamlootsManager = new StreamlootsClientManager(this, config, _cardRegistry, logger);
        _streamlootsManager.load();

        this.getCommand("streamloots").setExecutor(new StreamlootsCommandExecutor(_streamlootsManager));
    }

    @Override
    public void onDisable() {
        if (_streamlootsManager != null) {
            _streamlootsManager.unload();
            _streamlootsManager = null;
        }
        if (_cardRegistry != null) {
            _cardRegistry.destroy();
            _cardRegistry = null;
        }
    }


    public IStreamlootsCardRegistry getCardRegistry() {
        return this._cardRegistry;
    }
}

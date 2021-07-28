package fr.entrivax.streamloots;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StreamlootsPlugin extends JavaPlugin {
    protected static Logger logger;
    private StreamlootsClientManager _streamlootsManager;

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
        this.saveDefaultConfig();

        FileConfiguration config = this.getConfig();
        logger = getLogger();
        _streamlootsManager = new StreamlootsClientManager(this, config, logger);
        _streamlootsManager.load();

        this.getCommand("streamloots").setExecutor(new StreamlootsCommandExecutor(_streamlootsManager));
        logger.log(Level.INFO, "Plugin enabled");
    }

    @Override
    public void onDisable() {
        if (_streamlootsManager != null) {
            _streamlootsManager.unload();
            _streamlootsManager = null;
        }
        logger.log(Level.INFO, "Plugin disabled");
    }
}

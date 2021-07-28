package fr.entrivax.streamloots;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StreamlootsClientManager {
    JavaPlugin _plugin;
    Logger _logger;
    StreamlootsClient _streamloots;
    FileConfiguration _config;
    public StreamlootsClientManager(JavaPlugin plugin, FileConfiguration config, Logger logger) {
        this._plugin = plugin;
        this._logger = logger;
        this._config = config;
    }
    
    public void load() {
        unload();
        List<CardConfig> cardConfigs = new ArrayList<CardConfig>();
        File cardsFile = new File(this._plugin.getDataFolder(), "cards.json");
        if (cardsFile.exists()) {
            try(FileReader fileReader = new FileReader(cardsFile)) {
                cardConfigs = new Gson().fromJson(fileReader, new TypeToken<List<CardConfig>>() {}.getType());
            } catch (Exception ex) {
                this._logger.log(Level.WARNING, ex.toString());
            }
        }
        _streamloots = new StreamlootsClient(
            "https://widgets.streamloots.com/alerts/%s/media-stream".formatted(this._config.getString("alerts-id")),
            new StreamlootsRedemptionHandler(this._plugin, cardConfigs, this._logger),
            this._logger
        );
        _streamloots.connect();
    }

    public void unload() {
        if (_streamloots != null) {
            _streamloots.disconnect();
            _streamloots = null;
        }
    }
}

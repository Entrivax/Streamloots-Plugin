package fr.entrivax.streamloots;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardRegistry;

public class StreamlootsClientManager {
    JavaPlugin _plugin;
    Logger _logger;
    StreamlootsClient _streamloots;
    FileConfiguration _config;
    IStreamlootsCardRegistry _cardRegistry;
    public StreamlootsClientManager(JavaPlugin plugin, FileConfiguration config, IStreamlootsCardRegistry cardRegistry, Logger logger) {
        this._plugin = plugin;
        this._logger = logger;
        this._config = config;
        this._cardRegistry = cardRegistry;
    }
    
    public void load() {
        unload();
        List<CardConfig> cardConfigs = new ArrayList<CardConfig>();
        File cardsFile = new File(this._plugin.getDataFolder(), "cards.json");
        if (cardsFile.exists()) {
            try (FileReader fileReader = new FileReader(cardsFile)) {
                List<JsonObject> configs = new Gson().fromJson(fileReader, new TypeToken<List<JsonObject>>() {}.getType());
                for (int i = 0; i < configs.size(); i++) {
                    JsonObject config = configs.get(i);
                    ArrayList<CardCommand> commands = new ArrayList<CardCommand>();
                    config.getAsJsonArray("commands").forEach((command) -> {
                        JsonObject commandObject = command.getAsJsonObject();
                        commands.add(new CardCommand(commandObject.get("type").getAsString(), commandObject));
                    });
                    cardConfigs.add(new CardConfig(config.get("id").getAsString(), commands));
                }
            } catch (Exception ex) {
                this._logger.log(Level.WARNING, ex.toString());
            }
        }
        _logger.log(Level.INFO, "Loaded " + cardConfigs.size() + " cards");
        _streamloots = new StreamlootsClient(
            "https://widgets.streamloots.com/alerts/%s/media-stream".formatted(this._config.getString("alerts-id")),
            new StreamlootsRedemptionHandler(this._plugin, cardConfigs, _cardRegistry, this._logger),
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

package fr.entrivax.streamlootsbase;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.entrivax.streamloots.StreamlootsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StreamlootsBasePlugin extends JavaPlugin {
    Logger logger;
    @Override
    public void onEnable() {
        logger = getLogger();
        StreamlootsPlugin streamlootsPlugin = (StreamlootsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Streamloots");
        logger.log(Level.INFO, "Plugin enabled and found " + streamlootsPlugin.getName());
    }
}

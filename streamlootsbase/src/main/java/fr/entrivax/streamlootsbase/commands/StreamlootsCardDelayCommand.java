package fr.entrivax.streamlootsbase.commands;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;

public class StreamlootsCardDelayCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private long _ticks;
    private int _taskId;
    public StreamlootsCardDelayCommand(JavaPlugin plugin, long ticks) {
        _plugin = plugin;
        _ticks = ticks;
        _taskId = -1;
    }

    @Override
    public Runnable run(Runnable next) {
        BukkitScheduler scheduler = _plugin.getServer().getScheduler();
        _taskId = scheduler.scheduleSyncDelayedTask(_plugin, () -> {
            _taskId = -1;
            next.run();
        }, _ticks);
        return () -> {
            if (_taskId != -1) {
                scheduler.cancelTask(_taskId);
            }
        };
    }
}

package fr.entrivax.streamlootsbase.commands;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import fr.entrivax.streamlootsbase.PlayersHelper;
import fr.entrivax.streamlootscore.commands.IStreamlootsCardCommand;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;

public class StreamlootsCardDelayCommand implements IStreamlootsCardCommand {
    private JavaPlugin _plugin;
    private long _ticks;
    private BukkitTask _task;
    private BukkitTask _timerTask;
    private String _delayName;
    private String _displayTo;

    private long _ticksRemaining;
    public StreamlootsCardDelayCommand(JavaPlugin plugin, long ticks, String delayName, String displayTo) {
        _plugin = plugin;
        _ticks = ticks;
        _delayName = delayName;
        _displayTo = displayTo;
    }

    @Override
    public Runnable run(Runnable next) {
        List<Player> players = PlayersHelper.getTargetedPlayers(_plugin.getServer(), _displayTo);
        BukkitScheduler scheduler = _plugin.getServer().getScheduler();
        final BossBar bar = players.size() > 0 ? BossBar.bossBar(Component.text(_delayName != null ? _delayName : "Time remaining"), 1, Color.WHITE, Overlay.PROGRESS) : null;
        final ForwardingAudience audience = players.size() > 0 ? Audience.audience(players) : null;
        final long refreshTicks = 20;

        _task = scheduler.runTaskLater(_plugin, () -> {
            _task = null;
            if (_timerTask != null) {
                _timerTask.cancel();
                _timerTask = null;
            }
            if (audience != null && bar != null) {
                audience.hideBossBar(bar);
            }
            next.run();
        }, _ticks);

        if (players.size() > 0) {
            _ticksRemaining = _ticks;
            // scheduler.runTask(_plugin, () -> {
                if (audience != null && bar != null) {
                    audience.showBossBar(bar);
                }
            // });
            _timerTask = scheduler.runTaskTimer(_plugin, new Runnable() {
                @Override
                public void run() {
                    _ticksRemaining -= refreshTicks;
                    bar.progress((float)((double)_ticksRemaining / (double)_ticks));
                }
            }, refreshTicks, refreshTicks);
        }

        return () -> {
            if (_task != null) {
                _task.cancel();
            }
            if (_timerTask != null) {
                _timerTask.cancel();
                _timerTask = null;
            }
            if (audience != null && bar != null) {
                audience.hideBossBar(bar);
            }
        };
    }
}

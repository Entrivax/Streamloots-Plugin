package fr.entrivax.streamloots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;
import fr.entrivax.streamloots.commands.IStreamlootsCardRegistry;
import fr.entrivax.streamloots.commands.IStreamlootsCommandBuilder;
import fr.entrivax.streamloots.commands.StreamlootsCardCommandsProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class StreamlootsRedemptionHandler implements IStreamlootsRedemptionHandler {
    private List<CardConfig> _cardConfigs;
    private Server _server;
    private Logger _logger;
    private ArrayList<StreamlootsCardCommandsProcessor> _runningProcessors;
    private IStreamlootsCardRegistry _cardRegistry;
    public StreamlootsRedemptionHandler(JavaPlugin plugin, List<CardConfig> cardConfigs, IStreamlootsCardRegistry cardRegistry, Logger logger) {
        _cardConfigs = cardConfigs;
        _logger = logger;
        _cardRegistry = cardRegistry;
        _runningProcessors = new ArrayList<StreamlootsCardCommandsProcessor>();
        _server = plugin.getServer();
    }
    @Override
    public void handle(StreamlootsRedemption cardInfo) {
        broadcastCardPlayed(cardInfo);
    }

    private void broadcastCardPlayed(StreamlootsRedemption cardInfo) {
        _server.broadcast(Component.text("")
            .append(Component.text("[")
                .append(Component.text(cardInfo.data.cardName)
                    .color(TextColor.color(getColorFromRarity(cardInfo.data.cardRarity))))
                .append(Component.text("]"))
                .hoverEvent(Component.text(cardInfo.data.description).asHoverEvent()))
            .append(Component.text(" " + cardInfo.message)));
        for (int i = 0; i < _cardConfigs.size(); i++) {
            CardConfig cardConfig = _cardConfigs.get(i);
            if (cardConfig.id.equals(cardInfo.data.cardId)) {
                processCardCommands(cardConfig);
                break;
            }
        }
    }

    private void processCardCommands(CardConfig cardConfig) {
        ArrayList<IStreamlootsCardCommand> commands = new ArrayList<IStreamlootsCardCommand>();
        for (int i = 0; i < cardConfig.commands.size(); i++) {
            IStreamlootsCardCommand command = getCommand(cardConfig.commands.get(i));
            if (command != null) {
                commands.add(command);
            }
        }
        StreamlootsCardCommandsProcessor processor = new StreamlootsCardCommandsProcessor(commands.toArray(new IStreamlootsCardCommand[0]));
        _runningProcessors.add(processor);
        processor.run(() -> {
            _runningProcessors.remove(processor);
        });
    }
    private IStreamlootsCardCommand getCommand(CardCommand cardCommand) {
        IStreamlootsCommandBuilder builder = _cardRegistry.getBuilder(cardCommand.type);
        if (builder == null) {
            _logger.log(Level.WARNING, "No command builder found for the card type \"" + cardCommand.type + "\"");
        } else {
            return builder.build(cardCommand);
        }
        return null;
    }

    public void cancelRunningTasks() {
        for (int i = 0; i < _runningProcessors.size(); i++) {
            _runningProcessors.get(i).cancel();
        }
        _runningProcessors.clear();
    }

    private int getColorFromRarity(String rarity) {
        switch (rarity) {
            case "COMMON":
                return 0x7bc944;
            case "RARE":
                return 0x40a8f4;
            case "EPIC":
                return 0xd42ae6;
            case "LEGENDARY":
                return 0xffa727;
            default:
                return 0xffffff;
        }
    }
}

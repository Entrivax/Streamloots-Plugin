package fr.entrivax.streamloots;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import fr.entrivax.streamloots.commands.IStreamlootsCardCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardGiveItemCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardHealCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardPlaySoundCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardSetHealthCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardSetHungerCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardSpawnItemCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardChatCommandCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardCommandsProcessor;
import fr.entrivax.streamloots.commands.StreamlootsCardDelayCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardDeleteCurrentItemCommand;
import fr.entrivax.streamloots.commands.StreamlootsCardDropCurrentCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class StreamlootsRedemptionHandler implements IStreamlootsRedemptionHandler {
    private List<CardConfig> _cardConfigs;
    private Server _server;
    private JavaPlugin _plugin;
    private Logger _logger;
    private ArrayList<StreamlootsCardCommandsProcessor> _runningProcessors;
    public StreamlootsRedemptionHandler(JavaPlugin plugin, List<CardConfig> cardConfigs, Logger logger) {
        _cardConfigs = cardConfigs;
        _logger = logger;
        _logger.log(Level.INFO, "Loaded " + cardConfigs.size() + " cards");
        _plugin = plugin;
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
            EntryPoint.logger.log(Level.INFO, "Comparing " + cardInfo.data.cardId + " with " + cardConfig.id);
            if (cardConfig.id.equals(cardInfo.data.cardId)) {
                EntryPoint.logger.log(Level.INFO, "Card config found");
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
        switch (cardCommand.type) {
            case COMMAND:
                return new StreamlootsCardChatCommandCommand(_plugin, cardCommand.command);
            case DELAY:
                return new StreamlootsCardDelayCommand(_plugin, cardCommand.amount);
            case DELETECURRENT:
                return new StreamlootsCardDeleteCurrentItemCommand(_plugin, cardCommand.applyOn, _logger);
            case DROPCURRENT:
                return new StreamlootsCardDropCurrentCommand(_plugin, cardCommand.applyOn, _logger);
            case GIVEITEM:
                return new StreamlootsCardGiveItemCommand(_plugin, cardCommand.applyOn, cardCommand.item, cardCommand.amount, _logger);
            case HEAL:
                return new StreamlootsCardHealCommand(_plugin, cardCommand.applyOn, cardCommand.amount, _logger);
            case PLAYSOUND:
                return new StreamlootsCardPlaySoundCommand(_plugin, cardCommand.applyOn, cardCommand.sound, cardCommand.position);
            case SETHEALTH:
                return new StreamlootsCardSetHealthCommand(_plugin, cardCommand.applyOn, cardCommand.amount, _logger);
            case SETHUNGER:
                return new StreamlootsCardSetHungerCommand(_plugin, cardCommand.applyOn, cardCommand.amount, _logger);
            case SPAWNITEM:
                return new StreamlootsCardSpawnItemCommand(_plugin, cardCommand.applyOn, cardCommand.item, cardCommand.amount, cardCommand.position, _logger);
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

package fr.entrivax.streamlootsbase;

import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.entrivax.streamlootscore.CardCommand;
import fr.entrivax.streamlootscore.StreamlootsPlugin;
import fr.entrivax.streamlootscore.commands.IStreamlootsCardCommand;
import fr.entrivax.streamlootscore.commands.IStreamlootsCardRegistry;
import fr.entrivax.streamlootscore.commands.IStreamlootsCommandBuilder;
import fr.entrivax.streamlootsbase.commands.*;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StreamlootsBasePlugin extends JavaPlugin {
    Logger logger;
    @Override
    public void onEnable() {
        logger = getLogger();
        StreamlootsPlugin streamlootsPlugin = (StreamlootsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Streamloots-Core");
        IStreamlootsCardRegistry registry = streamlootsPlugin.getCardRegistry();
        registerCardBuilders(registry);
    }

    private void registerCardBuilders(IStreamlootsCardRegistry registry) {
        JavaPlugin plugin = this;
        Logger logger = this.logger;
        registry.registerBuilder("COMMAND", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardChatCommandCommand(plugin, cardCommand.command.get("command").getAsString());
            }
        });

        registry.registerBuilder("DELAY", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardDelayCommand(plugin, cardCommand.command.get("amount").getAsInt());
            }
        });

        registry.registerBuilder("DELETECURRENT", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardDeleteCurrentItemCommand(plugin, cardCommand.command.get("applyOn").getAsString(), logger);
            }
        });

        registry.registerBuilder("DROPCURRENT", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardDropCurrentCommand(plugin, cardCommand.command.get("applyOn").getAsString(), logger);
            }
        });

        registry.registerBuilder("GIVEITEM", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                JsonElement amount = cardCommand.command.get("amount");
                return new StreamlootsCardGiveItemCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("item").getAsString(), amount != null ? amount.getAsInt() : null, logger);
            }
        });

        registry.registerBuilder("HEAL", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardHealCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("amount").getAsInt(), logger);
            }
        });

        registry.registerBuilder("PLAYSOUND", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardPlaySoundCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("sound").getAsString(), getPositionFrom(cardCommand.command.getAsJsonObject("position")));
            }
        });

        registry.registerBuilder("SETHEALTH", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardSetHealthCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("amount").getAsInt(), logger);
            }
        });

        registry.registerBuilder("SETHUNGER", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                return new StreamlootsCardSetHungerCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("amount").getAsInt(), logger);
            }
        });

        registry.registerBuilder("SPAWNITEM", new IStreamlootsCommandBuilder(){
            @Override
            public IStreamlootsCardCommand build(CardCommand cardCommand) {
                JsonElement amount = cardCommand.command.get("amount");
                return new StreamlootsCardSpawnItemCommand(plugin, cardCommand.command.get("applyOn").getAsString(), cardCommand.command.get("item").getAsString(), amount != null ? amount.getAsInt() : null, getPositionFrom(cardCommand.command.getAsJsonObject("position")), logger);
            }
        });
    }

    private Position getPositionFrom(JsonObject jsonObject) {
        Position position = new Position();
        position.front = jsonObject.get("front") != null ? jsonObject.get("front").getAsDouble() : null;
        position.left = jsonObject.get("left") != null ? jsonObject.get("left").getAsDouble() : null;
        position.x = jsonObject.get("x") != null ? jsonObject.get("x").getAsDouble() : null;
        position.y = jsonObject.get("y") != null ? jsonObject.get("y").getAsDouble() : null;
        position.z = jsonObject.get("z") != null ? jsonObject.get("z").getAsDouble() : null;
        position.rx = jsonObject.get("rx") != null ? jsonObject.get("rx").getAsDouble() : null;
        position.ry = jsonObject.get("ry") != null ? jsonObject.get("ry").getAsDouble() : null;
        position.rz = jsonObject.get("rz") != null ? jsonObject.get("rz").getAsDouble() : null;
        return position;
    }
}

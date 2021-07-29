package fr.entrivax.streamlootscore;

import com.google.gson.JsonObject;

public class CardCommand {
    public String type;
    public JsonObject command;

    public CardCommand(String type, JsonObject command) {
        this.type = type;
        this.command = command;
    }

    public CardCommand clone() {
        return new CardCommand(type, command.deepCopy());
    }
}

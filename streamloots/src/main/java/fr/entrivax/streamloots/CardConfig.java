package fr.entrivax.streamloots;

import java.util.List;

public class CardConfig {
    public String id;
    public List<CardCommand> commands;

    public CardConfig(String id, List<CardCommand> commands) {
        this.id = id;
        this.commands = commands;
    }
}

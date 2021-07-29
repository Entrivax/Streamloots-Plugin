package fr.entrivax.streamloots.commands;

import fr.entrivax.streamloots.CardCommand;

public interface IStreamlootsCommandBuilder {
    public IStreamlootsCardCommand build(CardCommand cardCommand);
}

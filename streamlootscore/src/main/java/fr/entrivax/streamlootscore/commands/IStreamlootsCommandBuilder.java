package fr.entrivax.streamlootscore.commands;

import fr.entrivax.streamlootscore.CardCommand;

public interface IStreamlootsCommandBuilder {
    public IStreamlootsCardCommand build(CardCommand cardCommand);
}

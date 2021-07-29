package fr.entrivax.streamlootscore.commands;

import java.util.HashMap;

public class StreamlootsCardRegistry implements IStreamlootsCardRegistry {
    private HashMap<String, IStreamlootsCommandBuilder> _commandBuilders;

    public StreamlootsCardRegistry() {
        _commandBuilders = new HashMap<String, IStreamlootsCommandBuilder>();
    }

    public StreamlootsCardRegistry registerBuilder(String cardType, IStreamlootsCommandBuilder commandBuilder) {
        _commandBuilders.put(cardType, commandBuilder);
        return this;
    }

    public void destroy() {
        _commandBuilders = null;
    }

    @Override
    public IStreamlootsCommandBuilder getBuilder(String cardType) {
        return _commandBuilders.get(cardType);
    }
}

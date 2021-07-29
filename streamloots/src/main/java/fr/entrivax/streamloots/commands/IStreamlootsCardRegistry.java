package fr.entrivax.streamloots.commands;

public interface IStreamlootsCardRegistry {
    public StreamlootsCardRegistry registerBuilder(String cardType, IStreamlootsCommandBuilder commandBuilder);
    public IStreamlootsCommandBuilder getBuilder(String cardType);
}

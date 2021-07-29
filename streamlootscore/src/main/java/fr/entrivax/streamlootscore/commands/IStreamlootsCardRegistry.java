package fr.entrivax.streamlootscore.commands;

public interface IStreamlootsCardRegistry {
    public StreamlootsCardRegistry registerBuilder(String cardType, IStreamlootsCommandBuilder commandBuilder);
    public IStreamlootsCommandBuilder getBuilder(String cardType);
}

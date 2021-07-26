package fr.entrivax.streamloots.commands;

public interface IStreamlootsCardCommand {

    /**
     * @param next Function to call when the command as finished processing
     * @return Function to call to stop the current task
     */
    public Runnable run(Runnable next);
}

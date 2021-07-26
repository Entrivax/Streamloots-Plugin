package fr.entrivax.streamloots.commands;

public class StreamlootsCardCommandsProcessor {
    IStreamlootsCardCommand[] _commands;
    Integer _currentCommand;
    Runnable _cancelCurrentTask;
    Runnable _onFinished;
    public StreamlootsCardCommandsProcessor(IStreamlootsCardCommand[] commands) {
        this._commands = commands;
    }

    public void run(Runnable onFinished) {
        this._onFinished = onFinished;
        this._cancelCurrentTask = null;
        this._currentCommand = 0;
        runNext();
    }

    public void cancel() {
        if (_cancelCurrentTask != null) {
            _cancelCurrentTask.run();
            _cancelCurrentTask = null;
        }
    }

    private void runNext() {
        if (_currentCommand >= _commands.length) {
            _cancelCurrentTask = null;
            if (_onFinished != null) {
                _onFinished.run();
            }
            return;
        }
        IStreamlootsCardCommand command = _commands[_currentCommand++];
        _cancelCurrentTask = command.run(() -> {
            runNext();
        });
    }
}

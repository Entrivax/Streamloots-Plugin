package fr.entrivax.streamloots;

public interface IStreamlootsRedemptionHandler {
	public void handle(StreamlootsRedemption cardInfo);
	public void cancelRunningTasks();
}

package fr.entrivax.streamlootscore;

public interface IStreamlootsRedemptionHandler {
	public void handle(StreamlootsRedemption cardInfo);
	public void cancelRunningTasks();
}

package fr.entrivax.streamloots;

public class StreamlootsRedemption {
	public String imageUrl;
	public String soundUrl;
	public String message;
	public StreamlootsRedemptionData data;
	
	public class StreamlootsRedemptionData {
		public String cardId;
		public String cardSetId;
		public String cardName;
		public String cardRarity;
		public String description;
		public String type;
	}
}
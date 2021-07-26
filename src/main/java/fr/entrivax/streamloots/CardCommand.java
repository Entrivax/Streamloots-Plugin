package fr.entrivax.streamloots;

import com.google.gson.annotations.SerializedName;

public class CardCommand {
    public String command;

    public String applyOn;
    public String item;
    public Integer amount;
    public String sound;
    public Position position;
    public CardCommandType type;

    public enum CardCommandType {
        @SerializedName("COMMAND")
        COMMAND,
        @SerializedName("GIVEITEM")
        GIVEITEM,
        @SerializedName("DROPCURRENT")
        DROPCURRENT,
        @SerializedName("HEAL")
        HEAL,
        @SerializedName("SETHEALTH")
        SETHEALTH,
        @SerializedName("SETHUNGER")
        SETHUNGER,
        @SerializedName("PLAYSOUND")
        PLAYSOUND,
        @SerializedName("DELAY")
        DELAY
    }
}

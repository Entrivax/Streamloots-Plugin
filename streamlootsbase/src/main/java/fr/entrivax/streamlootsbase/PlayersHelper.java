package fr.entrivax.streamlootsbase;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class PlayersHelper {
    public static Location getLocationFromPlayer(Position position, Player player) {
        Location playerLocation = player.getLocation();
        double yaw = playerLocation.getYaw() * Math.PI / 180.0;
        if (position.x != null) {
            playerLocation.setX(position.x);
        }
        if (position.y != null) {
            playerLocation.setY(position.y);
        }
        if (position.z != null) {
            playerLocation.setZ(position.z);
        }
        if (position.front != null) {
            double dx = -Math.sin(yaw);
            double dz = Math.cos(yaw);
            playerLocation.add(dx * position.front, 0, dz * position.front);
        }
        if (position.left != null) {
            double dx = -Math.sin(yaw + Math.PI / 2.0);
            double dz = Math.cos(yaw + Math.PI / 2.0);
            playerLocation.add(dx * position.left, 0, dz * position.left);
        }
        if (position.rx != null) {
            playerLocation.add(position.rx, 0, 0);
        }
        if (position.ry != null) {
            playerLocation.add(0, position.ry, 0);
        }
        if (position.rz != null) {
            playerLocation.add(0, 0, position.ry);
        }
        return playerLocation;
    }

    public static List<Player> getTargetedPlayers(Server server, String playerString) {
        boolean revertResult = false;
        ArrayList<Player> players = new ArrayList<Player>();
        if (playerString.startsWith("^")) {
            playerString = playerString.substring(1);
            revertResult = true;
        }
        String[] targets = playerString.split(",");
        if (!revertResult) {
            for (int i = 0; i < targets.length; i++) {
                Player player = server.getPlayer(targets[i]);
                if (player != null) {
                    players.add(player);
                }
            }
        } else {
            Player[] onlinePlayers = (Player[])server.getOnlinePlayers().toArray();

            ArrayList<Player> playersToRemove = new ArrayList<Player>();
            for (int i = 0; i < targets.length; i++) {
                Player player = server.getPlayer(targets[i]);
                if (player != null) {
                    playersToRemove.add(player);
                }
            }
            for (int i = 0; i < onlinePlayers.length; i++) {
                Player player = onlinePlayers[i];
                if (!playersToRemove.contains(player)) {
                    players.add(player);
                }
            }
        }

        return players;
    }
}

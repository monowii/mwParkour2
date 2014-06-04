package fr.monowii.parkour2;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class Lobby {

    private static Location lobby = null;

    public static void load() {
        FileConfiguration cfg = Parkour2.getParkoursConfig();

        if (cfg.getString("lobby.world") != "undefined") {
            Location lobby = new Location(Parkour2.getPlugin().getServer().getWorld(cfg.getString("lobby.world")),
                    cfg.getDouble("lobby.x"),
                    cfg.getDouble("lobby.y"),
                    cfg.getDouble("lobby.z"));

            lobby.setPitch((float)cfg.getDouble("lobby.pitch"));
            lobby.setYaw((float)cfg.getDouble("lobby.yaw"));
        }
    }

    public static void save() {
        FileConfiguration cfg = Parkour2.getParkoursConfig();
        cfg.set("lobby.world", lobby.getWorld().getName());
        cfg.set("lobby.x", lobby.getX());
        cfg.set("lobby.y", lobby.getY());
        cfg.set("lobby.z", lobby.getZ());
        cfg.set("lobby.pitch", lobby.getPitch());
        cfg.set("lobby.yaw", lobby.getYaw());
        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setLobby(Location loc) {
        lobby = loc;
    }

    public static Location getLobby() {
        return lobby;
    }

}

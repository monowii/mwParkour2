package fr.monowii.parkour2.managers;

import fr.monowii.parkour2.Parkour2;
import fr.monowii.parkour2.PlayerParkourInfo;
import fr.monowii.parkour2.level.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayersManager {

    private HashMap<String, PlayerParkourInfo> players = new HashMap<String, PlayerParkourInfo>(); //playerName / PlayerParkourInfo

    //Players
    public void addPlayer(Player p, int levelId) {
        if (!Parkour2.getLevelsManager().containsLevel(levelId))
            return;

        if (players.containsKey(p.getName()))
            players.remove(p.getName());

        players.put(p.getName(), new PlayerParkourInfo(levelId));
    }

    public void removePlayer(Player p) {
        players.remove(p.getName());
    }

    public PlayerParkourInfo getPlayer(Player p) {
        return players.get(p.getName());
    }

    public HashMap<String, PlayerParkourInfo> getPlayers() {
        return players;
    }

    public boolean containsPlayer(Player p) {
        return players.containsKey(p.getName());
    }

    public void teleportToLastCheckpoint(Player player, Level level) {
        PlayerParkourInfo ppi = players.get(player.getName());

        player.setFireTicks(0);

        if (ppi.getLastCheckpoint() == 0 || !level.getOptions().isRespawnAtCheckpoint()) {
            player.teleport(level.getSpawn());
        } else {
            Location loc = level.getCheckpoints().get(ppi.getLastCheckpoint()).clone();
            loc.setX(loc.getBlockX() + 0.5);

            if (loc.clone().add(0, -1, 0).getBlock().getType() == Material.FENCE || loc.clone().add(0, -1, 0).getBlock().getType() == Material.NETHER_FENCE)
                loc.setY(loc.getBlockY()+0.5);

            loc.setZ(loc.getBlockZ()+0.5);
            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
            player.teleport(loc);
        }
    }
}

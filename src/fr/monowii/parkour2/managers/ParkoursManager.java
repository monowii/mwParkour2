package fr.monowii.parkour2.managers;

import fr.monowii.parkour2.Parkour2;
import fr.monowii.parkour2.parkour.CheckpointInfo;
import fr.monowii.parkour2.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ParkoursManager
{
    private HashMap<Integer, Parkour> parkours = new LinkedHashMap<Integer, Parkour>(); //ParkourId / Parkour


    public void loadParkours() {
        parkours.clear();
        FileConfiguration cfg = Parkour2.getParkoursConfig();

        if (!cfg.isConfigurationSection("parkours")) {
            cfg.set("parkours", "");
            try {
                cfg.save(Parkour2.getParkoursFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        for (String parkourId : cfg.getConfigurationSection("parkours").getKeys(false))
        {
            //TODO Add a catch for null config ?
            String rootNode = "parkours."+parkourId+".";

            Parkour parkour = new Parkour(Integer.valueOf(parkourId));
            parkour.setName(cfg.getString(rootNode+"name"));
            parkour.setAuthors(cfg.getString(rootNode + "authors"));
            parkour.setActive(cfg.getBoolean(rootNode+"active"));

            Location start = new Location(
                    Bukkit.getWorld(cfg.getString(rootNode+"world")),
                    cfg.getDouble(rootNode+"spawn.x"),
                    cfg.getDouble(rootNode+"spawn.y"),
                    cfg.getDouble(rootNode+"spawn.z"));
            start.setYaw((float)cfg.getDouble(rootNode+"spawn.yaw"));
            start.setPitch((float) cfg.getDouble(rootNode + "spawn.pitch"));
            parkour.setSpawn(start);

            if (cfg.isConfigurationSection(rootNode+"checkpoints"))
            {
                for (String checkpointId : cfg.getConfigurationSection(rootNode+"checkpoints").getKeys(false))
                {
                    String rootCheckpoint = rootNode+"checkpoints."+checkpointId+".";

                    Location checkpoint = new Location(
                            Bukkit.getWorld(cfg.getString(rootNode+"world")),
                            cfg.getDouble(rootCheckpoint+"x"),
                            cfg.getDouble(rootCheckpoint+"y"),
                            cfg.getDouble(rootCheckpoint+"z"));
                    parkour.getCheckpoints().add(Integer.valueOf(checkpointId), checkpoint);
                }
            }

            parkour.getOptions().setRespawnAtCheckpoint(cfg.getBoolean(rootNode+"options.respawnAtCheckpoint"));
            parkour.getOptions().setLavaRespawn(cfg.getBoolean(rootNode+"options.lavaRespawn"));
            parkour.getOptions().setWaterRespawn(cfg.getBoolean(rootNode+"options.waterRespawn"));
            parkour.getOptions().setVoidRespawn(cfg.getBoolean(rootNode+"options.voidRespawn"));

            parkours.put(parkour.getId(), parkour);
        }
    }

    public boolean containsParkour(int parkourId) {
        return parkours.containsKey(parkourId);
    }

    public Parkour getParkour(int parkourId) {
        return parkours.get(parkourId);
    }

    public HashMap<Integer, Parkour> getParkours() {
        return parkours;
    }

    public int createParkour(Location spawn, String parkourName, String authors) {
        int maxParkourId = 0;
        for (int parkourId : parkours.keySet())
            if (parkourId > maxParkourId)
                maxParkourId = parkourId;
        maxParkourId++;

        Parkour parkour = new Parkour(maxParkourId);
        parkour.setName(parkourName);
        parkour.setSpawn(spawn);
        parkour.setAuthors(authors);

        FileConfiguration cfg = Parkour2.getParkoursConfig();
        String rootNode = "parkours."+maxParkourId+".";

        cfg.set(rootNode+"name", parkour.getName());
        cfg.set(rootNode+"authors", parkour.getAuthors());
        cfg.set(rootNode+"world", spawn.getWorld().getName());
        cfg.set(rootNode+"active", parkour.isActive());

        cfg.set(rootNode+"spawn.x", spawn.getX());
        cfg.set(rootNode+"spawn.y", spawn.getY());
        cfg.set(rootNode+"spawn.z", spawn.getZ());
        cfg.set(rootNode+"spawn.pitch", spawn.getPitch());
        cfg.set(rootNode+"spawn.yaw", spawn.getYaw());

        cfg.set(rootNode+"options.waterRespawn", true);
        cfg.set(rootNode+"options.lavaRespawn", true);
        cfg.set(rootNode+"options.voidRespawn", true);
        cfg.set(rootNode+"options.respawnAtCheckpoint", true);

        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        parkours.put(parkour.getId(), parkour);
        return parkour.getId();
    }

    public void deleteDelete(int parkourId) {
        FileConfiguration cfg = Parkour2.getParkoursConfig();

        cfg.set("parkours."+parkourId, null);
        parkours.remove(parkourId);

        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Checkpoints
    public boolean isCheckpoint(Location loc) {
        for (Parkour parkour : parkours.values())
            for (Location checkpoint : parkour.getCheckpoints())
                if (checkpoint.getBlockX() == loc.getBlockX() && checkpoint.getBlockY() == loc.getBlockY() && checkpoint.getBlockZ() == loc.getBlockZ())
                    return true;
        return false;
    }

    public CheckpointInfo getCheckpoint(Location loc) {
        for (Parkour parkour : parkours.values())
        {
            for (int i = 0 ; i < parkour.getCheckpoints().size() ; i++)
            {
                Location checkpoint = parkour.getCheckpoints().get(i);

                if (checkpoint.getBlockX() == loc.getBlockX() && checkpoint.getBlockY() == loc.getBlockY() && checkpoint.getBlockZ() == loc.getBlockZ())
                {
                    CheckpointInfo.CheckpointType checkpointType = CheckpointInfo.CheckpointType.CHECKPOINT;

                    if (parkour.getCheckpoints().size()-1 == i)
                        checkpointType = CheckpointInfo.CheckpointType.END;
                    else if (i == 0)
                        checkpointType = CheckpointInfo.CheckpointType.START;

                    return new CheckpointInfo(parkour.getId(), i, checkpointType);
                }
            }
        }
        return null;
    }
}
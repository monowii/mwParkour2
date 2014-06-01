package fr.monowii.parkour2.managers;

import fr.monowii.parkour2.Parkour2;
import fr.monowii.parkour2.level.CheckpointInfo;
import fr.monowii.parkour2.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LevelsManager
{
    private HashMap<Integer, Level> levels = new LinkedHashMap<Integer, Level>(); //LevelId / Level


    public void loadLevels() {
        levels.clear();
        FileConfiguration cfg = Parkour2.getLevelsConfig();

        if (!cfg.isConfigurationSection("levels")) {
            cfg.set("levels", "");
            try {
                cfg.save(Parkour2.getLevelsFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        for (String levelId : cfg.getConfigurationSection("levels").getKeys(false))
        {
            //TODO Add a catch for null config ?
            String rootNode = "levels."+levelId+".";

            Level level = new Level(Integer.valueOf(levelId));
            level.setName(cfg.getString(rootNode+"name"));
            level.setAuthors(cfg.getString(rootNode + "authors"));
            level.setActive(cfg.getBoolean(rootNode+"active"));

            Location start = new Location(
                    Bukkit.getWorld(cfg.getString(rootNode+"world")),
                    cfg.getDouble(rootNode+"spawn.x"),
                    cfg.getDouble(rootNode+"spawn.y"),
                    cfg.getDouble(rootNode+"spawn.z"));
            start.setYaw((float)cfg.getDouble(rootNode+"spawn.yaw"));
            start.setPitch((float) cfg.getDouble(rootNode + "spawn.pitch"));
            level.setSpawn(start);

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
                    level.getCheckpoints().add(Integer.valueOf(checkpointId), checkpoint);
                }
            }

            level.getOptions().setRespawnAtCheckpoint(cfg.getBoolean(rootNode+"options.respawnAtCheckpoint"));
            level.getOptions().setLavaRespawn(cfg.getBoolean(rootNode+"options.lavaRespawn"));
            level.getOptions().setWaterRespawn(cfg.getBoolean(rootNode+"options.waterRespawn"));
            level.getOptions().setVoidRespawn(cfg.getBoolean(rootNode+"options.voidRespawn"));

            levels.put(level.getId(), level);
        }
    }

    public boolean containsLevel(int levelId) {
        return levels.containsKey(levelId);
    }

    public Level getLevel(int levelId) {
        return levels.get(levelId);
    }

    public HashMap<Integer, Level> getLevels() {
        return levels;
    }

    public int createLevel(Location spawn, String levelName, String authors) {
        int maxLevelId = 0;
        for (int levelId : levels.keySet())
            if (levelId > maxLevelId)
                maxLevelId = levelId;
        maxLevelId++;

        Level level = new Level(maxLevelId);
        level.setName(levelName);
        level.setSpawn(spawn);
        level.setAuthors(authors);

        FileConfiguration cfg = Parkour2.getLevelsConfig();
        String rootNode = "levels."+maxLevelId+".";

        cfg.set(rootNode+"name", level.getName());
        cfg.set(rootNode+"authors", level.getAuthors());
        cfg.set(rootNode+"world", spawn.getWorld().getName());
        cfg.set(rootNode+"active", level.isActive());

        cfg.set(rootNode+"spawn.x", spawn.getX());
        cfg.set(rootNode+"spawn.y", spawn.getY());
        cfg.set(rootNode+"spawn.z", spawn.getZ());
        cfg.set(rootNode+"spawn.pitch", spawn.getPitch());
        cfg.set(rootNode+"spawn.yaw", spawn.getYaw());

        cfg.set(rootNode+"options.waterRespawn", false);
        cfg.set(rootNode+"options.lavaRespawn", false);
        cfg.set(rootNode+"options.voidRespawn", false);
        cfg.set(rootNode+"options.respawnAtCheckpoint", false);

        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        levels.put(level.getId(), level);
        return level.getId();
    }

    public void deleteDelete(int levelId) {
        FileConfiguration cfg = Parkour2.getLevelsConfig();

        cfg.set("levels."+levelId, null);
        levels.remove(levelId);

        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Checkpoints
    public boolean isCheckpoint(Location loc) {
        for (Level l : levels.values())
            for (Location checkpoint : l.getCheckpoints())
                if (checkpoint.getBlockX() == loc.getBlockX() && checkpoint.getBlockY() == loc.getBlockY() && checkpoint.getBlockZ() == loc.getBlockZ())
                    return true;
        return false;
    }

    public CheckpointInfo getCheckpoint(Location loc) {
        for (Level level : levels.values())
        {
            for (int i = 0 ; i < level.getCheckpoints().size() ; i++)
            {
                Location checkpoint = level.getCheckpoints().get(i);

                if (checkpoint.getBlockX() == loc.getBlockX() && checkpoint.getBlockY() == loc.getBlockY() && checkpoint.getBlockZ() == loc.getBlockZ())
                {
                    CheckpointInfo.CheckpointType checkpointType = CheckpointInfo.CheckpointType.CHECKPOINT;
                    if (level.getCheckpoints().size()-1 == i)
                        checkpointType = CheckpointInfo.CheckpointType.END;
                    else if (i == 0)
                        checkpointType = CheckpointInfo.CheckpointType.START;

                    return new CheckpointInfo(level.getId(), i, checkpointType);
                }
            }
        }
        return null;
    }
}
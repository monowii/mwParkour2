package fr.monowii.parkour2.level;

import fr.monowii.parkour2.Parkour2;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.util.ArrayList;

public class Level
{
    private int id;
    private String name;
    private String authors;
    private boolean active;

    private ParkourOptions options;

    private Location spawn;
    private ArrayList<Location> checkpoints = new ArrayList<Location>();

    public Level(int id) {
        this.id = id;
        this.active = false;
        this.name = "Unknown";
        this.authors = "Unknown";
        this.options = new ParkourOptions(this.id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        FileConfiguration cfg = Parkour2.getLevelsConfig();
        cfg.set("levels."+id+".name", name);
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.name = name;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        FileConfiguration cfg = Parkour2.getLevelsConfig();
        cfg.set("levels." + id + ".authors", authors);
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.authors = authors;
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        FileConfiguration cfg = Parkour2.getLevelsConfig();
        cfg.set("levels."+id+".active", active);
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.active = active;
    }


    public void setOptions(ParkourOptions options) {
        this.options = options;
    }

    public ParkourOptions getOptions() {
        return options;
    }


    public ArrayList<Location> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(ArrayList<Location> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public boolean removeLastCheckpoint() {
        int lastCheckpoint = checkpoints.size()-1;
        if (lastCheckpoint < 0)
            return false;
        checkpoints.remove(lastCheckpoint);

        FileConfiguration cfg = Parkour2.getLevelsConfig();
        cfg.set("levels."+id+".checkpoints."+lastCheckpoint, null);
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void addCheckpoint(Location loc) {
        int checkpoint = checkpoints.size();
        checkpoints.add(checkpoint, loc);

        FileConfiguration cfg = Parkour2.getLevelsConfig();
        String checkpointRoot = "levels."+id+".checkpoints."+checkpoint+".";
        cfg.set(checkpointRoot+"x", loc.getBlockX());
        cfg.set(checkpointRoot+"y", loc.getBlockY());
        cfg.set(checkpointRoot+"z", loc.getBlockZ());
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        FileConfiguration cfg = Parkour2.getLevelsConfig();
        cfg.set("levels."+id+".world", spawn.getWorld().getName());
        cfg.set("levels."+id+".spawn.x", spawn.getX());
        cfg.set("levels."+id+".spawn.y", spawn.getY());
        cfg.set("levels."+id+".spawn.z", spawn.getZ());
        cfg.set("levels."+id+".spawn.pitch", spawn.getPitch());
        cfg.set("levels."+id+".spawn.yaw", spawn.getYaw());
        try {
            cfg.save(Parkour2.getLevelsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.spawn = spawn;
    }
}

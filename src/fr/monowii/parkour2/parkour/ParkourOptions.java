package fr.monowii.parkour2.parkour;

import fr.monowii.parkour2.Parkour2;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class ParkourOptions
{
    private boolean waterRespawn;
    private boolean lavaRespawn;
    private boolean voidRespawn;
    private boolean respawnAtCheckpoint;
    private int parkourId;

    public ParkourOptions(int parkourId) {
        this.parkourId = parkourId;
        this.waterRespawn = true;
        this.lavaRespawn = true;
        this.voidRespawn = true;
        this.respawnAtCheckpoint = true;
    }

    public boolean isWaterRespawn() {
        return waterRespawn;
    }

    public void setWaterRespawn(boolean waterRespawn) {
        FileConfiguration cfg = Parkour2.getParkoursConfig();
        cfg.set("parkours."+parkourId+".options.waterRespawn", waterRespawn);
        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.waterRespawn = waterRespawn;
    }

    public boolean isLavaRespawn() {
        return lavaRespawn;
    }

    public void setLavaRespawn(boolean lavaRespawn) {
        FileConfiguration cfg = Parkour2.getParkoursConfig();
        cfg.set("parkours."+parkourId+".options.lavaRespawn", lavaRespawn);
        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.lavaRespawn = lavaRespawn;
    }

    public boolean isVoidRespawn() {
        return voidRespawn;
    }

    public void setVoidRespawn(boolean voidRespawn) {
        FileConfiguration cfg = Parkour2.getParkoursConfig();
        cfg.set("parkours."+parkourId+".options.voidRespawn", voidRespawn);
        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.voidRespawn = voidRespawn;
    }




    public boolean isRespawnAtCheckpoint() {
        return respawnAtCheckpoint;
    }

    public void setRespawnAtCheckpoint(boolean respawnAtCheckpoint) {
        FileConfiguration cfg = Parkour2.getParkoursConfig();
        cfg.set("parkours."+parkourId+".options.respawnAtCheckpoint", respawnAtCheckpoint);
        try {
            cfg.save(Parkour2.getParkoursFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.respawnAtCheckpoint = respawnAtCheckpoint;
    }


}

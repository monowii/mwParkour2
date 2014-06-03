package fr.monowii.parkour2.managers;

public class PlayerParkourInfo
{
    private long startTime;
    private int parkourId;
    private int lastCheckpoint;

    public PlayerParkourInfo(int parkourId) {
        this.parkourId = parkourId;
        this.startTime = System.currentTimeMillis();
        lastCheckpoint = 0;
    }

    public long getStartTime() {
        return startTime;
    }

    public void resetStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public int getLastCheckpoint() {
        return lastCheckpoint;
    }

    public void setLastCheckpoint(int lastCheckpoint) {
        this.lastCheckpoint = lastCheckpoint;
    }

    public int getParkourId() {
        return parkourId;
    }
}

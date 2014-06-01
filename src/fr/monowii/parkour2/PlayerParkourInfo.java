package fr.monowii.parkour2;

public class PlayerParkourInfo
{
    private long startTime;
    private int levelId;
    private int lastCheckpoint;

    public PlayerParkourInfo(int levelId) {
        this.levelId = levelId;
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

    public int getLevelId() {
        return levelId;
    }
}

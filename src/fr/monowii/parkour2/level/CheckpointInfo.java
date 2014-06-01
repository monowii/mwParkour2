package fr.monowii.parkour2.level;

public class CheckpointInfo
{
    public enum CheckpointType {
        START,
        CHECKPOINT,
        END;
    };

    private int levelId;
    private int checkpoint;
    private CheckpointType checkpointType;

    public CheckpointInfo(int levelId, int checkpoint, CheckpointType checkpointType) {
        this.levelId = levelId;
        this.checkpoint = checkpoint;
        this.checkpointType = checkpointType;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public int getLevelId() {
        return levelId;
    }

    public CheckpointType getCheckpointType() {
        return checkpointType;
    }
}

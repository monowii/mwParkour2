package fr.monowii.parkour2.parkour;

public class CheckpointInfo
{
    public enum CheckpointType {
        START,
        CHECKPOINT,
        END;
    };

    private int parkourId;
    private int checkpoint;
    private CheckpointType checkpointType;

    public CheckpointInfo(int parkourId, int checkpoint, CheckpointType checkpointType) {
        this.parkourId = parkourId;
        this.checkpoint = checkpoint;
        this.checkpointType = checkpointType;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public int getParkourId() {
        return parkourId;
    }

    public CheckpointType getCheckpointType() {
        return checkpointType;
    }
}

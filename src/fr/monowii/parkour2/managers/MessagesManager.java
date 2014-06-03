package fr.monowii.parkour2.managers;

public class MessagesManager {

    public static String prefix = "§7[§dmwParkour2§7]§f ";

    public static String playerJoinParkour = "§aYou joined parkour %parkourName (%parkourId)";
    public static String playerLeaveParkour = "§aYou left parkour %parkourName";
    public static String playerStartParkour = "§bYou started parkour %parkourName by %authors !";
    public static String playerRestartParkour = "§bYou restarted parkour %parkourName by %authors !";
    public static String playerCheckpointParkour = "§bYou passed checkpoint %checkpoint/%totalCheckpoints";
    public static String playerEndParkour = "§3You finished %parkourName in %time !";
    public static String playerBestScore = "§6You beat your old best time !";

    public static String PlayerForgotLastCheckpoint = "§cYou have forgot the last checkpoint !";
    public static String PlayerAlreadyPassedCheckpoint = "§cYou already have passed this checkpoint !";
    public static String PlayerBadParkour = "§cYou are not on the good parkour !";

    public static String parkourCreated = "§aParkour %parkourId (%parkourName) created !";
    public static String parkourDeleted = "§aParkour %parkourId deleted !";
    public static String parkourActive = "§aParkour %parkourId activation is %activeState !";
    public static String parkourSetSpawn = "§aSpawn set !";
    public static String parkourSetName = "§aNew name set !";
    public static String parkourSetAuthors = "§aAuthors set !";
    public static String parkourSetOption = "§aOption set !";

    public static String checkpointAdded = "§aCheckpoint added on parkour %parkourName !";
    public static String lastCheckpointRemoved = "§aLast checkpoint removed !";

    public static String ErrorRemoveLastCheckpoint = "§cThere is no checkpoints in this parkour !";
    public static String ErrorNotEnoughCheckpointsToActive = "§cThere is not enough checkpoints to toggle this parkour ON !";
    public static String ErrorCheckpointUsedByParkour = "§cThis checkpoint is already used by a parkour !";
    public static String ErrorParkourNotActive = "§cThis parkour is not active !";
    public static String ErrorParkourNameTooLong = "§cParkour name is too long (25characters max)! ";
    public static String ErrorAuthorsTooLong = "§cAuthors string is too long (25characters max)! ";
    public static String ErrorArgs = "§cBad args !";

    static {

    }

}

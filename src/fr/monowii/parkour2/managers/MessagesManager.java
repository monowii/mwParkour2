package fr.monowii.parkour2.managers;

public class MessagesManager {

    public static String prefix = "§7[§dmwParkour2§7]§f ";

    public static String playerJoinLevel = "§aYou joined level %levelName (%levelId)";
    public static String playerLeaveLevel = "§aYou left level %levelName";
    public static String playerStartLevel = "§bYou started parkour %levelName by %authors !";
    public static String playerRestartLevel = "§bYou restarted parkour %levelName by %authors !";
    public static String playerCheckpointLevel = "§bYou passed checkpoint %checkpoint/%totalCheckpoints";
    public static String playerEndLevel = "§3You finished %levelName in %time !";
    public static String playerBestScore = "§6You beat your old best time !";

    public static String PlayerForgotLastCheckpoint = "§cYou have forgot the last checkpoint !";
    public static String PlayerAlreadyPassedCheckpoint = "§cYou already have passed this checkpoint !";
    public static String PlayerBadParkour = "§cYou are not on the good parkour !";

    public static String levelCreated = "§aLevel %levelId (%levelName) created !";
    public static String levelDeleted = "§aLevel %levelId deleted !";
    public static String levelActive = "§aLevel %levelId activation is %activeState !";
    public static String levelSetSpawn = "§aSpawn set !";
    public static String levelSetName = "§aNew name set !";
    public static String levelSetAuthors = "§aAuthors set !";
    public static String levelSetOption = "§aOption set !";

    public static String checkpointAdded = "§aCheckpoint added on level %levelName !";
    public static String lastCheckpointRemoved = "§aLast checkpoint removed !";

    public static String ErrorRemoveLastCheckpoint = "§cThere is no checkpoints in this level !";
    public static String ErrorNotEnoughCheckpointsToActive = "§cThere is not enough checkpoints to toggle this level ON !";
    public static String ErrorCheckpointUsedByLevel = "§cThis checkpoint is already used by a level !";
    public static String ErrorLevelNotActive = "§cThis level is not active !";
    public static String ErrorLevelNameTooLong = "§cLevel name is too long (25characters max)! ";
    public static String ErrorAuthorsTooLong = "§cAuthors string is too long (25characters max)! ";
    public static String ErrorArgs = "§cBad args !";

    static {

    }

}

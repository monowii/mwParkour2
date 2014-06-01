package fr.monowii.parkour2;

import fr.monowii.parkour2.level.CheckpointInfo;
import fr.monowii.parkour2.level.Level;
import fr.monowii.parkour2.managers.MessagesManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlayersCommands implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player p = null;
        if (sender instanceof Player) p = (Player) sender;

        if (p == null) {
            sender.sendMessage("§cThis command is only usable by a player !");
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("parkour"))
        {
            if (args.length == 0)
            {
                p.sendMessage("§6---------=[ §8mwParkour2 v" + Parkour2.getPlugin().getDescription().getVersion()+ " by monowii §6]=---------");

                if (p.hasPermission("mwparkour2.leveleditor") || p.hasPermission("mwparkour2.admin"))
                {
                    p.sendMessage("§a/"+label+" new <levelName> <authors>§f  - Create a new level with your location as spawn");
                    p.sendMessage("§a/"+label+" add <levelId>§f  - Add a checkpoint to the level");
                    p.sendMessage("§a/"+label+" removeLast <levelId>§f  - Remove the last checkpoint level");
                    p.sendMessage("§a/"+label+" setName <levelId> <levelName>§f  - Set the level name");
                    p.sendMessage("§a/"+label+" setAuthors <levelId> <authors>§f  - Set the authors");
                    p.sendMessage("§a/"+label+" setSpawn <levelId>§f  - Set the level spawn at your location");
                    p.sendMessage("§a/"+label+" setOption <levelId> <§2w§aater§2R§aespawn/§2l§aava§2R§aespawn/§2v§aoid§2R§aespawn/§2r§aespawn§2A§at§2C§aheckpoint> <§2t§arue/§2f§aalse>§f  - Set a level option");
                }
                if (p.hasPermission("mwparkour2.admin"))
                {
                    p.sendMessage("§2/"+label+" delete <levelId>§f  - Delete a level");
                    p.sendMessage("§2/"+label+" active <levelId>§f  - Active ON/OFF a level");
                    p.sendMessage("§2/"+label+" checkpointInfo§f  - Get checkpoint info where you are standing");
                }

                p.sendMessage("/§7"+label+" list [page]§f  - Show all the levels");
                p.sendMessage("/§7"+label+" info <levelId>§f - Display parkour info");
                p.sendMessage("/§7"+label+" best <levelId>§f - Show leaderboard");
                p.sendMessage("/§7"+label+" join <levelId>§f - Join a level");
                p.sendMessage("/§7"+label+" leave§f - Leave the level");

                return true;
            }

            if (args[0].equalsIgnoreCase("new") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 3)
                {
                    String levelName = args[1];
                    String authors = args[2];

                    if (levelName.length() > 25) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorLevelNameTooLong);
                        return false;
                    }
                    if (authors.length() > 25) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorAuthorsTooLong);
                        return false;
                    }

                    int levelId = Parkour2.getLevelsManager().createLevel(p.getLocation(), levelName, authors);
                    p.sendMessage(MessagesManager.prefix+MessagesManager.levelCreated.replace("%levelId", ""+levelId).replace("%levelName", levelName));
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("add") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));

                        Block targetBlock = p.getTargetBlock(null, 5);
                        if (targetBlock != null && targetBlock.getType() == Material.STONE_PLATE)
                        {
                            if (!Parkour2.getLevelsManager().isCheckpoint(targetBlock.getLocation())) {
                                level.addCheckpoint(targetBlock.getLocation());
                                p.sendMessage(MessagesManager.prefix+MessagesManager.checkpointAdded.replace("%levelName", level.getName()));
                            }
                            else
                                p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorCheckpointUsedByLevel);
                        }
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("removeLast") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));

                        if (level.removeLastCheckpoint())
                        {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.lastCheckpointRemoved);

                            if (level.getCheckpoints().size() < 2 && level.isActive())
                            {
                                level.setActive(false);
                                p.sendMessage(MessagesManager.prefix + MessagesManager.levelActive.replace("%levelId", "" + level.getId()).replace("%activeState", "false"));
                            }
                        }
                        else {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorRemoveLastCheckpoint);
                        }
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setName") && p.hasPermission("mwparkour2.leveleditor"))
            {
                System.out.println("setNayyyme !");

                if (args.length == 3 && Utils.isNumeric(args[1]))
                {
                    System.out.println("setNayyyme !");
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])));
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));
                        String newLevelName = args[2];

                        if (newLevelName.length() > 25) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorLevelNameTooLong);
                            return false;
                        }

                        level.setName(newLevelName);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelSetName);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setAuthors") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 3 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));
                        String newAuthors = args[2];

                        if (newAuthors.length() > 25) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorAuthorsTooLong);
                            return false;
                        }

                        level.setAuthors(newAuthors);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelSetAuthors);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setSpawn") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));

                        level.setSpawn(p.getLocation());
                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelSetSpawn);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setOption") && p.hasPermission("mwparkour2.leveleditor"))
            {
                if (args.length == 4 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));
                        if (!(args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")))
                            return false;

                        boolean state = args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t") ? true : false;

                        if (args[2].equalsIgnoreCase("waterRespawn") || args[2].equalsIgnoreCase("wr"))
                        {
                            level.getOptions().setWaterRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("lavaRespawn") || args[2].equalsIgnoreCase("lr"))
                        {
                            level.getOptions().setLavaRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("voidRespawn") || args[2].equalsIgnoreCase("vr"))
                        {
                            level.getOptions().setVoidRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("respawnAtCheckpoint") || args[2].equalsIgnoreCase("rac"))
                        {
                            level.getOptions().setRespawnAtCheckpoint(state);
                        }
                        else
                        {
                            return false;
                        }

                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelSetOption);

                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("delete") && p.hasPermission("mwparkour2.admin"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    int levelId = Integer.valueOf(args[1]);

                    if (Parkour2.getLevelsManager().containsLevel(levelId))
                    {
                        Parkour2.getLevelsManager().deleteDelete(levelId);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelDeleted.replace("%levelId", ""+levelId));
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("active") && p.hasPermission("mwparkour2.admin"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(args[1])))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(args[1]));

                        if (level.getCheckpoints().size() < 2)
                        {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorNotEnoughCheckpointsToActive);
                            return false;
                        }

                        boolean newState = !level.isActive();
                        level.setActive(newState);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.levelActive.replace("%levelId", ""+level.getId()).replace("%activeState", ""+newState));
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("checkpointInfo") && p.hasPermission("mwparkour2.admin"))
            {
                if (Parkour2.getLevelsManager().isCheckpoint(p.getLocation()))
                {
                    CheckpointInfo ci = Parkour2.getLevelsManager().getCheckpoint(p.getLocation());
                    p.sendMessage("§aCheckpoint "+ci.getCheckpoint()+" ("+ci.getCheckpointType().name()+")  from level "+ci.getLevelId());
                }
                else {
                    p.sendMessage("§cYou are not standing on a parkour checkpoint !");
                }
            }
            else if (args[0].equalsIgnoreCase("list"))
            {
                int levelsToDisplay = 10;

                int maxPages = (int) Math.ceil((Parkour2.getLevelsManager().getLevels().size()-1)/levelsToDisplay);
                int page = 0;
                if (args.length == 2 && Utils.isNumeric(args[1]))
                    page = Integer.valueOf(args[1]);
                if (page < 0)
                    page = 0;
                if (page > maxPages)
                    page = maxPages;

                p.sendMessage("§2---[ LevelsList (page "+page+"/"+maxPages+") ]---");

                int i = 0;
                int j = 0;
                for (Map.Entry<Integer, Level> levels : Parkour2.getLevelsManager().getLevels().entrySet())
                {
                    if (i >= levelsToDisplay*page && i <= (levelsToDisplay*page)+10)
                    {
                        Level level = levels.getValue();

                        p.sendMessage((level.isActive() ? "§a" : "§4") + level.getId()+" §f| §b"+level.getName()+" §3by "+level.getAuthors()+" §7("+level.getCheckpoints().size()+"checkpoints)");

                        j++;
                        if (j >= levelsToDisplay)
                            break;
                    }
                    i++;
                }

                return true;
            }
            else if (args[0].equalsIgnoreCase("best"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    int levelId = Integer.valueOf(args[1]);

                    if (Parkour2.getLevelsManager().containsLevel(levelId))
                    {
                        p.sendMessage("-----=[ Best times in "+ Parkour2.getLevelsManager().getLevel(levelId).getName()+" by "+ Parkour2.getLevelsManager().getLevel(levelId).getAuthors()+" ]=-----");
                        int rank = 0;
                        for (Map.Entry<String, Long> entry : Parkour2.getTimesManager().getTimes(levelId, 0).entrySet()) {
                            rank++;
                            p.sendMessage(rank + " §b| " + entry.getKey() + " - " + Utils.convertTime(entry.getValue()));
                        }

                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("join") && p.hasPermission("mwparkour2.join"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    int levelId = Integer.valueOf(args[1]);

                    if (Parkour2.getLevelsManager().containsLevel(levelId))
                    {
                        Level level = Parkour2.getLevelsManager().getLevel(levelId);


                        level.getSpawn().getChunk().load();

                        p.teleport(level.getSpawn());
                        p.sendMessage(MessagesManager.prefix + MessagesManager.playerJoinLevel.replace("%levelName", level.getName()).replace("%levelId", ""+level.getId()));
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("leave"))
            {
                if (Parkour2.getPlayersManager().containsPlayer(p))
                {
                    String levelName = Parkour2.getLevelsManager().getLevel( Parkour2.getPlayersManager().getPlayer(p).getLevelId() ).getName();
                    p.sendMessage(MessagesManager.prefix+MessagesManager.playerLeaveLevel.replace("%levelName", levelName));
                    Parkour2.getPlayersManager().removePlayer(p);
                }
            }
        }

        return false;
    }
}

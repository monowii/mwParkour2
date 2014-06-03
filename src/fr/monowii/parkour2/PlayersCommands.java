package fr.monowii.parkour2;

import fr.monowii.parkour2.parkour.CheckpointInfo;
import fr.monowii.parkour2.parkour.Parkour;
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

                if (p.hasPermission("mwparkour2.parkoureditor") || p.hasPermission("mwparkour2.admin"))
                {
                    p.sendMessage("§a/"+label+" new <parkourName> <authors>§f  - Create a new parkour (with your location as spawn)");
                    p.sendMessage("§a/"+label+" add <parkourId>§f  - Add a checkpoint to a parkour");
                    p.sendMessage("§a/"+label+" removeLast <parkourId>§f  - Remove the last checkpoint of a parkour");
                    p.sendMessage("§a/"+label+" setName <parkourId> <parkourName>§f  - Set the parkour name");
                    p.sendMessage("§a/"+label+" setAuthors <parkourId> <authors>§f  - Set the authors");
                    p.sendMessage("§a/"+label+" setSpawn <parkourId>§f  - Set the parkour spawn at your location");
                    p.sendMessage("§a/"+label+" setOption <parkourId> <§2w§aater§2R§aespawn/§2l§aava§2R§aespawn/§2v§aoid§2R§aespawn/§2r§aespawn§2A§at§2C§aheckpoint> <§2t§arue/§2f§aalse>§f  - Set a parkour option");
                }
                if (p.hasPermission("mwparkour2.admin"))
                {
                    p.sendMessage("§2/"+label+" delete <parkourId>§f  - Delete a parkour");
                    p.sendMessage("§2/"+label+" active <parkourId>§f  - Active ON/OFF a parkour");
                    p.sendMessage("§2/"+label+" checkpointInfo§f  - Get checkpoint info where you are standing");
                }

                p.sendMessage("/§7"+label+" list [page]§f  - Show all parkours");
                p.sendMessage("/§7"+label+" info <parkourId>§f - Display a parkour infos");
                p.sendMessage("/§7"+label+" best <parkourId>§f - Show leaderboard");
                p.sendMessage("/§7"+label+" join <parkourId>§f - Join a parkour");
                p.sendMessage("/§7"+label+" leave§f - Leave the parkour");

                return true;
            }

            if (args[0].equalsIgnoreCase("new") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 3)
                {
                    String parkourName = args[1];
                    String authors = args[2];

                    if (parkourName.length() > 25) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorParkourNameTooLong);
                        return false;
                    }
                    if (authors.length() > 25) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorAuthorsTooLong);
                        return false;
                    }

                    int parkourId = Parkour2.getParkoursManager().createParkour(p.getLocation(), parkourName, authors);
                    p.sendMessage(MessagesManager.prefix+MessagesManager.parkourCreated.replace("%parkourId", ""+parkourId).replace("%parkourName", parkourName));
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("add") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));

                        Block targetBlock = p.getTargetBlock(null, 5);
                        if (targetBlock != null && targetBlock.getType() == Material.STONE_PLATE)
                        {
                            if (!Parkour2.getParkoursManager().isCheckpoint(targetBlock.getLocation())) {
                                parkour.addCheckpoint(targetBlock.getLocation());
                                p.sendMessage(MessagesManager.prefix+MessagesManager.checkpointAdded.replace("%parkourName", parkour.getName()));
                            }
                            else
                                p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorCheckpointUsedByParkour);
                        }
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("removeLast") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));

                        if (parkour.removeLastCheckpoint())
                        {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.lastCheckpointRemoved);

                            if (parkour.getCheckpoints().size() < 2 && parkour.isActive())
                            {
                                parkour.setActive(false);
                                p.sendMessage(MessagesManager.prefix + MessagesManager.parkourActive.replace("%parkourId", "" + parkour.getId()).replace("%activeState", "false"));
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
            else if (args[0].equalsIgnoreCase("setName") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 3 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])));
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));
                        String newParkourName = args[2];

                        if (newParkourName.length() > 25) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorParkourNameTooLong);
                            return false;
                        }

                        parkour.setName(newParkourName);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourSetName);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setAuthors") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 3 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));
                        String newAuthors = args[2];

                        if (newAuthors.length() > 25) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorAuthorsTooLong);
                            return false;
                        }

                        parkour.setAuthors(newAuthors);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourSetAuthors);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setSpawn") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));

                        parkour.setSpawn(p.getLocation());
                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourSetSpawn);
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("setOption") && p.hasPermission("mwparkour2.parkoureditor"))
            {
                if (args.length == 4 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));
                        if (!(args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")))
                            return false;

                        boolean state = args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t") ? true : false;

                        if (args[2].equalsIgnoreCase("waterRespawn") || args[2].equalsIgnoreCase("wr"))
                        {
                            parkour.getOptions().setWaterRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("lavaRespawn") || args[2].equalsIgnoreCase("lr"))
                        {
                            parkour.getOptions().setLavaRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("voidRespawn") || args[2].equalsIgnoreCase("vr"))
                        {
                            parkour.getOptions().setVoidRespawn(state);
                        }
                        else if (args[2].equalsIgnoreCase("respawnAtCheckpoint") || args[2].equalsIgnoreCase("rac"))
                        {
                            parkour.getOptions().setRespawnAtCheckpoint(state);
                        }
                        else
                        {
                            return false;
                        }

                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourSetOption);

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
                    int parkourId = Integer.valueOf(args[1]);

                    if (Parkour2.getParkoursManager().containsParkour(parkourId))
                    {
                        Parkour2.getParkoursManager().deleteDelete(parkourId);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourDeleted.replace("%parkourId", ""+parkourId));
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("active") && p.hasPermission("mwparkour2.admin"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(args[1])))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(args[1]));

                        if (parkour.getCheckpoints().size() < 2)
                        {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorNotEnoughCheckpointsToActive);
                            return false;
                        }

                        boolean newState = !parkour.isActive();
                        parkour.setActive(newState);
                        p.sendMessage(MessagesManager.prefix+MessagesManager.parkourActive.replace("%parkourId", ""+ parkour.getId()).replace("%activeState", ""+newState));
                        return true;
                    }
                } else {
                    p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorArgs);
                }
            }
            else if (args[0].equalsIgnoreCase("checkpointInfo") && p.hasPermission("mwparkour2.admin"))
            {
                if (Parkour2.getParkoursManager().isCheckpoint(p.getLocation()))
                {
                    CheckpointInfo ci = Parkour2.getParkoursManager().getCheckpoint(p.getLocation());
                    p.sendMessage("§aCheckpoint "+ci.getCheckpoint()+" ("+ci.getCheckpointType().name()+")  from parkour "+ci.getParkourId());
                }
                else {
                    p.sendMessage("§cYou are not standing on a parkour checkpoint !");
                }
            }
            else if (args[0].equalsIgnoreCase("list") && p.hasPermission("mwparkour2.list"))
            {
                int parkoursToDisplay = 10;

                int maxPages = (int) Math.ceil((Parkour2.getParkoursManager().getParkours().size()-1)/parkoursToDisplay);
                int page = 0;
                if (args.length == 2 && Utils.isNumeric(args[1]))
                    page = Integer.valueOf(args[1]);
                if (page < 0)
                    page = 0;
                if (page > maxPages)
                    page = maxPages;

                p.sendMessage("§2---[ Parkours (page "+page+"/"+maxPages+") ]---");

                int i = 0;
                int j = 0;
                for (Map.Entry<Integer, Parkour> parkours : Parkour2.getParkoursManager().getParkours().entrySet())
                {
                    if (i >= parkoursToDisplay*page && i <= (parkoursToDisplay*page)+10)
                    {
                        Parkour parkour = parkours.getValue();

                        p.sendMessage((parkour.isActive() ? "§a" : "§4") + parkour.getId()+" §f| §b"+ parkour.getName()+" §3by "+ parkour.getAuthors()+" §7("+ parkour.getCheckpoints().size()+"checkpoints)");

                        j++;
                        if (j >= parkoursToDisplay)
                            break;
                    }
                    i++;
                }

                return true;
            }
            else if (args[0].equalsIgnoreCase("best") && p.hasPermission("mwparkour2.best"))
            {
                if (args.length == 2 && Utils.isNumeric(args[1]))
                {
                    int parkourId = Integer.valueOf(args[1]);

                    if (Parkour2.getParkoursManager().containsParkour(parkourId))
                    {
                        p.sendMessage("-----=[ Best times in "+ Parkour2.getParkoursManager().getParkour(parkourId).getName()+" by "+ Parkour2.getParkoursManager().getParkour(parkourId).getAuthors()+" ]=-----");
                        int rank = 0;
                        for (Map.Entry<String, Long> entry : Parkour2.getTimesManager().getTimes(parkourId, 0).entrySet()) {
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
                    int parkourId = Integer.valueOf(args[1]);

                    if (Parkour2.getParkoursManager().containsParkour(parkourId))
                    {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(parkourId);


                        parkour.getSpawn().getChunk().load();

                        p.teleport(parkour.getSpawn());
                        p.sendMessage(MessagesManager.prefix + MessagesManager.playerJoinParkour.replace("%parkourName", parkour.getName()).replace("%parkourId", ""+ parkour.getId()));
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
                    String parkourName = Parkour2.getParkoursManager().getParkour(Parkour2.getPlayersManager().getPlayer(p).getParkourId()).getName();
                    p.sendMessage(MessagesManager.prefix+MessagesManager.playerLeaveParkour.replace("%parkourName", parkourName));
                    Parkour2.getPlayersManager().removePlayer(p);
                }
            }
        }

        return false;
    }
}

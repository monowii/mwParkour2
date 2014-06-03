package fr.monowii.parkour2;

import fr.monowii.parkour2.events.*;
import fr.monowii.parkour2.parkour.CheckpointInfo;
import fr.monowii.parkour2.parkour.Parkour;
import fr.monowii.parkour2.managers.MessagesManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class PlayersListeners implements Listener
{
    private boolean mParkourStart = true;
    private boolean mParkourRestart = true;
    private boolean mParkourCheckpoint = true;
    private boolean mParkourFinish = true;
    private boolean mParkourJoin = true;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (Parkour2.getPlayersManager().containsPlayer(e.getPlayer()))
            Parkour2.getPlayersManager().removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player)
        {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
            {
                Player p = (Player) e.getEntity();

                if (Parkour2.getPlayersManager().containsPlayer(p))
                {
                    Parkour parkour = Parkour2.getParkoursManager().getParkour(Parkour2.getPlayersManager().getPlayer(p).getParkourId());

                    if (parkour.getOptions().isVoidRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(p, parkour, ParkourDeathCause.VOID);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        e.setDamage(0);
                        Parkour2.getPlayersManager().teleportToLastCheckpoint(p, parkour);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
        {
            if (to.getBlock().getType() == Material.WATER || to.getBlock().getType() == Material.STATIONARY_WATER)
            {
                if (Parkour2.getPlayersManager().containsPlayer(e.getPlayer()))
                {
                    Parkour parkour = Parkour2.getParkoursManager().getParkour(Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getParkourId());

                    if (parkour.getOptions().isWaterRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(e.getPlayer(), parkour, ParkourDeathCause.WATER);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        Parkour2.getPlayersManager().teleportToLastCheckpoint(e.getPlayer(), parkour);
                    }
                }
            }
            else if (to.getBlock().getType() == Material.LAVA || to.getBlock().getType() == Material.STATIONARY_LAVA)
            {
                if (Parkour2.getPlayersManager().containsPlayer(e.getPlayer()))
                {
                    Parkour parkour = Parkour2.getParkoursManager().getParkour(Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getParkourId());

                    if (parkour.getOptions().isLavaRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(e.getPlayer(), parkour, ParkourDeathCause.LAVA);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        Parkour2.getPlayersManager().teleportToLastCheckpoint(e.getPlayer(), parkour);
                    }
                }
            }

            if (e.getTo().getBlock().getType() == Material.STONE_PLATE)
            {
                if (Parkour2.getParkoursManager().isCheckpoint(to))
                {
                    Player p = e.getPlayer();
                    CheckpointInfo checkpointInfo = Parkour2.getParkoursManager().getCheckpoint(to);
                    Parkour parkour = Parkour2.getParkoursManager().getParkour(checkpointInfo.getParkourId());

                    if (!parkour.isActive()) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorParkourNotActive);
                        return;
                    }

                    if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.START)
                    {
                        if (preparePlayer(p, parkour))
                            return;

                        //If the player restart the parkour
                        if (Parkour2.getPlayersManager().containsPlayer(p) && parkour.getId() == Parkour2.getPlayersManager().getPlayer(p).getParkourId())
                        {
                            PlayerParkourRestartEvent ppre = new PlayerParkourRestartEvent(p, parkour);
                            Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppre);
                            if (ppre.isCancelled())
                                return;

                            Parkour2.getPlayersManager().getPlayer(p).resetStartTime();

                            if (mParkourRestart)
                                p.sendMessage(MessagesManager.prefix+MessagesManager.playerRestartParkour.replace("%parkourName", parkour.getName()).replace("%authors", parkour.getAuthors()));

                            Parkour2.getPlayersManager().getPlayer(p).setLastCheckpoint(0);
                        }
                        //If the player start another parkour
                        else
                        {
                            PlayerParkourStartEvent ppse = new PlayerParkourStartEvent(p, parkour);
                            Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppse);
                            if (ppse.isCancelled())
                                return;

                            if (Parkour2.getPlayersManager().containsPlayer(p))
                                Parkour2.getPlayersManager().removePlayer(p);

                            Parkour2.getPlayersManager().addPlayer(p, parkour.getId());

                            if (mParkourStart)
                                p.sendMessage(MessagesManager.prefix+MessagesManager.playerStartParkour.replace("%parkourName", parkour.getName()).replace("%authors", parkour.getAuthors()));
                        }
                    }
                    else if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.CHECKPOINT)
                    {
                        if (!Parkour2.getPlayersManager().containsPlayer(p))
                            return;

                        if (preparePlayer(p, parkour))
                            return;

                        if (parkour.getId() != Parkour2.getPlayersManager().getPlayer(p).getParkourId()) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerBadParkour);
                            return;
                        }

                        if (Parkour2.getPlayersManager().getPlayer(p).getLastCheckpoint() >= checkpointInfo.getCheckpoint()) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerAlreadyPassedCheckpoint);
                            return;
                        }

                        if (Parkour2.getPlayersManager().getPlayer(p).getLastCheckpoint() != checkpointInfo.getCheckpoint()-1) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerForgotLastCheckpoint);
                            return;
                        }

                        PlayerParkourCheckpointEvent ppce = new PlayerParkourCheckpointEvent(p, parkour, checkpointInfo);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppce);
                        if (ppce.isCancelled())
                            return;

                        Parkour2.getPlayersManager().getPlayer(p).setLastCheckpoint(checkpointInfo.getCheckpoint());

                        if (mParkourCheckpoint)
                            p.sendMessage(MessagesManager.prefix+MessagesManager.playerCheckpointParkour.replace("%checkpoint", ""+checkpointInfo.getCheckpoint()).replace("%totalCheckpoints", ""+(parkour.getCheckpoints().size()-2)));


                    }
                    else if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.END)
                    {
                        if (!Parkour2.getPlayersManager().containsPlayer(p))
                            return;

                        if (preparePlayer(p, parkour))
                            return;

                        if (parkour.getId() != Parkour2.getPlayersManager().getPlayer(p).getParkourId()) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerBadParkour);
                            return;
                        }

                        if (Parkour2.getPlayersManager().getPlayer(p).getLastCheckpoint() != checkpointInfo.getCheckpoint()-1) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerForgotLastCheckpoint);
                            return;
                        }

                        long time = System.currentTimeMillis() - Parkour2.getPlayersManager().getPlayer(p).getStartTime();
                        long oldTime = Parkour2.getTimesManager().getPlayerTime(parkour.getId(), p);

                        PlayerParkourFinishEvent ppfe = new PlayerParkourFinishEvent(p, parkour, time);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppfe);
                        if (ppfe.isCancelled())
                            return;

                        if (!Parkour2.getTimesManager().hasScore(parkour.getId(), p) || time < oldTime) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.playerBestScore);
                            Parkour2.getTimesManager().addPlayerTime(parkour.getId(), p, time);
                        }

                        if (mParkourFinish)
                            p.sendMessage(MessagesManager.prefix + MessagesManager.playerEndParkour.replace("%parkourName", parkour.getName()).replace("%time", Utils.convertTime(time)));

                        Parkour2.getPlayersManager().removePlayer(p);


                    }
                }
            }
        }
    }

    public boolean preparePlayer(Player p, Parkour parkour) {
        if (p.getGameMode() == GameMode.CREATIVE) {
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(parkour.getSpawn());
            p.getInventory().clear();
            p.getEquipment().clear();
            p.getActivePotionEffects().clear();
            return true;
        }
        return false;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).equals("[pk2]") || e.getLine(0).equals("§e[mwParkour2]"))
        {
            if (e.getPlayer().hasPermission("mwparkour2.sign") || e.getPlayer().hasPermission("mwparkour2.admin"))
            {
                String line = e.getLine(1);

                if (line.equalsIgnoreCase("info") && Utils.isNumeric(e.getLine(2)))
                {
                    int parkourId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getParkoursManager().containsParkour(parkourId)) {
                        Parkour parkour = Parkour2.getParkoursManager().getParkour(parkourId);
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, parkour.getName());
                        e.setLine(2, "(parkour"+parkourId+") by");
                        e.setLine(3, parkour.getAuthors());
                    }

                }
                else if (line.equalsIgnoreCase("join") && Utils.isNumeric(e.getLine(2))) {
                    int parkourId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getParkoursManager().containsParkour(parkourId)) {
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, "§fJoin");
                        e.setLine(2, Parkour2.getParkoursManager().getParkour(parkourId).getName());
                        e.setLine(3, "(parkour " + parkourId + ")");
                    }
                }
                else if (line.equalsIgnoreCase("best") && Utils.isNumeric(e.getLine(2))) {
                    int parkourId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getParkoursManager().containsParkour(parkourId)) {
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, "§aBest");
                        e.setLine(2, Parkour2.getParkoursManager().getParkour(parkourId).getName());
                        e.setLine(3, "(parkour " + parkourId + ")");
                    }
                }
            }
            else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (e.getClickedBlock().getState() instanceof Sign)
            {
                Sign s = (Sign) e.getClickedBlock().getState();

                if (s.getLine(0).equals("§e[mwParkour2]"))
                {
                    if (s.getLine(1).equals("§fJoin"))
                    {
                        String parkourId = s.getLine(3).split(" ")[1];
                        parkourId = parkourId.substring(0, parkourId.length()-1);

                        if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(parkourId)))
                        {
                            Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(parkourId));

                            e.getPlayer().teleport(parkour.getSpawn());

                            if (mParkourJoin)
                                e.getPlayer().sendMessage(MessagesManager.prefix + MessagesManager.playerJoinParkour.replace("%parkourName", parkour.getName()).replace("%parkourId", ""+ parkour.getId()));
                        }
                    }
                    else if (s.getLine(1).equals("§aBest"))
                    {
                        Player p = e.getPlayer();
                        String parkourId = s.getLine(3).split(" ")[1];
                        parkourId = parkourId.substring(0, parkourId.length()-1);

                        if (Parkour2.getParkoursManager().containsParkour(Integer.valueOf(parkourId)))
                        {
                            Parkour parkour = Parkour2.getParkoursManager().getParkour(Integer.valueOf(parkourId));

                            p.sendMessage("-----=[ Best times in "+ parkour.getName()+" by "+ parkour.getAuthors()+" ]=-----");
                            int rank = 0;
                            for (Map.Entry<String, Long> entry : Parkour2.getTimesManager().getTimes(parkour.getId(), 0).entrySet()) {
                                rank++;
                                p.sendMessage(rank + " §b| " + entry.getKey() + " - " + Utils.convertTime(entry.getValue()));
                            }
                        }
                    }
                }
            }
        }

        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            if (Parkour2.getPlayersManager().containsPlayer(e.getPlayer()) && e.getPlayer().getItemInHand().getType() == Material.STICK)
            {
                int parkourId = Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getParkourId();

                e.getPlayer().teleport(Parkour2.getParkoursManager().getParkour(parkourId).getSpawn());
                e.setCancelled(true);
            }
        }
    }
}

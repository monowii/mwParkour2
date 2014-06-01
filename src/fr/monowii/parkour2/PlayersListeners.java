package fr.monowii.parkour2;

import fr.monowii.parkour2.events.*;
import fr.monowii.parkour2.level.CheckpointInfo;
import fr.monowii.parkour2.level.Level;
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
                    Level level = Parkour2.getLevelsManager().getLevel(Parkour2.getPlayersManager().getPlayer(p).getLevelId());

                    if (level.getOptions().isVoidRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(p, level, ParkourDeathCause.VOID);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        e.setDamage(0);
                        Parkour2.getPlayersManager().teleportToLastCheckpoint(p, level);
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
                    Level level = Parkour2.getLevelsManager().getLevel(Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getLevelId());

                    if (level.getOptions().isWaterRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(e.getPlayer(), level, ParkourDeathCause.WATER);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        Parkour2.getPlayersManager().teleportToLastCheckpoint(e.getPlayer(), level);
                    }
                }
            }
            else if (to.getBlock().getType() == Material.LAVA || to.getBlock().getType() == Material.STATIONARY_LAVA)
            {
                if (Parkour2.getPlayersManager().containsPlayer(e.getPlayer()))
                {
                    Level level = Parkour2.getLevelsManager().getLevel(Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getLevelId());

                    if (level.getOptions().isLavaRespawn())
                    {
                        PlayerParkourDeathEvent ppde = new PlayerParkourDeathEvent(e.getPlayer(), level, ParkourDeathCause.LAVA);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppde);
                        if (ppde.isCancelled())
                            return;

                        Parkour2.getPlayersManager().teleportToLastCheckpoint(e.getPlayer(), level);
                    }
                }
            }

            if (e.getTo().getBlock().getType() == Material.STONE_PLATE)
            {
                if (Parkour2.getLevelsManager().isCheckpoint(to))
                {
                    Player p = e.getPlayer();
                    CheckpointInfo checkpointInfo = Parkour2.getLevelsManager().getCheckpoint(to);
                    Level level = Parkour2.getLevelsManager().getLevel(checkpointInfo.getLevelId());

                    if (!level.isActive()) {
                        p.sendMessage(MessagesManager.prefix+MessagesManager.ErrorLevelNotActive);
                        return;
                    }

                    if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.START)
                    {
                        if (preparePlayer(p, level))
                            return;

                        //If the player restart the level
                        if (Parkour2.getPlayersManager().containsPlayer(p) && level.getId() == Parkour2.getPlayersManager().getPlayer(p).getLevelId())
                        {
                            PlayerParkourRestartEvent ppre = new PlayerParkourRestartEvent(p, level);
                            Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppre);
                            if (ppre.isCancelled())
                                return;

                            Parkour2.getPlayersManager().getPlayer(p).resetStartTime();

                            if (mParkourRestart)
                                p.sendMessage(MessagesManager.prefix+MessagesManager.playerRestartLevel.replace("%levelName", level.getName()).replace("%authors", level.getAuthors()));

                            Parkour2.getPlayersManager().getPlayer(p).setLastCheckpoint(0);
                        }
                        //If the player start another level
                        else
                        {
                            PlayerParkourStartEvent ppse = new PlayerParkourStartEvent(p, level);
                            Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppse);
                            if (ppse.isCancelled())
                                return;

                            if (Parkour2.getPlayersManager().containsPlayer(p))
                                Parkour2.getPlayersManager().removePlayer(p);

                            Parkour2.getPlayersManager().addPlayer(p, level.getId());

                            if (mParkourStart)
                                p.sendMessage(MessagesManager.prefix+MessagesManager.playerStartLevel.replace("%levelName", level.getName()).replace("%authors", level.getAuthors()));
                        }
                    }
                    else if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.CHECKPOINT)
                    {
                        if (!Parkour2.getPlayersManager().containsPlayer(p))
                            return;

                        if (preparePlayer(p, level))
                            return;

                        if (level.getId() != Parkour2.getPlayersManager().getPlayer(p).getLevelId()) {
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

                        PlayerParkourCheckpointEvent ppce = new PlayerParkourCheckpointEvent(p, level, checkpointInfo);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppce);
                        if (ppce.isCancelled())
                            return;

                        Parkour2.getPlayersManager().getPlayer(p).setLastCheckpoint(checkpointInfo.getCheckpoint());

                        if (mParkourCheckpoint)
                            p.sendMessage(MessagesManager.prefix+MessagesManager.playerCheckpointLevel.replace("%checkpoint", ""+checkpointInfo.getCheckpoint()).replace("%totalCheckpoints", ""+(level.getCheckpoints().size()-2)));


                    }
                    else if (checkpointInfo.getCheckpointType() == CheckpointInfo.CheckpointType.END)
                    {
                        if (!Parkour2.getPlayersManager().containsPlayer(p))
                            return;

                        if (preparePlayer(p, level))
                            return;

                        if (level.getId() != Parkour2.getPlayersManager().getPlayer(p).getLevelId()) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerBadParkour);
                            return;
                        }

                        if (Parkour2.getPlayersManager().getPlayer(p).getLastCheckpoint() != checkpointInfo.getCheckpoint()-1) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.PlayerForgotLastCheckpoint);
                            return;
                        }

                        long time = System.currentTimeMillis() - Parkour2.getPlayersManager().getPlayer(p).getStartTime();
                        long oldTime = Parkour2.getTimesManager().getPlayerTime(level.getId(), p);

                        PlayerParkourFinishEvent ppfe = new PlayerParkourFinishEvent(p, level, time);
                        Parkour2.getPlugin().getServer().getPluginManager().callEvent(ppfe);
                        if (ppfe.isCancelled())
                            return;

                        if (!Parkour2.getTimesManager().hasScore(level.getId(), p) || time < oldTime) {
                            p.sendMessage(MessagesManager.prefix+MessagesManager.playerBestScore);
                            Parkour2.getTimesManager().addPlayerTime(level.getId(), p, time);
                        }

                        if (mParkourFinish)
                            p.sendMessage(MessagesManager.prefix + MessagesManager.playerEndLevel.replace("%levelName", level.getName()).replace("%time", Utils.convertTime(time)));

                        Parkour2.getPlayersManager().removePlayer(p);


                    }
                }
            }
        }
    }

    public boolean preparePlayer(Player p, Level level) {
        if (p.getGameMode() == GameMode.CREATIVE) {
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(level.getSpawn());
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

                if (line.equalsIgnoreCase("info") && Utils.isNumeric(e.getLine(2))) {
                    int levelId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getLevelsManager().containsLevel(levelId)) {
                        Level level = Parkour2.getLevelsManager().getLevel(levelId);
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, level.getName());
                        e.setLine(2, "(level"+levelId+") by");
                        e.setLine(3, level.getAuthors());
                    }

                }
                else if (line.equalsIgnoreCase("join") && Utils.isNumeric(e.getLine(2))) {
                    int levelId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getLevelsManager().containsLevel(levelId)) {
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, "§fJoin");
                        e.setLine(2, Parkour2.getLevelsManager().getLevel(levelId).getName());
                        e.setLine(3, "(level " + levelId + ")");
                    }
                }
                else if (line.equalsIgnoreCase("best") && Utils.isNumeric(e.getLine(2))) {
                    int levelId = Integer.valueOf(e.getLine(2));

                    if (Parkour2.getLevelsManager().containsLevel(levelId)) {
                        e.setLine(0, "§e[mwParkour2]");
                        e.setLine(1, "§aBest");
                        e.setLine(2, Parkour2.getLevelsManager().getLevel(levelId).getName());
                        e.setLine(3, "(level " + levelId + ")");
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
                        String levelId = s.getLine(3).split(" ")[1];
                        levelId = levelId.substring(0, levelId.length()-1);

                        if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(levelId)))
                        {
                            Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(levelId));

                            e.getPlayer().teleport(level.getSpawn());

                            if (mParkourJoin)
                                e.getPlayer().sendMessage(MessagesManager.prefix + MessagesManager.playerJoinLevel.replace("%levelName", level.getName()).replace("%levelId", ""+level.getId()));
                        }
                    }
                    else if (s.getLine(1).equals("§aBest"))
                    {
                        Player p = e.getPlayer();
                        String levelId = s.getLine(3).split(" ")[1];
                        levelId = levelId.substring(0, levelId.length()-1);

                        if (Parkour2.getLevelsManager().containsLevel(Integer.valueOf(levelId)))
                        {
                            Level level = Parkour2.getLevelsManager().getLevel(Integer.valueOf(levelId));

                            p.sendMessage("-----=[ Best times in "+ level.getName()+" by "+ level.getAuthors()+" ]=-----");
                            int rank = 0;
                            for (Map.Entry<String, Long> entry : Parkour2.getTimesManager().getTimes(level.getId(), 0).entrySet()) {
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
                int levelId = Parkour2.getPlayersManager().getPlayer(e.getPlayer()).getLevelId();
                e.getPlayer().teleport(Parkour2.getLevelsManager().getLevel(levelId).getSpawn());
                e.setCancelled(true);
            }
        }
    }
}

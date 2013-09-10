package monowii.mwParkour2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

public class Parkour extends JavaPlugin implements Listener
{

	/* Permissions
	 * 
	 * Use parkour plate : parkour.use
	 * 
	 * cp new : parkour.mapeditor
	 * cp setMapCreator : parkour.mapeditor
	 * cp setMapName : parkour.mapeditor
	 * cp remove : parkour.mapeditor
	 * cp setspawn : parkour.mapeditor
	 * 
	 * cp pRemove / resetScores : parkour.admin
	 * And ALL CP COMMANDS : parkour.admin
	 */

	/* 1.5.5 Changelog
	 * Better performance onPlayerInteract
	 * new Special signs: joinLastMap / lastBestScores
	 * better teleportation with lava / water
	 */

	/* TODO
	 * 
	 */

	static Economy economy = null;

	//Used for parkour creation
	ArrayList<Location> newMapCheckpoints = new ArrayList<Location>();
	boolean newMap = false;
	String newMapPlayerEditor = "";
	int CheckpointNumber = 0;
	int NewMapNumber = 0;
	String newMapName = null;
	String newMapCreator = null;

	//Options
	boolean removePotionsEffectsOnParkour = false;
	boolean BroadcastMessage = false;
	String BroadcastMsg = "&emwParkour2&f>&8 New record for &7PLAYER &8on map MAPNAME !";
	boolean CheckpointEffect = true;
	boolean InvincibleWhileParkour = true;
	boolean RespawnOnLava = false;
	boolean RespawnOnWater = false;
	boolean FullHunger = false;
	boolean LastCheckpointTeleport = false;
	boolean rewardEnable = false;
	boolean rewardIfBetterScore = true;

	//Used for player parkour management
	Location lobby = null;
	ArrayList<Integer> maps = new ArrayList<Integer>();
	HashMap<Integer, Boolean> toggleParkour = new HashMap<Integer, Boolean>(); //Parkour active or not
	HashMap<Location, String> cLoc = new HashMap<Location, String>(); //HashMap infos>     Location : mapNumber_Chekcpoint
	HashMap<String, String> Parkour = new HashMap<String, String>(); //HashMap infos>    playerName : mapNumber_parkourStartTime_Chekcpoint
	HashMap<String, Long> Records = new HashMap<String, Long>();
	HashMap<String, Long> rewardPlayersCooldown = new HashMap<String, Long>(); //HashMap infos>    playerName : LastRewardTime

	//Used for saveing/loading scores
	String path = "plugins" + File.separator + "mwParkour2" + File.separator + "PlayersScores.scores";
	File scores = new File(path);

	public void onEnable()
	{
		setupEconomy();

		LoadCfg();

		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (IOException e)
		{
			debug("Failed to submit data :(");
		}

		getServer().getPluginManager().registerEvents(this, this);

		if (!scores.getAbsoluteFile().exists())
		{
			try
			{
				scores.createNewFile();
				saveScore();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		intMaps();
		loadScore();
		loadToggleMap();
		loadLobby();
		intCheckpointsLoc();
	}

	private boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
		{
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args)
	{
		Player p = null;
		if (sender instanceof Player)
		{
			p = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("parkour") && p != null)
		{
			if (args.length == 0)
			{
				p.sendMessage("§6---------=[ §8mwParkour2 v" + getDescription().getVersion() + " by monowii §6]=---------");

				if (p.hasPermission("parkour.mapeditor") || p.hasPermission("parkour.admin"))
				{
					p.sendMessage("§a/" + CommandLabel + " new <mapName> <mapCreator>§f  - Create a new map");
					p.sendMessage("§a/" + CommandLabel + " done§f  - Confirm and create the map");
					p.sendMessage("§a/" + CommandLabel + " remove <mapNumber>§f  - Remove a map");
					p.sendMessage("§a/" + CommandLabel + " changeMapName <mapNumber> <newMapName>§f  - Change the map name");
					p.sendMessage("§a/" + CommandLabel + " changeMapCreator <mapNumber> <newMapCreator>§f  - Change the Creator");
					p.sendMessage("§a/" + CommandLabel + " setSpawn <mapNumber>§f  - Set the map spawn");
				}
				if (p.hasPermission("parkour.admin"))
				{
					p.sendMessage("§2/" + CommandLabel + " setLobby§f  - Set the lobby spawn");
					p.sendMessage("§2/" + CommandLabel + " toggle <mapNumber>§f  - toggle ON/OFF a parkour");
					p.sendMessage("§2/" + CommandLabel + " resetScores <mapNumber>§f  - Reset All scores for a map");
					p.sendMessage("§2/" + CommandLabel + " pReset <Player> [<mapNumber> / all]§f  - Reset scores for a player");
				}
				p.sendMessage("/§7" + CommandLabel + " MapList§f  - Show all the maps");
				p.sendMessage("/§7" + CommandLabel + " best <MapNumber>§f  - Show the best score of a map");
				p.sendMessage("/§7" + CommandLabel + " leave§f - Left the map");
				p.sendMessage("/§7" + CommandLabel + " join <mapNumber>§f - Join a map");
			}
			else
			{
				if (args[0].equalsIgnoreCase("join"))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{

								if (Parkour.containsKey(p.getName()))
								{
									Parkour.remove(p.getName());
								}

								FileConfiguration cfg = getConfig();

								if (cfg.contains("Parkour.map" + args[1] + ".spawn"))
								{
									Location loc = new Location(getServer().getWorld(getConfig().get("Parkour.map" + args[1] + ".world").toString()), cfg.getDouble("Parkour.map" + args[1] + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + args[1] + ".spawn.posY"), cfg.getDouble("Parkour.map" + args[1] + ".spawn.posZ"));
									loc.setPitch((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posYaw"));

									p.teleport(loc);
								}
								else
								{
									p.sendMessage("§cMap spawn is not set !");
								}

							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("You must specify the map number !");
					}
				}
				else if (args[0].equalsIgnoreCase("leave"))
				{
					if (Parkour.containsKey(p.getName()))
					{
						p.sendMessage("§aYou leave the parkour !");
						Parkour.remove(p.getName());
						if (lobby != null)
						{
							p.teleport(lobby);
						}

					}
					else
					{
						p.sendMessage("§cYou are not in a parkour !");
					}
				}

				else if (args[0].equalsIgnoreCase("remove") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								String mapNumber = args[1].toString();
								getConfig().getConfigurationSection("Parkour").set("map" + mapNumber, null);
								getConfig().set("Parkour.mapsNombre", Integer.valueOf(getConfig().getInt("Parkour.mapsNombre") - 1));
								saveConfig();
								p.sendMessage("§2map" + mapNumber + "§f is now deleted !");

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();)
								{
									String key = it.next();
									String[] KeySplit = key.split(":");
									if (KeySplit[0].equals(args[1]))
									{
										it.remove();
									}
								}
								saveScore();
								intCheckpointsLoc();
								intMaps();
								loadToggleMap();
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("§cYou must specify the map number !");
					}
				}

				else if (args[0].equalsIgnoreCase("toggle") && p.hasPermission("parkour.admin"))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								if (getConfig().getBoolean("Parkour.map" + args[1] + ".toggle"))
								{
									p.sendMessage("Map toggle to §4OFF");
									getConfig().set("Parkour.map" + args[1] + ".toggle", false);
									saveConfig();
								}
								else
								{
									p.sendMessage("Map toggle to §aON");
									getConfig().set("Parkour.map" + args[1] + ".toggle", true);
									saveConfig();
								}
								loadToggleMap();
							}
							else
							{
								p.sendMessage("not valid map");
							}
						}
						else
						{
							p.sendMessage("Not Valid number");
						}
					}
					else
					{
						p.sendMessage("§cYou must specify the map number !");
					}
				}

				else if (args[0].equalsIgnoreCase("new") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (args.length == 3)
					{
						if (args[1] != null && args[2] != null)
						{
							if (!newMap)
							{
								newMapPlayerEditor = p.getName();
								newMap = true;
								p.sendMessage("MapEditor §aON §7(Use the stick and right click on all checkpoint in order then type /pk done)");
								CheckpointNumber = 1;
								newMapName = args[1];
								newMapCreator = args[2];
								NewMapNumber = (maxMapNumber() + 1);
							}
							else
							{
								p.sendMessage("§cA player is already using the MapEditor (" + newMapPlayerEditor + ") ! You must wait a bit !");
							}
						}
						else
						{
							p.sendMessage("§cCorrect usage : /pk new <mapName> <mapCreator>");
						}
					}
					else
					{
						p.sendMessage("§cCorrect usage : /pk new <mapName> <mapCreator>");
					}
				}

				else if (args[0].equalsIgnoreCase("done") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (!newMap)
					{
						p.sendMessage("§cMapEditor is not ON !");
					}
					else
					{
						if (p.getName().equalsIgnoreCase(newMapPlayerEditor))
						{
							if (CheckpointNumber >= 3)
							{
								p.sendMessage("§a" + newMapName + "(map" + NewMapNumber + ") created ! §fMapEditor §4OFF");

								FileConfiguration cfg = getConfig();
								cfg.set("Parkour.mapsNombre", (getConfig().getInt("Parkour.mapsNombre")) + 1);
								cfg.set("Parkour.map" + NewMapNumber + ".world", p.getWorld().getName());
								cfg.set("Parkour.map" + NewMapNumber + ".mapName", newMapName);
								cfg.set("Parkour.map" + NewMapNumber + ".mapCreator", newMapCreator);
								cfg.set("Parkour.map" + NewMapNumber + ".nombreCp", (CheckpointNumber - 1));
								cfg.set("Parkour.map" + NewMapNumber + ".toggle", true);

								saveConfig();
								intMaps();
								loadToggleMap();

								newMapName = null;
								newMapCreator = null;
								CheckpointNumber = 0;
								NewMapNumber = 0;
								newMap = false;
								intCheckpointsLoc();
								newMapCheckpoints.clear();

								newMapPlayerEditor = null;
							}
							else
							{
								p.sendMessage("§cA parkour need at least 3 checkpoints ! §fMapEditor §4OFF");
								newMapPlayerEditor = null;
								newMapName = null;
								newMapCreator = null;
								newMapCheckpoints.clear();
								CheckpointNumber = 0;
								NewMapNumber = 0;
								newMap = false;
							}

						}
						else
						{
							p.sendMessage("§cA player is already using the MapEditor (" + newMapPlayerEditor + ") ! You must wait a bit !");
						}
					}
				}

				else if (args[0].equalsIgnoreCase("changeMapName") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (args.length == 3)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								getConfig().set("Parkour.map" + args[1] + ".mapName", args[2]);
								saveConfig();
								p.sendMessage("§aMap name set to '§b" + args[2] + "'§a for map" + args[1]);
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("§cCorrect usage : /pk changeMapName <mapNumber> <newMapName>");
					}
				}

				else if (args[0].equalsIgnoreCase("changeMapCreator") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (args.length == 3)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								getConfig().set("Parkour.map" + args[1] + ".mapCreator", args[2]);
								saveConfig();
								p.sendMessage("§aCreator set to '§b" + args[2] + "'§a for map" + args[1]);
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("§cCorrect usage /pk ... !");
					}
				}

				else if (args[0].equalsIgnoreCase("setspawn") && (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor")))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								cfg.set("Parkour.map" + mapNumber + ".spawn.posX", p.getLocation().getX());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posY", p.getLocation().getY());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posZ", p.getLocation().getZ());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posPitch", p.getLocation().getPitch());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posYaw", p.getLocation().getYaw());
								saveConfig();
								p.sendMessage("§aParkour spawn set to §2map" + mapNumber + "§f !");
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}

					}
					else
					{
						p.sendMessage("You don't specify the map !");
					}
				}

				else if (args[0].equalsIgnoreCase("setLobby") && p.hasPermission("parkour.admin"))
				{
					FileConfiguration cfg = getConfig();
					cfg.set("Lobby.world", p.getWorld().getName());
					cfg.set("Lobby.posX", p.getLocation().getX());
					cfg.set("Lobby.posY", p.getLocation().getY());
					cfg.set("Lobby.posZ", p.getLocation().getZ());
					cfg.set("Lobby.posPitch", p.getLocation().getPitch());
					cfg.set("Lobby.posYaw", p.getLocation().getYaw());
					saveConfig();
					p.sendMessage("§aLobby set !");
					loadLobby();
				}

				else if (args[0].equalsIgnoreCase("pReset") && p.hasPermission("parkour.admin"))
				{
					if (args.length == 3)
					{
						boolean DeleteOnAllMaps = false;
						if (args[2].equalsIgnoreCase("all"))
						{
							DeleteOnAllMaps = true;
						}

						if (isNumber(args[2]) || DeleteOnAllMaps)
						{
							if ((isNumber(args[2]) && maps.contains(toInt(args[2]))) || DeleteOnAllMaps)
							{
								boolean PlayerFound = false;
								String player = args[1];
								String mapNumber = args[2];

								Iterator<String> it = Records.keySet().iterator();

								while (it.hasNext())
								{
									String key = it.next();
									String[] KeySplit = key.split(":");

									System.out.println("Key: " + key);

									if (KeySplit[1].equalsIgnoreCase(player))
									{
										if (DeleteOnAllMaps)
										{
											it.remove();
											PlayerFound = true;
										}
										else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapNumber))
										{
											PlayerFound = true;
											it.remove();
										}
									}
								}
								saveScore();

								if (!PlayerFound)
								{
									p.sendMessage("§cPlayer not found in this scoreboard !");
									return false;
								}

								if (DeleteOnAllMaps)
								{
									p.sendMessage("§aScores reset for player " + player + " on all maps !");
								}
								else
								{
									p.sendMessage("§aScores reset for player " + player + " on map " + mapNumber + " !");
								}

								loadScore();
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("§cYou must specify the player/mapNumber !");
					}
				}

				else if (args[0].equalsIgnoreCase("MapList"))
				{
					p.sendMessage("§8---------=[§a Parkour Map List §8]=---------");

					for (int i : maps)
					{
						String mapNumber = "" + i;
						if (maps.contains(toInt(mapNumber)))
						{
							String mode = "§4OFF";
							if (toggleParkour.get(i))
							{
								mode = "§aON";
							}
							p.sendMessage(mode + " §f| §b " + getMapName(i) + "§7 (§2map" + i + "§7) §7(" + getCfgTotalCheckpoints(i) + " Checkpoints) §aby " + getMapCreator(i));
						}
					}
				}

				else if (args[0].equalsIgnoreCase("best"))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								DisplayMapScores(args[1], p.getName());
							}
							else
							{
								p.sendMessage("§cUnknown map number !");
							}
						}
						else
						{
							p.sendMessage("§cThis is not a valid mapNumber !");
						}
					}
					else
					{
						p.sendMessage("§cYou don't specify the map !");
					}
				}

				else if (args[0].equalsIgnoreCase("resetScores") && p.hasPermission("parkour.admin"))
				{
					if (args.length == 2)
					{
						if (isNumber(args[1]))
						{
							if (maps.contains(toInt(args[1])))
							{
								int mapNumber = Integer.parseInt(args[1]);
								p.sendMessage("§2Scores reset for map" + mapNumber + "!");

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();)
								{
									String key = it.next();
									String[] pName = key.split(":");
									int pMap = Integer.parseInt(pName[0]);
									if (pMap == mapNumber)
									{
										it.remove();
									}
								}
								saveScore();
							}
							else
							{
								p.sendMessage("§cIt is not a valid mapNumber !");
							}
						}
						else
						{
							p.sendMessage("§cIt is not a valid number !");
						}
					}
					else
					{
						p.sendMessage("§cYou must specify the map number !");
					}
				}

				else
				{

					p.sendMessage("§cUnknown command arguments !");

				}
			}
		}
		return false;
	}

	/////////////////////////////////
	// _____                _       
	//|  ___|              | |      
	//| |____   _____ _ __ | |_ ___ 
	//|  __\ \ / / _ \ '_ \| __/ __|
	//| |___\ V /  __/ | | | |_\__ \
	//\____/ \_/ \___|_| |_|\__|___/
	////////////////////////////////

	@EventHandler
	public void onDisco(PlayerQuitEvent e)
	{
		if (Parkour.containsKey(e.getPlayer().getName()))
		{
			Parkour.remove(e.getPlayer().getName());
		}
		if (rewardPlayersCooldown.containsKey(e.getPlayer().getName()))
		{
			rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(newMapPlayerEditor))
		{
			newMapPlayerEditor = null;
			newMapName = null;
			newMapCreator = null;
			newMapCheckpoints.clear();
			CheckpointNumber = 0;
			NewMapNumber = 0;
			newMap = false;
			System.out.println("playerEditor has left");
		}
	}

	@EventHandler
	public void onPlayerDmg(EntityDamageEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			Player p = (Player) e.getEntity();
			if (Parkour.containsKey(p.getName()) && InvincibleWhileParkour)
			{
				e.setCancelled(true);
				p.setFireTicks(0);
			}

		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e)
	{
		if (e.getLine(0).equalsIgnoreCase("[mwParkour2]") && !e.getPlayer().hasPermission("parkour.mapeditor")) //Block player try to bypass permissions for place signs
		{
			e.setCancelled(true);
		}

		if (e.getPlayer().hasPermission("parkour.mapeditor"))
		{
			//15 char max par lines (on sign)

			if (e.getLine(0).equalsIgnoreCase("[pk2]"))
			{
				if (e.getLine(1).equalsIgnoreCase("leave"))
				{
					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "leave");
					e.setLine(2, "");
					e.setLine(3, "");
				}
				if (e.getLine(1).equalsIgnoreCase("joinlastmap"))
				{
					int mapNumber = maxMapNumber();

					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "joinLastMap");
					e.setLine(2, "§b" + getMapName(mapNumber));
					e.setLine(3, "(map" + mapNumber + ")");
				}
				if (e.getLine(1).equalsIgnoreCase("join"))
				{
					if (isNumber(e.getLine(2)))
					{
						if (maps.contains(toInt(e.getLine(2))))
						{
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[mwParkour2]");
							e.setLine(1, "join");
							e.setLine(2, "§b" + getMapName(MapNumber));
							e.setLine(3, "(map" + MapNumber + ")");
						}
						else
						{
							e.setCancelled(true);
						}
					}
					else
					{
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("lastbestscores"))
				{
					int mapNumber = maxMapNumber();

					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "lastBestScores");
					e.setLine(2, "§b" + getMapName(mapNumber));
					e.setLine(3, "(map" + mapNumber + ")");
				}
				if (e.getLine(1).equalsIgnoreCase("infos"))
				{
					if (isNumber(e.getLine(2)))
					{
						if (maps.contains(toInt(e.getLine(2))))
						{
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "Parkour #" + MapNumber);
							e.setLine(1, "---------------");
							e.setLine(2, "§b" + getMapName(MapNumber));
							e.setLine(3, getMapCreator(MapNumber));
						}
						else
						{
							e.setCancelled(true);
						}
					}
					else
					{
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("best"))
				{
					if (isNumber(e.getLine(2)))
					{
						if (maps.contains(toInt(e.getLine(2))))
						{
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[mwParkour2]");
							e.setLine(1, "bestScores");
							e.setLine(2, "§b" + getMapName(MapNumber));
							e.setLine(3, "(map" + MapNumber + ")");
						}
						else
						{
							e.setCancelled(true);
						}
					}
					else
					{
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onIntaract(PlayerInteractEvent e)
	{
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (e.getClickedBlock().getState() instanceof Sign)
			{
				Sign s = (Sign) e.getClickedBlock().getState();

				if (s.getLine(0).equals("[mwParkour2]"))
				{

					if (s.getLine(1).equals("leave"))
					{
						if (Parkour.containsKey(e.getPlayer().getName()))
						{
							e.getPlayer().sendMessage("§aYou leave the parkour !");
							Parkour.remove(e.getPlayer().getName());

						}
						if (lobby != null)
						{
							e.getPlayer().teleport(lobby);
						}
					}
					if (s.getLine(1).equals("joinLastMap"))
					{
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);
						
						if (isNumber(mapNumber))
						{
							if (maps.contains(toInt(mapNumber)))
							{
								if (toInt(mapNumber) == maxMapNumber())
								{
									Player p = e.getPlayer();
									FileConfiguration cfg = getConfig();

									if (cfg.contains("Parkour.map" + mapNumber + ".spawn"))
									{
										Location loc = new Location(
												getServer().getWorld(getConfig().getString("Parkour.map" + mapNumber + ".world")),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posX"),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posY"),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posZ"));
										
										loc.setPitch((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posPitch"));
										loc.setYaw((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posYaw"));

										if (!loc.getChunk().isLoaded())
										{
											loc.getChunk().load(true);
										}

										p.teleport(loc);
									}
									else
									{
										p.sendMessage("§cMap spawn is not set !");
									}
								}
								else
								{
									s.setLine(0, "[mwParkour2]");
									s.setLine(1, "joinLastMap");
									s.setLine(2, "§b" + getMapName(maxMapNumber()));
									s.setLine(3, "(map" + maxMapNumber() + ")");
									s.update();
								}
							}
							else
							{
								s.setLine(0, "[mwParkour2]");
								s.setLine(1, "joinLastMap");
								s.setLine(2, "§b" + getMapName(maxMapNumber()));
								s.setLine(3, "(map" + maxMapNumber() + ")");
								s.update();
							}
						}
					}
					if (s.getLine(1).equals("lastBestScores"))
					{
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber))
						{
							if (maps.contains(toInt(mapNumber)))
							{
								if (toInt(mapNumber) == maxMapNumber())
								{
									DisplayMapScores(mapNumber, e.getPlayer().getName());
								}
								else
								{
									s.setLine(0, "[mwParkour2]");
									s.setLine(1, "lastBestScores");
									s.setLine(2, "§b" + getMapName(maxMapNumber()));
									s.setLine(3, "(map" + maxMapNumber() + ")");
									s.update();
								}
							}
							else
							{
								s.setLine(0, "[mwParkour2]");
								s.setLine(1, "lastBestScores");
								s.setLine(2, "§b" + getMapName(maxMapNumber()));
								s.setLine(3, "(map" + maxMapNumber() + ")");
								s.update();
							}
						}
					}
					if (s.getLine(1).equals("join"))
					{
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber))
						{
							if (maps.contains(toInt(mapNumber)))
							{
								Player p = e.getPlayer();

								if (Parkour.containsKey(p.getName()))
								{
									Parkour.remove(p.getName());
								}

								FileConfiguration cfg = getConfig();

								if (cfg.contains("Parkour.map" + mapNumber + ".spawn"))
								{
									Location loc = new Location(
											getServer().getWorld(getConfig().getString("Parkour.map" + mapNumber + ".world")),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posY"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posZ"));
									
									loc.setPitch((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posYaw"));

									if (!loc.getChunk().isLoaded())
									{
										loc.getChunk().load(true);
									}

									p.teleport(loc);
								}
								else
								{
									p.sendMessage("§cMap spawn is not set !");
								}
							}
							else
							{
								e.getPlayer().sendMessage("§cThis map no longer exists !");
							}
						}
					}

					if (s.getLine(1).equals("bestScores"))
					{
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber))
						{
							if (maps.contains(toInt(mapNumber)))
							{
								DisplayMapScores(mapNumber, e.getPlayer().getName());
							}
							else
							{
								e.getPlayer().sendMessage("§cThis map no longer exists !");
							}
						}
					}
				}
			}
		}

		if (newMap)//map creation
		{
			if (e.getPlayer().getName().equals(newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				Player p = e.getPlayer();

				if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70)
				{
					if (!cLoc.containsKey(e.getClickedBlock().getLocation()))
					{
						Location bLoc = e.getClickedBlock().getLocation();

						if (newMapCheckpoints.contains(bLoc))
						{
							p.sendMessage("§cThis checkpoint is alredy used for this map !");
						}
						else
						{
							FileConfiguration cfg = getConfig();
							
							p.sendMessage("§8Checkpoint " + CheckpointNumber + " set on new map " + NewMapNumber);

							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posX", bLoc.getX());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posY", bLoc.getY());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posZ", bLoc.getZ());

							saveConfig();
							newMapCheckpoints.add(bLoc);
							CheckpointNumber++;
							
						}
					}
					else
					{
						p.sendMessage("§cThis checkpoint is alredy used for another map !");
					}
				}
				else
				{
					p.sendMessage("§cUse a stick to place checkpoints (Right click on stone pressure plate)");
				}
			}
		}

		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getPlayer().getItemInHand().getTypeId() == 280) //player use stick to teleport to last last checkpoint
		{
			if (Parkour.containsKey(e.getPlayer().getName()))
				teleportLastCheckpoint(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		if (((int) e.getFrom().getX() != (int) e.getTo().getX()) || ((int) e.getFrom().getY() != (int) e.getTo().getY()) || ((int) e.getFrom().getZ() != (int) e.getTo().getZ()))
		{
			if (e.getTo().getBlock().getTypeId() == 70)
			{
				int x = (int) e.getTo().getBlock().getX();
				int y = (int) e.getTo().getBlock().getY();
				int z = (int) e.getTo().getBlock().getZ();
				Location bLoc = new Location(e.getTo().getWorld(), x, y, z);

				if (cLoc.containsKey(bLoc))
				{
					Player p = e.getPlayer();

					int Checkpoint = getCheckpoint(cLoc.get(bLoc).toString());

					if (!p.hasPermission("parkour.use"))
					{
						p.sendMessage("§cYou don't have permission to do this parkour !");
						return;
					}

					if (!toggleParkour.get(getCpMapNumber(cLoc.get(bLoc).toString())))
					{
						p.sendMessage("This parkour is §4OFF");
						return;
					}

					if (!Parkour.containsKey(p.getName()))
					{

						if (Checkpoint == 1)
						{
							int Map = getCpMapNumber(cLoc.get(bLoc).toString());

							Parkour.put(p.getName(), (getCpMapNumber(cLoc.get(bLoc).toString()) + "_" + Long.valueOf(System.currentTimeMillis()) + "_1"));
							p.sendMessage("§aYou have started the parkour on '§b" + getMapName(Map) + "'§a by §2" + getMapCreator(Map) + " §7(§amap" + Map + "§7)");

							if (CheckpointEffect)
							{
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (removePotionsEffectsOnParkour)
							{
								for (PotionEffect effect : p.getActivePotionEffects())
								{
									p.removePotionEffect(effect.getType());
								}
							}
							if (FullHunger)
							{
								p.setFoodLevel(20);
							}
						}
						else
						{
							p.sendMessage("§cYou must start at the checkpoint 1 !");
						}
					}
					else
					{
						int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()).toString());
						int CpMap = getCpMapNumber(cLoc.get(bLoc).toString());
						int Map = getPlMapNumber(Parkour.get(p.getName()).toString());
						int TotalCheckpoints = getCfgTotalCheckpoints(Map);

						if (CpMap != Map)
						{
							if (Checkpoint == 1)
							{
								p.sendMessage("§aYou have started the parkour on '§b" + getMapName(Map) + "'§a by §2" + getMapCreator(Map) + " §7(§amap" + CpMap + "§7)");
								Parkour.put(p.getName(), (getCpMapNumber(cLoc.get(bLoc).toString()) + "_" + Long.valueOf(System.currentTimeMillis()) + "_1"));

								if (CheckpointEffect)
								{
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour)
								{
									for (PotionEffect effect : p.getActivePotionEffects())
									{
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger)
								{
									p.setFoodLevel(20);
								}

							}
							else
							{
								p.sendMessage("§cYou are not in the parkour !");

							}
						}
						else
						{

							if (Checkpoint == 1)
							{
								if (CheckpointEffect)
								{
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour)
								{
									for (PotionEffect effect : p.getActivePotionEffects())
									{
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger)
								{
									p.setFoodLevel(20);
								}

								p.sendMessage("§aYou have restarted your time !");
								setPlTime(p.getName(), Long.valueOf(System.currentTimeMillis()));
								setPlCheckpoint(p.getName(), 1);

							}
							else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1)))
							{
								if (CheckpointEffect)
								{
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								long totalTime = System.currentTimeMillis() - Long.valueOf(getPlTime(Parkour.get(p.getName()).toString()));
								Parkour.remove(p.getName());

								if (!Records.containsKey(Map + ":" + p.getName()))
								{

									p.sendMessage("§bYou finished this parkour for the first time in " + convertTime(totalTime));
									Records.put(Map + ":" + p.getName(), totalTime);
									saveScore();

									if (BroadcastMessage)
									{
										getServer().broadcastMessage(BroadcastMsg.replace("&", "§").replaceAll("PLAYER", p.getName()).replaceAll("MAPNAME", getMapName(Map)));
									}
									giveReward(p, Map);

								}
								else
								{

									if (Records.get(Map + ":" + p.getName()) >= totalTime)
									{

										p.sendMessage("§2You beat your old score !");
										p.sendMessage("§aYou finished this parkour in " + convertTime(totalTime));
										Records.put(Map + ":" + p.getName(), totalTime);
										saveScore();

										if (BroadcastMessage)
										{
											getServer().broadcastMessage(BroadcastMsg.replace("&", "§").replaceAll("PLAYER", p.getName()).replaceAll("MAPNAME", getMapName(Map)));
										}
										giveReward(p, Map);

									}
									else
									{
										p.sendMessage("§4You don't beat your old score");
										p.sendMessage("§aYou finished this parkour in " + convertTime(totalTime));

										if (!rewardIfBetterScore)
										{
											giveReward(p, Map);
										}
									}

								}

								final String pl = p.getName();

								if (lobby != null)
								{
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
									{
										public void run()
										{
											getServer().getPlayer(pl).teleport(lobby);
										}
									}, 5L);
								}
							}
							else if (PlCheckpoint == (Checkpoint - 1))
							{

								if (CheckpointEffect)
								{
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage("§bCheckpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2) + " reached !");

							}
							else if (Checkpoint <= PlCheckpoint)
							{
								p.sendMessage("§cYou already reached this checkpoint !");

							}
							else if (Checkpoint > PlCheckpoint)
							{
								p.sendMessage("§cYou forgot to pass the last checkpoint !");

							}
						}
					}
				}
			}
			if ((e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER) && RespawnOnWater)
			{
				if (Parkour.containsKey(e.getPlayer().getName()))
					teleportLastCheckpoint(e.getPlayer());
			}
			if ((e.getTo().getBlock().getType() == Material.LAVA || e.getTo().getBlock().getType() == Material.STATIONARY_LAVA) && RespawnOnLava)
			{
				if (Parkour.containsKey(e.getPlayer().getName()))
					teleportLastCheckpoint(e.getPlayer());
			}
		}
	}

	///////////////////////////////////////////////
	//______               _   _                 
	//|  ___|             | | (_)                
	//| |_ ___  _ __   ___| |_ _  ___  _ __  ___ 
	//|  _/ _ \| '_ \ / __| __| |/ _ \| '_ \/ __|
	//| || (_) | | | | (__| |_| | (_) | | | \__ \
	//\_| \___/|_| |_|\___|\__|_|\___/|_| |_|___/
	///////////////////////////////////////////////

	private void teleportLastCheckpoint(Player p)
	{
		FileConfiguration cfg = getConfig();
		Location lastCheckpoint = null;

		int MapNumber = getPlMapNumber(Parkour.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()));
		
		if (PlCheckpoint == 1 || !LastCheckpointTeleport) //Teleport to map spawn
		{
			if (cfg.contains("Parkour.map" + MapNumber + ".spawn"))
			{
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posX"),
						cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posY"),
						cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posZ"));
				
				lastCheckpoint.setPitch((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posPitch"));
				lastCheckpoint.setYaw((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posYaw"));

				p.teleport(lastCheckpoint);
			}
			else
			{
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posX") + 0.5,
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posY"),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posZ") + 0.5);

				lastCheckpoint.setPitch(p.getLocation().getPitch());
				lastCheckpoint.setYaw(p.getLocation().getYaw());
				p.teleport(lastCheckpoint);
			}
		}
		else
		{
			lastCheckpoint = new Location(
					getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posX") + 0.5,
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posY"),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posZ") + 0.5);

			lastCheckpoint.setPitch(p.getLocation().getPitch());
			lastCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(lastCheckpoint);
		}
	}

	private void setPlCheckpoint(String p, int Cp)
	{
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		Parkour.put(p, CpFinal);
	}

	private void setPlTime(String p, Long Time)
	{
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		Parkour.put(p, TimeFinal);
	}

	private Long getPlTime(String HashTable)
	{
		String[] Splitter = HashTable.split("_");
		Long Time = Long.valueOf(Splitter[1]);
		return Time;
	}

	private int getPlCheckpoint(String HashTable)
	{
		String[] Splitter = HashTable.split("_");
		int Cp = Integer.parseInt(Splitter[2]);
		return Cp;
	}

	private int getPlMapNumber(String HashTable)
	{
		String[] Splitter = HashTable.split("_");
		int mapNumber = Integer.parseInt(Splitter[0]);
		return mapNumber;
	}

	private int getCpMapNumber(String HashTable)
	{
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[0]);
		return CpMap;
	}

	private int getCheckpoint(String HashTable)
	{
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[1]);
		return CpMap;
	}

	private int getCfgTotalCheckpoints(int mapNumber)
	{
		return getConfig().getInt("Parkour.map" + mapNumber + ".nombreCp");
	}

	private String getMapCreator(int mapNumber)
	{
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapCreator"))
		{
			return getConfig().getString("Parkour.map" + mapNumber + ".mapCreator");

		}
		else
		{
			return "unknownCreator";
		}
	}

	private String getMapName(int mapNumber)
	{
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapName"))
		{
			return getConfig().getString("Parkour.map" + mapNumber + ".mapName");
		}
		else
		{
			return "unknownMapName";
		}
	}

	private boolean mapExist(String MapNumber)
	{
		if (getConfig().getInt("Parkour.map" + MapNumber + ".nombreCp") != 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean isNumber(String number)
	{
		try
		{
			Integer.parseInt(number);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private void intCheckpointsLoc()
	{
		cLoc.clear();
		FileConfiguration cfg = getConfig();
		for (int mapNumber : maps)
		{
			for (int i = cfg.getInt("Parkour.map" + mapNumber + ".nombreCp"); i >= 1; i--)
			{
				Location loc = new Location(getServer().getWorld(cfg.getString("Parkour.map" + mapNumber + ".world")), cfg.getInt("Parkour.map" + mapNumber + ".cp." + i + ".posX"), cfg.getInt("Parkour.map" + mapNumber
						+ ".cp." + i + ".posY"), cfg.getInt("Parkour.map" + mapNumber + ".cp." + i + ".posZ"));
				String HashTable = mapNumber + "_" + i;
				cLoc.put(loc, HashTable);
			}
		}
	}

	private void intMaps()
	{
		maps.clear();
		String mapList = getConfig().getConfigurationSection("Parkour").getKeys(false).toString().replaceAll("\\s", "").replace("[", "").replace("]", "");
		String[] mapsSplit = mapList.split(",");
		for (int i = getConfig().getInt("Parkour.mapsNombre"); i >= 0; i--)
		{
			if (mapExist(mapsSplit[i].substring(3)))
			{
				maps.add(Integer.parseInt(mapsSplit[i].substring(3)));
			}
		}
		Collections.sort(maps);
	}

	private void loadToggleMap()
	{
		toggleParkour.clear();
		for (int mapNumber : maps)
		{
			if (getConfig().contains("Parkour.map" + mapNumber + ".toggle"))
			{
				toggleParkour.put(mapNumber, getConfig().getBoolean("Parkour.map" + mapNumber + ".toggle"));
			}
		}
	}

	private void LoadCfg()
	{
		FileConfiguration cfg = getConfig();

		//Options
		cfg.addDefault("options.InvincibleWhileParkour", true);
		cfg.addDefault("options.RespawnOnLava", true);
		cfg.addDefault("options.RespawnOnWater", true);
		cfg.addDefault("options.CheckpointEffect", true);
		cfg.addDefault("options.removePotionsEffectsOnParkour", false);
		cfg.addDefault("options.setFullHungerOnParkour", false);
		cfg.addDefault("options.LastCheckpointTeleport", true);

		//Rewards
		cfg.addDefault("rewards.enable", false);
		cfg.addDefault("rewards.cooldown", 300);
		cfg.addDefault("rewards.cooldownMessage", "You will receive your next reward on this map in TIME");
		cfg.addDefault("rewards.rewardIfBetterScore", true);

		cfg.addDefault("rewards.money.enable", false);
		cfg.addDefault("rewards.money.amount", 10);
		cfg.addDefault("rewards.money.message", "&bYou have received MONEYAMOUNT Dollars !");
		cfg.addDefault("rewards.command.enable", false);
		cfg.addDefault("rewards.command.cmd", "give PLAYER 5 10");
		cfg.addDefault("rewards.command.message", "&bYou have received 5 wood !");

		cfg.addDefault("options.BroadcastOnRecord.enable", true);
		cfg.addDefault("options.BroadcastOnRecord.message", "&emwParkour2&f>&8 New record for &7PLAYER &8on map MAPNAME !");

		cfg.addDefault("Parkour.mapsNombre", 0);
		cfg.options().copyDefaults(true);
		saveConfig();

		removePotionsEffectsOnParkour = cfg.getBoolean("options.removePotionsEffectsOnParkour");
		InvincibleWhileParkour = cfg.getBoolean("options.InvincibleWhileParkour");
		RespawnOnLava = cfg.getBoolean("options.RespawnOnLava");
		RespawnOnWater = cfg.getBoolean("options.RespawnOnWater");
		CheckpointEffect = cfg.getBoolean("options.CheckpointEffect");
		BroadcastMessage = cfg.getBoolean("options.BroadcastOnRecord.enable");
		FullHunger = cfg.getBoolean("options.BroadcastOnRecord.enable");
		LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");

		rewardIfBetterScore = cfg.getBoolean("rewards.rewardIfBetterScore");
		rewardEnable = cfg.getBoolean("rewards.enable");

		if (BroadcastMessage)
		{
			BroadcastMsg = cfg.getString("options.BroadcastOnRecord.message");
		}

	}

	private void loadLobby()
	{
		FileConfiguration cfg = getConfig();

		if (cfg.contains("Lobby"))
		{
			lobby = null;
			Location loc = new Location(getServer().getWorld(cfg.getString("Lobby.world")), cfg.getDouble("Lobby.posX"), cfg.getDouble("Lobby.posY"), cfg.getDouble("Lobby.posZ"));
			loc.setPitch((float) cfg.getDouble("Lobby.posPitch"));
			loc.setYaw((float) cfg.getDouble("Lobby.posYaw"));
			lobby = loc;
		}
	}

	private String convertTime(long ms)
	{
		int ms1 = (int) ms;
		int secs = ms1 / 1000;
		int mins = secs / 60;
		int hours = mins / 60;

		hours %= 24;
		secs %= 60;
		mins %= 60;
		ms1 %= 1000;

		String hoursS = Integer.toString(hours);
		String secsS = Integer.toString(secs);
		String minsS = Integer.toString(mins);
		String ms2 = Integer.toString(ms1);

		if (secs < 10)
		{
			secsS = "0" + secsS;
		}
		if (mins < 10)
		{
			minsS = "0" + minsS;
		}
		if (hours < 10)
		{
			hoursS = "0" + hoursS;
		}

		return hoursS + "h:" + minsS + "m:" + secsS + "s:" + ms2 + "ms";
	}

	private int maxMapNumber()
	{
		return getConfig().getInt("Parkour.mapsNombre");
	}

	private void saveScore()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((path))));
			oos.writeObject((Object) Records);
			oos.flush();
			oos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void loadScore()
	{
		try
		{
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
			Records.clear();
			@SuppressWarnings("unchecked")
			HashMap<String, Long> scoreMap = (HashMap<String, Long>) ois.readObject();
			Records = scoreMap;
			ois.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private int toInt(String msg)
	{
		return Integer.parseInt(msg);
	}

	private void debug(String msg)
	{
		System.out.println("[ mwParkourDebug ] " + msg);
	}

	private void DisplayMapScores(String map, String player)
	{
		ArrayList<String> players = new ArrayList<String>();
		ArrayList<Long> theTimes = new ArrayList<Long>();
		int NombreRecordsMap = 0; //Number of recoreds saved in a map

		for (Entry<String, Long> entry : Records.entrySet())
		{
			String[] mapNumber = entry.getKey().split(":");
			if (mapNumber[0].equals(map))
			{
				theTimes.add(entry.getValue());
				NombreRecordsMap++;
			}
		}

		Collections.sort(theTimes);

		for (int i = 0; i < NombreRecordsMap; i++)
		{
			for (Entry<String, Long> entry : Records.entrySet())
			{
				if (entry.getValue() == theTimes.get(i))
				{
					String[] pName = entry.getKey().split(":");
					players.add(pName[1]);
				}
			}
		}

		getServer().getPlayer(player).sendMessage("§8---=§2Best§8=§2times§8=§7( §b" + getMapName(Integer.parseInt(map)) + "§7 by§2 " + getMapCreator(Integer.parseInt(map)) + " §7(§aMap" + map + "§7) )§8=---");

		//If the player is in top 10
		boolean PlayerNotInTop = true;

		for (int i = 0; i < NombreRecordsMap; i++)
		{
			if (i < 10)
			{
				if (player == players.get(i))
				{
					PlayerNotInTop = false;
				}
				if (i == 0)
				{
					getServer().getPlayer(player).sendMessage("§f#§e" + (i + 1) + " §6" + players.get(i) + " - " + convertTime((theTimes.get(i)).longValue()));
				}
				else
				{
					getServer().getPlayer(player).sendMessage("§f#§e" + (i + 1) + " §b" + players.get(i) + " - " + convertTime((theTimes.get(i)).longValue()));
				}
			}
			if (!PlayerNotInTop)
			{
				getServer().getPlayer(player).sendMessage("§8--§aYour§8-§atime§8--");
				getServer().getPlayer(player).sendMessage("§f#§e" + (i + 1) + " §b" + player + " - " + convertTime((theTimes.get(i)).longValue()));
			}
		}
	}

	private void giveReward(Player p, int mapNumber)
	{
		if (rewardEnable)
		{
			FileConfiguration cfg = getConfig();

			boolean rewardMoneyEnable = cfg.getBoolean("rewards.money.enable");
			boolean rewardCommandEnable = cfg.getBoolean("rewards.command.enable");

			String rewardMoneyMsg = cfg.getString("rewards.money.message");
			String rewardCommandMsg = cfg.getString("rewards.command.message");

			String rewardCmd = cfg.getString("rewards.command.cmd");
			int rewardMoney = cfg.getInt("rewards.money.amount");

			int rewardCooldown = cfg.getInt("rewards.cooldown");
			String rewardCooldownMsg = cfg.getString("rewards.cooldownMessage");

			if (!rewardPlayersCooldown.containsKey(p.getName()))
			{
				if (rewardMoneyEnable && rewardMoney > 0)
				{
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					economy.depositPlayer(p.getName(), rewardMoney);
					p.sendMessage(rewardMoneyMsg.replace("&", "§").replaceAll("MONEYAMOUNT", "" + rewardMoney));
				}
				if (rewardCommandEnable)
				{
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					getServer().dispatchCommand(getServer().getConsoleSender(), rewardCmd.replaceAll("PLAYER", p.getName()));
					p.sendMessage(rewardCommandMsg.replace("&", "§"));
				}
			}
			else
			{
				if (System.currentTimeMillis() - rewardPlayersCooldown.get(p.getName()) >= rewardCooldown * 1000)
				{
					if (rewardMoneyEnable && rewardMoney > 0)
					{
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						economy.depositPlayer(p.getName(), rewardMoney);
						p.sendMessage(rewardMoneyMsg.replace("&", "§").replaceAll("MONEYAMOUNT", "" + rewardMoney));
					}
					if (rewardCommandEnable)
					{
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						getServer().dispatchCommand(getServer().getConsoleSender(), rewardCmd.replaceAll("PLAYER", p.getName()));
						p.sendMessage(rewardCommandMsg.replace("&", "§"));
					}
				}
				else
				{
					long time = (System.currentTimeMillis() - rewardPlayersCooldown.get(p.getName()));

					int ms1 = (int) time;
					int secs = ms1 / 1000;
					int mins = secs / 60;
					int hours = mins / 60;

					hours %= 24;
					secs %= 60;
					mins %= 60;
					ms1 %= 1000;

					String hoursS = Integer.toString(hours);
					String secsS = Integer.toString(secs);
					String minsS = Integer.toString(mins);

					if (secs < 10)
					{
						secsS = "0" + secsS;
					}
					if (mins < 10)
					{
						minsS = "0" + minsS;
					}
					if (hours < 10)
					{
						hoursS = "0" + hoursS;
					}

					p.sendMessage(rewardCooldownMsg.replaceAll("TIME", hoursS + "h:" + minsS + "m:" + secsS + "s"));
				}
			}
		}
	}

}
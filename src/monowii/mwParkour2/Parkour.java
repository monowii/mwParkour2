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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
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

public class Parkour extends JavaPlugin implements Listener {

	/*
	 * Permissions
	 * 
	 * Use parkour plate : parkour.use
	 * 
	 * cp new : parkour.mapeditor cp setMapCreator : parkour.mapeditor cp setMapName : parkour.mapeditor cp remove :
	 * parkour.mapeditor cp setspawn : parkour.mapeditor
	 * 
	 * cp pRemove / resetScores : parkour.admin And ALL CP COMMANDS : parkour.admin
	 */

	/*
	 * 1.5.5 Changelog Better performance onPlayerInteract new Special signs: joinLastMap / lastBestScores better
	 * teleportation with lava / water
	 * 
	 * 1.5.6 Water and lava respawn can no be activated for each map, not global for all maps Works perfectly with 1.6!
	 * 
	 * 1.5.7 A bit of an API
	 * 
	 * 1.5.8 /reload works
	 */

	static Economy economy = null;

	// Used for parkour creation
	ArrayList<Location> newMapCheckpoints = new ArrayList<Location>();
	boolean newMap = false;
	String newMapPlayerEditor = "";
	int CheckpointNumber = 0;
	int NewMapNumber = 0;
	String newMapName = null;
	String newMapCreator = null;

	// Options
	boolean removePotionsEffectsOnParkour = false;
	boolean BroadcastMessage = false;
	String BroadcastMsg = "&emwParkour2&f>&8 New record for &7PLAYER &8on map MAPNAME !";
	boolean CheckpointEffect = true;
	boolean InvincibleWhileParkour = true;
	boolean FullHunger = false;
	boolean LastCheckpointTeleport = false;
	boolean rewardEnable = false;
	boolean rewardIfBetterScore = true;

	boolean vault;

	// Used for player parkour management
	Location lobby = null;
	ArrayList<Integer> maps = new ArrayList<Integer>();
	HashMap<Integer, Boolean> toggleParkour = new HashMap<Integer, Boolean>(); // Parkour active or not
	HashMap<Location, String> cLoc = new HashMap<Location, String>(); // HashMap infos> Location : mapNumber_Chekcpoint
	HashMap<String, String> Parkour = new HashMap<String, String>(); // HashMap infos> playerName :
																		// mapNumber_parkourStartTime_Chekcpoint
	HashMap<String, Long> Records = new HashMap<String, Long>(); // Map:Player, Time
	HashMap<String, Long> rewardPlayersCooldown = new HashMap<String, Long>(); // HashMap infos> playerName :
																				// LastRewardTime

	// Used for saveing/loading scores
	String path = "plugins" + File.separator + "mwParkour2" + File.separator + "PlayersScores.scores";
	File scores = new File(path);

	public void onEnable() {
		LoadCfg();

		this.vault = setupEconomy();

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			debug("Failed to submit data :(");
		}

		getServer().getPluginManager().registerEvents(this, this);

		if (!scores.getAbsoluteFile().exists()) {
			try {
				scores.createNewFile();
				saveScore();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		intMaps();
		loadScore();
		loadToggleMap();
		loadLobby();
		intCheckpointsLoc();
	}

	@Override
	public void onDisable() {
		// Reset everything
		newMap = false;
		newMapCheckpoints.clear();
		newMapCreator = "";
		newMapName = "";
		newMapPlayerEditor = "";
		NewMapNumber = 0;
		CheckpointNumber = 0;

		maps.clear();
		toggleParkour.clear();
		cLoc.clear();
		Parkour.clear();
		Records.clear();
		rewardPlayersCooldown.clear();
	}

	private boolean setupEconomy() {
		try {
			Class.forName("net.milkbowl.vault.economy.Economy");
		} catch (ClassNotFoundException e) {
			debug("Vault not found. Disabling money reward.");
			getConfig().set("rewards.money.enable", false);
			saveConfig();
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(
				net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("parkour") && p != null) {
			if (args.length == 0) {
				p.sendMessage("\u00A76---------=[ \u00A78mwParkour2 v" + getDescription().getVersion()
						+ " by monowii \u00A76]=---------");

				if (p.hasPermission("parkour.mapeditor") || p.hasPermission("parkour.admin")) {
					p.sendMessage("\u00A7a/" + CommandLabel + " new <mapName> <mapCreator>\u00A7f  - Create a new map");
					p.sendMessage("\u00A7a/" + CommandLabel + " done\u00A7f  - Confirm and create the map");
					p.sendMessage("\u00A7a/" + CommandLabel + " remove <mapNumber>\u00A7f  - Remove a map");
					p.sendMessage("\u00A7a/" + CommandLabel
							+ " changeMapName <mapNumber> <newMapName>\u00A7f  - Change the map name");
					p.sendMessage("\u00A7a/" + CommandLabel
							+ " changeMapCreator <mapNumber> <newMapCreator>\u00A7f  - Change the Creator");
					p.sendMessage("\u00A7a/" + CommandLabel + " setSpawn <mapNumber>\u00A7f  - Set the map spawn");
					p.sendMessage("\u00A7a/" + CommandLabel
							+ " toggleWaterRespawn <mapNumber>\u00A7f  - Toggles Water repsawn on this Map");
					p.sendMessage("\u00A7a/" + CommandLabel
							+ " toggleLavaRespawn <mapNumber>\u00A7f  - Toggles Lava Respawn on this Map");
				}
				if (p.hasPermission("parkour.admin")) {
					p.sendMessage("\u00A72/" + CommandLabel + " setLobby\u00A7f  - Set the lobby spawn");
					p.sendMessage("\u00A72/" + CommandLabel + " toggle <mapNumber>\u00A7f  - toggle ON/OFF a parkour");
					p.sendMessage("\u00A72/" + CommandLabel
							+ " resetScores <mapNumber>\u00A7f  - Reset All scores for a map");
					p.sendMessage("\u00A72/" + CommandLabel
							+ " pReset <Player> [<mapNumber> / all]\u00A7f  - Reset scores for a player");
				}
				p.sendMessage("/\u00A77" + CommandLabel + " MapList\u00A7f  - Show all the maps");
				p.sendMessage("/\u00A77" + CommandLabel + " best <MapNumber>\u00A7f  - Show the best score of a map");
				p.sendMessage("/\u00A77" + CommandLabel + " leave\u00A7f - Left the map");
				p.sendMessage("/\u00A77" + CommandLabel + " join <mapNumber>\u00A7f - Join a map");
			} else {
				if (args[0].equalsIgnoreCase("join")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {

								if (Parkour.containsKey(p.getName())) {
									Parkour.remove(p.getName());
								}

								FileConfiguration cfg = getConfig();

								if (cfg.contains("Parkour.map" + args[1] + ".spawn")) {
									Location loc = new Location(getServer().getWorld(
											getConfig().get("Parkour.map" + args[1] + ".world").toString()),
											cfg.getDouble("Parkour.map" + args[1] + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + args[1] + ".spawn.posY"),
											cfg.getDouble("Parkour.map" + args[1] + ".spawn.posZ"));
									loc.setPitch((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + args[1] + ".spawn.posYaw"));

									p.teleport(loc);
								} else {
									p.sendMessage("\u00A7cMap spawn is not set !");
								}

							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("You must specify the map number !");
					}
				} else if (args[0].equalsIgnoreCase("leave")) {
					if (Parkour.containsKey(p.getName())) {
						p.sendMessage("\u00A7aYou leave the parkour !");
						Parkour.remove(p.getName());
						if (lobby != null) {
							p.teleport(lobby);
						}

					} else {
						p.sendMessage("\u00A7cYou are not in a parkour !");
					}
				}

				else if (args[0].equalsIgnoreCase("remove")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								String mapNumber = args[1].toString();
								getConfig().getConfigurationSection("Parkour").set("map" + mapNumber, null);
								getConfig().set("Parkour.mapsNombre",
										Integer.valueOf(getConfig().getInt("Parkour.mapsNombre") - 1));
								saveConfig();
								p.sendMessage("\u00A72map" + mapNumber + "\u00A7f is now deleted !");

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();) {
									String key = it.next();
									String[] KeySplit = key.split(":");
									if (KeySplit[0].equals(args[1])) {
										it.remove();
									}
								}
								saveScore();
								intCheckpointsLoc();
								intMaps();
								loadToggleMap();
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("\u00A7cYou must specify the map number !");
					}
				}

				else if (args[0].equalsIgnoreCase("toggle") && p.hasPermission("parkour.admin")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								if (getConfig().getBoolean("Parkour.map" + args[1] + ".toggle")) {
									p.sendMessage("Map toggle to \u00A74OFF");
									getConfig().set("Parkour.map" + args[1] + ".toggle", false);
									saveConfig();
								} else {
									p.sendMessage("Map toggle to \u00A7aON");
									getConfig().set("Parkour.map" + args[1] + ".toggle", true);
									saveConfig();
								}
								loadToggleMap();
							} else {
								p.sendMessage("not valid map");
							}
						} else {
							p.sendMessage("Not Valid number");
						}
					} else {
						p.sendMessage("\u00A7cYou must specify the map number !");
					}
				}

				else if (args[0].equalsIgnoreCase("new")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 3) {
						if (args[1] != null && args[2] != null) {
							if (!newMap) {
								newMapPlayerEditor = p.getName();
								newMap = true;
								p.sendMessage("MapEditor \u00A7aON \u00A77(Use the stick and right click on all checkpoint in order then type /pk done)");
								CheckpointNumber = 1;
								newMapName = args[1];
								newMapCreator = args[2];
								NewMapNumber = (maxMapNumber() + 1);
							} else {
								p.sendMessage("\u00A7cA player is already using the MapEditor (" + newMapPlayerEditor
										+ ") ! You must wait a bit !");
							}
						} else {
							p.sendMessage("\u00A7cCorrect usage : /pk new <mapName> <mapCreator>");
						}
					} else {
						p.sendMessage("\u00A7cCorrect usage : /pk new <mapName> <mapCreator>");
					}
				}

				else if (args[0].equalsIgnoreCase("done")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (!newMap) {
						p.sendMessage("\u00A7cMapEditor is not ON !");
					} else {
						if (p.getName().equalsIgnoreCase(newMapPlayerEditor)) {
							if (CheckpointNumber >= 3) {
								p.sendMessage("\u00A7a" + newMapName + "(map" + NewMapNumber
										+ ") created ! \u00A7fMapEditor \u00A74OFF");

								FileConfiguration cfg = getConfig();
								cfg.set("Parkour.mapsNombre", (getConfig().getInt("Parkour.mapsNombre")) + 1);
								cfg.set("Parkour.map" + NewMapNumber + ".world", p.getWorld().getName());
								cfg.set("Parkour.map" + NewMapNumber + ".mapName", newMapName);
								cfg.set("Parkour.map" + NewMapNumber + ".mapCreator", newMapCreator);
								cfg.set("Parkour.map" + NewMapNumber + ".nombreCp", (CheckpointNumber - 1));
								cfg.set("Parkour.map" + NewMapNumber + ".toggle", true);
								cfg.set("Parkour.map" + NewMapNumber + ".waterrespawn", false);
								cfg.set("Parkour.map" + NewMapNumber + ".lavarespawn", false);

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
							} else {
								p.sendMessage("\u00A7cA parkour need at least 3 checkpoints ! \u00A7fMapEditor \u00A74OFF");
								newMapPlayerEditor = null;
								newMapName = null;
								newMapCreator = null;
								newMapCheckpoints.clear();
								CheckpointNumber = 0;
								NewMapNumber = 0;
								newMap = false;
							}

						} else {
							p.sendMessage("\u00A7cA player is already using the MapEditor (" + newMapPlayerEditor
									+ ") ! You must wait a bit !");
						}
					}
				}

				else if (args[0].equalsIgnoreCase("changeMapName")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 3) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								getConfig().set("Parkour.map" + args[1] + ".mapName", args[2]);
								saveConfig();
								p.sendMessage("\u00A7aMap name set to '\u00A7b" + args[2] + "'\u00A7a for map"
										+ args[1]);
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("\u00A7cCorrect usage : /pk changeMapName <mapNumber> <newMapName>");
					}
				}

				else if (args[0].equalsIgnoreCase("changeMapCreator")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 3) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								getConfig().set("Parkour.map" + args[1] + ".mapCreator", args[2]);
								saveConfig();
								p.sendMessage("\u00A7aCreator set to '\u00A7b" + args[2] + "'\u00A7a for map" + args[1]);
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("\u00A7cCorrect usage /pk ... !");
					}
				}

				else if (args[0].equalsIgnoreCase("setspawn")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								cfg.set("Parkour.map" + mapNumber + ".spawn.posX", p.getLocation().getX());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posY", p.getLocation().getY());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posZ", p.getLocation().getZ());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posPitch", p.getLocation().getPitch());
								cfg.set("Parkour.map" + mapNumber + ".spawn.posYaw", p.getLocation().getYaw());
								saveConfig();
								p.sendMessage("\u00A7aParkour spawn set to \u00A72map" + mapNumber + "\u00A7f !");
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}

					} else {
						p.sendMessage("You don't specify the map !");
					}
				} else if (args[0].equalsIgnoreCase("toggleWaterRespawn")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".waterrespawn");
								cfg.set("Parkour.map" + mapNumber + ".waterrespawn", isActive);
								saveConfig();
								if (isActive) p.sendMessage("\u00A7aWaterrespawn is now ON for map \u00A72map"
										+ mapNumber + "\u00A7f !");
								else p.sendMessage("\u00A7aWaterrespawn is now OFF for map \u00A72map" + mapNumber
										+ "\u00A7f !");
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}

					} else {
						p.sendMessage("You don't specify the map !");
					}
				} else if (args[0].equalsIgnoreCase("toggleLavaRespawn")
						&& (p.hasPermission("parkour.admin") || p.hasPermission("parkour.mapeditor"))) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								FileConfiguration cfg = getConfig();
								String mapNumber = args[1].toString();
								boolean isActive = !cfg.getBoolean("Parkour.map" + mapNumber + ".lavarespawn");
								cfg.set("Parkour.map" + mapNumber + ".lavarespawn", isActive);
								saveConfig();
								if (isActive) p.sendMessage("\u00A7aLavarespawn is now ON for map \u00A72map"
										+ mapNumber + "\u00A7f !");
								else p.sendMessage("\u00A7aLavarespawn is now OFF for map \u00A72map" + mapNumber
										+ "\u00A7f !");
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}

					} else {
						p.sendMessage("You don't specify the map !");
					}
				} else if (args[0].equalsIgnoreCase("setLobby") && p.hasPermission("parkour.admin")) {
					FileConfiguration cfg = getConfig();
					cfg.set("Lobby.world", p.getWorld().getName());
					cfg.set("Lobby.posX", p.getLocation().getX());
					cfg.set("Lobby.posY", p.getLocation().getY());
					cfg.set("Lobby.posZ", p.getLocation().getZ());
					cfg.set("Lobby.posPitch", p.getLocation().getPitch());
					cfg.set("Lobby.posYaw", p.getLocation().getYaw());
					saveConfig();
					p.sendMessage("\u00A7aLobby set !");
					loadLobby();
				}

				else if (args[0].equalsIgnoreCase("pReset") && p.hasPermission("parkour.admin")) {
					if (args.length == 3) {
						boolean DeleteOnAllMaps = false;
						if (args[2].equalsIgnoreCase("all")) {
							DeleteOnAllMaps = true;
						}

						if (isNumber(args[2]) || DeleteOnAllMaps) {
							if ((isNumber(args[2]) && maps.contains(toInt(args[2]))) || DeleteOnAllMaps) {
								boolean PlayerFound = false;
								String player = args[1];
								String mapNumber = args[2];

								Iterator<String> it = Records.keySet().iterator();

								while (it.hasNext()) {
									String key = it.next();
									String[] KeySplit = key.split(":");

									System.out.println("Key: " + key);

									if (KeySplit[1].equalsIgnoreCase(player)) {
										if (DeleteOnAllMaps) {
											it.remove();
											PlayerFound = true;
										} else if (Integer.parseInt(KeySplit[0]) == Integer.parseInt(mapNumber)) {
											PlayerFound = true;
											it.remove();
										}
									}
								}
								saveScore();

								if (!PlayerFound) {
									p.sendMessage("\u00A7cPlayer not found in this scoreboard !");
									return true;
								}

								if (DeleteOnAllMaps) {
									p.sendMessage("\u00A7aScores reset for player " + player + " on all maps !");
								} else {
									p.sendMessage("\u00A7aScores reset for player " + player + " on map " + mapNumber
											+ " !");
								}

								loadScore();
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("\u00A7cYou must specify the player/mapNumber !");
					}
				}

				else if (args[0].equalsIgnoreCase("MapList")) {
					p.sendMessage("\u00A78---------=[\u00A7a Parkour Map List \u00A78]=---------");

					for (int i : maps) {
						String mapNumber = "" + i;
						if (maps.contains(toInt(mapNumber))) {
							String mode = "\u00A74OFF";
							if (toggleParkour.get(i)) {
								mode = "\u00A7aON";
							}
							p.sendMessage(mode + " \u00A7f| \u00A7b " + getMapName(i) + "\u00A77 (\u00A72map" + i
									+ "\u00A77) \u00A77(" + getCfgTotalCheckpoints(i) + " Checkpoints) \u00A7aby "
									+ getMapCreator(i));
						}
					}
				}

				else if (args[0].equalsIgnoreCase("best")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								displayHighscores(toInt(args[1]), p);
							} else {
								p.sendMessage("\u00A7cUnknown map number !");
							}
						} else {
							p.sendMessage("\u00A7cThis is not a valid mapNumber !");
						}
					} else {
						p.sendMessage("\u00A7cYou don't specify the map !");
					}
				}

				else if (args[0].equalsIgnoreCase("resetScores") && p.hasPermission("parkour.admin")) {
					if (args.length == 2) {
						if (isNumber(args[1])) {
							if (maps.contains(toInt(args[1]))) {
								int mapNumber = Integer.parseInt(args[1]);
								p.sendMessage("\u00A72Scores reset for map" + mapNumber + "!");

								for (Iterator<String> it = Records.keySet().iterator(); it.hasNext();) {
									String key = it.next();
									String[] pName = key.split(":");
									int pMap = Integer.parseInt(pName[0]);
									if (pMap == mapNumber) {
										it.remove();
									}
								}
								saveScore();
							} else {
								p.sendMessage("\u00A7cIt is not a valid mapNumber !");
							}
						} else {
							p.sendMessage("\u00A7cIt is not a valid number !");
						}
					} else {
						p.sendMessage("\u00A7cYou must specify the map number !");
					}
				}

				else {

					p.sendMessage("\u00A7cUnknown command arguments !");

				}
			}
		}
		return true;
	}

	// //////////////////////////////
	// _____ _
	// | ___| | |
	// | |____ _____ _ __ | |_ ___
	// | __\ \ / / _ \ '_ \| __/ __|
	// | |___\ V / __/ | | | |_\__ \
	// \____/ \_/ \___|_| |_|\__|___/
	// //////////////////////////////

	@EventHandler
	public void onDisco(PlayerQuitEvent e) {
		if (Parkour.containsKey(e.getPlayer().getName())) {
			Parkour.remove(e.getPlayer().getName());
		}
		if (rewardPlayersCooldown.containsKey(e.getPlayer().getName())) {
			rewardPlayersCooldown.remove(e.getPlayer().getName());
		}
		if (e.getPlayer().getName().equals(newMapPlayerEditor)) {
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
	public void onPlayerDmg(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (Parkour.containsKey(p.getName()) && InvincibleWhileParkour) {
				e.setCancelled(true);
				p.setFireTicks(0);
			}

		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).equalsIgnoreCase("[mwParkour2]") && !e.getPlayer().hasPermission("parkour.mapeditor")) {
			e.setCancelled(true);
		}

		if (e.getPlayer().hasPermission("parkour.mapeditor")) {
			// 15 char max par lines (on sign)

			if (e.getLine(0).equalsIgnoreCase("[pk2]")) {
				if (e.getLine(1).equalsIgnoreCase("leave")) {
					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "leave");
					e.setLine(2, "");
					e.setLine(3, "");
				}
				if (e.getLine(1).equalsIgnoreCase("joinlastmap")) {
					int mapNumber = maxMapNumber();

					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "joinLastMap");
					e.setLine(2, "\u00A7b" + getMapName(mapNumber));
					e.setLine(3, "(map" + mapNumber + ")");
				}
				if (e.getLine(1).equalsIgnoreCase("join")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[mwParkour2]");
							e.setLine(1, "join");
							e.setLine(2, "\u00A7b" + getMapName(MapNumber));
							e.setLine(3, "(map" + MapNumber + ")");
						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("lastbestscores")) {
					int mapNumber = maxMapNumber();

					e.setLine(0, "[mwParkour2]");
					e.setLine(1, "lastBestScores");
					e.setLine(2, "\u00A7b" + getMapName(mapNumber));
					e.setLine(3, "(map" + mapNumber + ")");
				}
				if (e.getLine(1).equalsIgnoreCase("infos")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "Parkour #" + MapNumber);
							e.setLine(1, "---------------");
							e.setLine(2, "\u00A7b" + getMapName(MapNumber));
							e.setLine(3, getMapCreator(MapNumber));
						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
				if (e.getLine(1).equalsIgnoreCase("best")) {
					if (isNumber(e.getLine(2))) {
						if (maps.contains(toInt(e.getLine(2)))) {
							int MapNumber = Integer.parseInt(e.getLine(2));

							e.setLine(0, "[mwParkour2]");
							e.setLine(1, "bestScores");
							e.setLine(2, "\u00A7b" + getMapName(MapNumber));
							e.setLine(3, "(map" + MapNumber + ")");
						} else {
							e.setCancelled(true);
						}
					} else {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onIntaract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Sign s = (Sign) e.getClickedBlock().getState();

				if (s.getLine(0).equals("[mwParkour2]")) {

					if (s.getLine(1).equals("leave")) {
						if (Parkour.containsKey(e.getPlayer().getName())) {
							e.getPlayer().sendMessage("\u00A7aYou leave the parkour !");
							Parkour.remove(e.getPlayer().getName());

						}
						if (lobby != null) {
							e.getPlayer().teleport(lobby);
						}
					}
					if (s.getLine(1).equals("joinLastMap")) {
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber)) {
							if (maps.contains(toInt(mapNumber))) {
								if (toInt(mapNumber) == maxMapNumber()) {
									Player p = e.getPlayer();
									FileConfiguration cfg = getConfig();

									if (cfg.contains("Parkour.map" + mapNumber + ".spawn")) {
										Location loc = new Location(getServer().getWorld(
												getConfig().getString("Parkour.map" + mapNumber + ".world")),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posX"),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posY"),
												cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posZ"));

										loc.setPitch((float) cfg.getDouble("Parkour.map" + mapNumber
												+ ".spawn.posPitch"));
										loc.setYaw((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posYaw"));

										if (!loc.getChunk().isLoaded()) {
											loc.getChunk().load(true);
										}

										p.teleport(loc);
									} else {
										p.sendMessage("\u00A7cMap spawn is not set !");
									}
								} else {
									s.setLine(0, "[mwParkour2]");
									s.setLine(1, "joinLastMap");
									s.setLine(2, "\u00A7b" + getMapName(maxMapNumber()));
									s.setLine(3, "(map" + maxMapNumber() + ")");
									s.update();
								}
							} else {
								s.setLine(0, "[mwParkour2]");
								s.setLine(1, "joinLastMap");
								s.setLine(2, "\u00A7b" + getMapName(maxMapNumber()));
								s.setLine(3, "(map" + maxMapNumber() + ")");
								s.update();
							}
						}
					}
					if (s.getLine(1).equals("lastBestScores")) {
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber)) {
							if (maps.contains(toInt(mapNumber))) {
								if (toInt(mapNumber) == maxMapNumber()) {
									displayHighscores(toInt(mapNumber), e.getPlayer());
								} else {
									s.setLine(0, "[mwParkour2]");
									s.setLine(1, "lastBestScores");
									s.setLine(2, "\u00A7b" + getMapName(maxMapNumber()));
									s.setLine(3, "(map" + maxMapNumber() + ")");
									s.update();
								}
							} else {
								s.setLine(0, "[mwParkour2]");
								s.setLine(1, "lastBestScores");
								s.setLine(2, "\u00A7b" + getMapName(maxMapNumber()));
								s.setLine(3, "(map" + maxMapNumber() + ")");
								s.update();
							}
						}
					}
					if (s.getLine(1).equals("join")) {
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber)) {
							if (maps.contains(toInt(mapNumber))) {
								Player p = e.getPlayer();

								if (Parkour.containsKey(p.getName())) {
									Parkour.remove(p.getName());
								}

								FileConfiguration cfg = getConfig();

								if (cfg.contains("Parkour.map" + mapNumber + ".spawn")) {
									Location loc = new Location(getServer().getWorld(
											getConfig().getString("Parkour.map" + mapNumber + ".world")),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posX"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posY"),
											cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posZ"));

									loc.setPitch((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posPitch"));
									loc.setYaw((float) cfg.getDouble("Parkour.map" + mapNumber + ".spawn.posYaw"));

									if (!loc.getChunk().isLoaded()) {
										loc.getChunk().load(true);
									}

									p.teleport(loc);
								} else {
									p.sendMessage("\u00A7cMap spawn is not set !");
								}
							} else {
								e.getPlayer().sendMessage("\u00A7cThis map no longer exists !");
							}
						}
					}

					if (s.getLine(1).equals("bestScores")) {
						String mapNumber = s.getLine(3).substring(4, s.getLine(3).length() - 1);

						if (isNumber(mapNumber)) {
							if (maps.contains(toInt(mapNumber))) {
								displayHighscores(toInt(mapNumber), e.getPlayer());
							} else {
								e.getPlayer().sendMessage("\u00A7cThis map no longer exists !");
							}
						}
					}
				}
			}
		}

		if (newMap)// map creation
		{
			if (e.getPlayer().getName().equals(newMapPlayerEditor) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Player p = e.getPlayer();

				if (p.getItemInHand().getTypeId() == 280 && e.getClickedBlock().getTypeId() == 70) {
					if (!cLoc.containsKey(e.getClickedBlock().getLocation())) {
						Location bLoc = e.getClickedBlock().getLocation();

						if (newMapCheckpoints.contains(bLoc)) {
							p.sendMessage("\u00A7cThis checkpoint is alredy used for this map !");
						} else {
							FileConfiguration cfg = getConfig();

							p.sendMessage("\u00A78Checkpoint " + CheckpointNumber + " set on new map " + NewMapNumber);

							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posX", bLoc.getX());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posY", bLoc.getY());
							cfg.set("Parkour.map" + NewMapNumber + ".cp." + CheckpointNumber + ".posZ", bLoc.getZ());

							saveConfig();
							newMapCheckpoints.add(bLoc);
							CheckpointNumber++;

						}
					} else {
						p.sendMessage("\u00A7cThis checkpoint is alredy used for another map !");
					}
				} else {
					p.sendMessage("\u00A7cUse a stick to place checkpoints (Right click on stone pressure plate)");
				}
			}
		}

		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
				&& e.getPlayer().getItemInHand().getTypeId() == 280) // player use stick to teleport to last last
																		// checkpoint
		{
			if (Parkour.containsKey(e.getPlayer().getName())) teleportLastCheckpoint(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {

		Player p = e.getPlayer();

		if (((int) e.getFrom().getX() != (int) e.getTo().getX())
				|| ((int) e.getFrom().getY() != (int) e.getTo().getY())
				|| ((int) e.getFrom().getZ() != (int) e.getTo().getZ())) {
			if (e.getTo().getBlock().getTypeId() == 70) {
				int x = (int) e.getTo().getBlock().getX();
				int y = (int) e.getTo().getBlock().getY();
				int z = (int) e.getTo().getBlock().getZ();
				Location bLoc = new Location(e.getTo().getWorld(), x, y, z);

				if (cLoc.containsKey(bLoc)) {

					int Checkpoint = getCheckpoint(cLoc.get(bLoc).toString());

					if (!p.hasPermission("parkour.use")) {
						p.sendMessage("\u00A7cYou don't have permission to do this parkour !");
						return;
					}

					if (!toggleParkour.get(getCpMapNumber(cLoc.get(bLoc).toString()))) {
						p.sendMessage("This parkour is \u00A74OFF");
						return;
					}

					if (!Parkour.containsKey(p.getName())) {

						if (Checkpoint == 1) {
							int Map = getCpMapNumber(cLoc.get(bLoc).toString());

							Parkour.put(
									p.getName(),
									(getCpMapNumber(cLoc.get(bLoc).toString()) + "_"
											+ Long.valueOf(System.currentTimeMillis()) + "_1"));
							p.sendMessage("\u00A7aYou have started the parkour on '\u00A7b" + getMapName(Map)
									+ "'\u00A7a by \u00A72" + getMapCreator(Map) + " \u00A77(\u00A7amap" + Map
									+ "\u00A77)");

							if (CheckpointEffect) {
								p.playEffect(bLoc, Effect.POTION_BREAK, 2);
							}
							if (removePotionsEffectsOnParkour) {
								for (PotionEffect effect : p.getActivePotionEffects()) {
									p.removePotionEffect(effect.getType());
								}
							}
							if (FullHunger) {
								p.setFoodLevel(20);
							}
						} else {
							p.sendMessage("\u00A7cYou must start at the checkpoint 1 !");
						}
					} else {
						int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()).toString());
						int CpMap = getCpMapNumber(cLoc.get(bLoc).toString());
						int Map = getPlMapNumber(Parkour.get(p.getName()).toString());
						int TotalCheckpoints = getCfgTotalCheckpoints(Map);

						if (CpMap != Map) {
							if (Checkpoint == 1) {
								p.sendMessage("\u00A7aYou have started the parkour on '\u00A7b" + getMapName(Map)
										+ "'\u00A7a by \u00A72" + getMapCreator(Map) + " \u00A77(\u00A7amap" + CpMap
										+ "\u00A77)");
								Parkour.put(
										p.getName(),
										(getCpMapNumber(cLoc.get(bLoc).toString()) + "_"
												+ Long.valueOf(System.currentTimeMillis()) + "_1"));

								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger) {
									p.setFoodLevel(20);
								}

							} else {
								p.sendMessage("\u00A7cYou are not in the parkour !");

							}
						} else {

							if (Checkpoint == 1) {
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}
								if (removePotionsEffectsOnParkour) {
									for (PotionEffect effect : p.getActivePotionEffects()) {
										p.removePotionEffect(effect.getType());
									}
								}
								if (FullHunger) {
									p.setFoodLevel(20);
								}

								p.sendMessage("\u00A7aYou have restarted your time !");
								setPlTime(p.getName(), Long.valueOf(System.currentTimeMillis()));
								setPlCheckpoint(p.getName(), 1);

							} else if ((Checkpoint == TotalCheckpoints) && (PlCheckpoint == (Checkpoint - 1))) {
								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								long totalTime = System.currentTimeMillis()
										- Long.valueOf(getPlTime(Parkour.get(p.getName()).toString()));
								Parkour.remove(p.getName());

								if (!Records.containsKey(Map + ":" + p.getName())) {

									p.sendMessage("\u00A7bYou finished this parkour for the first time in "
											+ convertTime(totalTime));
									Records.put(Map + ":" + p.getName(), totalTime);
									saveScore();

									if (BroadcastMessage) {
										getServer().broadcastMessage(
												ChatColor.translateAlternateColorCodes('&', BroadcastMsg)
														.replaceAll("PLAYER", p.getName())
														.replaceAll("MAPNAME", getMapName(Map)));
									}
									giveReward(p, Map);

								} else {

									if (Records.get(Map + ":" + p.getName()) >= totalTime) {

										p.sendMessage("\u00A72You beat your old score !");
										p.sendMessage("\u00A7aYou finished this parkour in " + convertTime(totalTime));
										Records.put(Map + ":" + p.getName(), totalTime);
										saveScore();

										if (BroadcastMessage) {
											getServer().broadcastMessage(
													ChatColor.translateAlternateColorCodes('&', BroadcastMsg)
															.replaceAll("PLAYER", p.getName())
															.replaceAll("MAPNAME", getMapName(Map)));
										}
										giveReward(p, Map);

									} else {
										p.sendMessage("\u00A74You don't beat your old score");
										p.sendMessage("\u00A7aYou finished this parkour in " + convertTime(totalTime));

										if (!rewardIfBetterScore) {
											giveReward(p, Map);
										}
									}

								}

								final String pl = p.getName();

								if (lobby != null) {
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										public void run() {
											getServer().getPlayer(pl).teleport(lobby);
										}
									}, 5L);
								}
							} else if (PlCheckpoint == (Checkpoint - 1)) {

								if (CheckpointEffect) {
									p.playEffect(bLoc, Effect.POTION_BREAK, 2);
								}

								setPlCheckpoint(p.getName(), Checkpoint);
								p.sendMessage("\u00A7bCheckpoint " + (Checkpoint - 1) + "/" + (TotalCheckpoints - 2)
										+ " reached !");

							} else if (Checkpoint <= PlCheckpoint) {
								p.sendMessage("\u00A7cYou already reached this checkpoint !");

							} else if (Checkpoint > PlCheckpoint) {
								p.sendMessage("\u00A7cYou forgot to pass the last checkpoint !");

							}
						}
					}
				}
			}
			if (Parkour.containsKey(p.getName())) {
				int Map = getPlMapNumber(Parkour.get(p.getName()).toString());
				if ((e.getTo().getBlock().getType() == Material.WATER || e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {
					if (getConfig().getBoolean("Parkour.map" + Map + ".waterrespawn"))
						teleportLastCheckpoint(e.getPlayer());
				}
				if ((e.getTo().getBlock().getType() == Material.LAVA || e.getTo().getBlock().getType() == Material.STATIONARY_LAVA)) {
					if (getConfig().getBoolean("Parkour.map" + Map + ".lavarespawn"))
						teleportLastCheckpoint(e.getPlayer());
				}
			}
		}
	}

	// /////////////////////////////////////////////
	// ______ _ _
	// | ___| | | (_)
	// | |_ ___ _ __ ___| |_ _ ___ _ __ ___
	// | _/ _ \| '_ \ / __| __| |/ _ \| '_ \/ __|
	// | || (_) | | | | (__| |_| | (_) | | | \__ \
	// \_| \___/|_| |_|\___|\__|_|\___/|_| |_|___/
	// /////////////////////////////////////////////

	private void teleportLastCheckpoint(Player p) {
		FileConfiguration cfg = getConfig();
		Location lastCheckpoint = null;

		int MapNumber = getPlMapNumber(Parkour.get(p.getName()));
		int PlCheckpoint = getPlCheckpoint(Parkour.get(p.getName()));

		if (PlCheckpoint == 1 || !LastCheckpointTeleport) // Teleport to map spawn
		{
			if (cfg.contains("Parkour.map" + MapNumber + ".spawn")) {
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posX"), cfg.getDouble("Parkour.map"
								+ MapNumber + ".spawn.posY"), cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posZ"));

				lastCheckpoint.setPitch((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posPitch"));
				lastCheckpoint.setYaw((float) cfg.getDouble("Parkour.map" + MapNumber + ".spawn.posYaw"));

				p.teleport(lastCheckpoint);
			} else {
				lastCheckpoint = new Location(
						getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posX") + 0.5, cfg.getDouble("Parkour.map"
								+ MapNumber + ".cp.1.posY"),
						cfg.getDouble("Parkour.map" + MapNumber + ".cp.1.posZ") + 0.5);

				lastCheckpoint.setPitch(p.getLocation().getPitch());
				lastCheckpoint.setYaw(p.getLocation().getYaw());
				p.teleport(lastCheckpoint);
			}
		} else {
			lastCheckpoint = new Location(getServer().getWorld(cfg.getString("Parkour.map" + MapNumber + ".world")),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posX") + 0.5,
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posY"),
					cfg.getDouble("Parkour.map" + MapNumber + ".cp." + PlCheckpoint + ".posZ") + 0.5);

			lastCheckpoint.setPitch(p.getLocation().getPitch());
			lastCheckpoint.setYaw(p.getLocation().getYaw());
			p.teleport(lastCheckpoint);
		}
	}

	private void setPlCheckpoint(String p, int Cp) {
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String CpFinal = Splitter[0] + "_" + Splitter[1] + "_" + Cp;
		Parkour.put(p, CpFinal);
	}

	private void setPlTime(String p, Long Time) {
		String HashTableSrc = Parkour.get(p);
		String[] Splitter = HashTableSrc.split("_");
		String TimeFinal = Splitter[0] + "_" + Time + "_" + Splitter[2];
		Parkour.put(p, TimeFinal);
	}

	private Long getPlTime(String HashTable) {
		String[] Splitter = HashTable.split("_");
		Long Time = Long.valueOf(Splitter[1]);
		return Time;
	}

	private int getPlCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int Cp = Integer.parseInt(Splitter[2]);
		return Cp;
	}

	private int getPlMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int mapNumber = Integer.parseInt(Splitter[0]);
		return mapNumber;
	}

	private int getCpMapNumber(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[0]);
		return CpMap;
	}

	private int getCheckpoint(String HashTable) {
		String[] Splitter = HashTable.split("_");
		int CpMap = Integer.parseInt(Splitter[1]);
		return CpMap;
	}

	private int getCfgTotalCheckpoints(int mapNumber) {
		return getConfig().getInt("Parkour.map" + mapNumber + ".nombreCp");
	}

	private boolean mapExist(String MapNumber) {
		if (getConfig().getInt("Parkour.map" + MapNumber + ".nombreCp") != 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isNumber(String number) {
		try {
			Integer.parseInt(number);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void intCheckpointsLoc() {
		cLoc.clear();
		FileConfiguration cfg = getConfig();
		for (int mapNumber : maps) {
			for (int i = cfg.getInt("Parkour.map" + mapNumber + ".nombreCp"); i >= 1; i--) {
				Location loc = new Location(getServer().getWorld(cfg.getString("Parkour.map" + mapNumber + ".world")),
						cfg.getInt("Parkour.map" + mapNumber + ".cp." + i + ".posX"), cfg.getInt("Parkour.map"
								+ mapNumber + ".cp." + i + ".posY"), cfg.getInt("Parkour.map" + mapNumber + ".cp." + i
								+ ".posZ"));
				String HashTable = mapNumber + "_" + i;
				cLoc.put(loc, HashTable);
			}
		}
	}

	private void intMaps() {
		maps.clear();
		String mapList = getConfig().getConfigurationSection("Parkour").getKeys(false).toString().replaceAll("\\s", "")
				.replace("[", "").replace("]", "");
		String[] mapsSplit = mapList.split(",");
		for (int i = getConfig().getInt("Parkour.mapsNombre"); i >= 0; i--) {
			if (mapExist(mapsSplit[i].substring(3))) {
				maps.add(Integer.parseInt(mapsSplit[i].substring(3)));
			}
		}
		Collections.sort(maps);
	}

	private void loadToggleMap() {
		toggleParkour.clear();
		for (int mapNumber : maps) {
			if (getConfig().contains("Parkour.map" + mapNumber + ".toggle")) {
				toggleParkour.put(mapNumber, getConfig().getBoolean("Parkour.map" + mapNumber + ".toggle"));
			}
		}
	}

	private void LoadCfg() {
		FileConfiguration cfg = getConfig();

		// Options
		cfg.addDefault("options.InvincibleWhileParkour", true);
		cfg.addDefault("options.RespawnOnLava", true);
		cfg.addDefault("options.RespawnOnWater", true);
		cfg.addDefault("options.CheckpointEffect", true);
		cfg.addDefault("options.removePotionsEffectsOnParkour", false);
		cfg.addDefault("options.setFullHungerOnParkour", false);
		cfg.addDefault("options.LastCheckpointTeleport", true);

		// Rewards
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
		cfg.addDefault("options.BroadcastOnRecord.message",
				"&emwParkour2&f>&8 New record for &7PLAYER &8on map MAPNAME !");

		cfg.addDefault("Parkour.mapsNombre", 0);
		cfg.options().copyDefaults(true);
		saveConfig();

		removePotionsEffectsOnParkour = cfg.getBoolean("options.removePotionsEffectsOnParkour");
		InvincibleWhileParkour = cfg.getBoolean("options.InvincibleWhileParkour");
		CheckpointEffect = cfg.getBoolean("options.CheckpointEffect");
		BroadcastMessage = cfg.getBoolean("options.BroadcastOnRecord.enable");
		FullHunger = cfg.getBoolean("options.BroadcastOnRecord.enable");
		LastCheckpointTeleport = cfg.getBoolean("options.LastCheckpointTeleport");

		rewardIfBetterScore = cfg.getBoolean("rewards.rewardIfBetterScore");
		rewardEnable = cfg.getBoolean("rewards.enable");

		if (BroadcastMessage) {
			BroadcastMsg = cfg.getString("options.BroadcastOnRecord.message");
		}

	}

	private void loadLobby() {
		FileConfiguration cfg = getConfig();

		if (cfg.contains("Lobby")) {
			lobby = null;
			Location loc = new Location(getServer().getWorld(cfg.getString("Lobby.world")),
					cfg.getDouble("Lobby.posX"), cfg.getDouble("Lobby.posY"), cfg.getDouble("Lobby.posZ"));
			loc.setPitch((float) cfg.getDouble("Lobby.posPitch"));
			loc.setYaw((float) cfg.getDouble("Lobby.posYaw"));
			lobby = loc;
		}
	}

	private int maxMapNumber() {
		return getConfig().getInt("Parkour.mapsNombre");
	}

	private void saveScore() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream((path))));
			oos.writeObject((Object) Records);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadScore() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
			Records.clear();
			@SuppressWarnings("unchecked")
			HashMap<String, Long> scoreMap = (HashMap<String, Long>) ois.readObject();
			Records = scoreMap;
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int toInt(String msg) {
		return Integer.parseInt(msg);
	}

	private void debug(String msg) {
		System.out.println("[ mwParkourDebug ] " + msg);
	}

	private void giveReward(Player p, int mapNumber) {
		if (rewardEnable) {
			FileConfiguration cfg = getConfig();

			boolean rewardMoneyEnable = cfg.getBoolean("rewards.money.enable");
			boolean rewardCommandEnable = cfg.getBoolean("rewards.command.enable");

			String rewardMoneyMsg = cfg.getString("rewards.money.message");
			String rewardCommandMsg = cfg.getString("rewards.command.message");

			String rewardCmd = cfg.getString("rewards.command.cmd");
			int rewardMoney = cfg.getInt("rewards.money.amount");

			int rewardCooldown = cfg.getInt("rewards.cooldown");
			String rewardCooldownMsg = cfg.getString("rewards.cooldownMessage");

			if (!rewardPlayersCooldown.containsKey(p.getName())) {
				if (rewardMoneyEnable && rewardMoney > 0) {
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					if (vault) economy.depositPlayer(p.getName(), rewardMoney);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll("MONEYAMOUNT",
							"" + rewardMoney));
				}
				if (rewardCommandEnable) {
					rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

					getServer().dispatchCommand(getServer().getConsoleSender(),
							rewardCmd.replaceAll("PLAYER", p.getName()));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
				}
			} else {
				if (System.currentTimeMillis() - rewardPlayersCooldown.get(p.getName()) >= rewardCooldown * 1000) {
					if (rewardMoneyEnable && rewardMoney > 0) {
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						if (vault) economy.depositPlayer(p.getName(), rewardMoney);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardMoneyMsg).replaceAll(
								"MONEYAMOUNT", "" + rewardMoney));
					}
					if (rewardCommandEnable) {
						rewardPlayersCooldown.put(p.getName(), System.currentTimeMillis());

						getServer().dispatchCommand(getServer().getConsoleSender(),
								rewardCmd.replaceAll("PLAYER", p.getName()));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', rewardCommandMsg));
					}
				} else {
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

					if (secs < 10) {
						secsS = "0" + secsS;
					}
					if (mins < 10) {
						minsS = "0" + minsS;
					}
					if (hours < 10) {
						hoursS = "0" + hoursS;
					}

					p.sendMessage(rewardCooldownMsg.replaceAll("TIME", hoursS + "h:" + minsS + "m:" + secsS + "s"));
				}
			}
		}
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	// Public API

	/**
	 * Returns all Records on the given Map - <Playername, Time>
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, Long> getRecords(int map) {
		Map<String, Long> records = new HashMap<String, Long>();
		for (String m : Records.keySet()) {
			String[] s = m.split(":");
			if (toInt(s[0]) == map) {
				records.put(s[1], Records.get(m));
			}
		}
		return sortByValue(records);
	}

	/**
	 * Converts a time in ms into a good read readable format
	 * 
	 * @param ms
	 * @return
	 */
	public String convertTime(long ms) {
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

		if (secs < 10) {
			secsS = "0" + secsS;
		}
		if (mins < 10) {
			minsS = "0" + minsS;
		}
		if (hours < 10) {
			hoursS = "0" + hoursS;
		}

		return hoursS + "h:" + minsS + "m:" + secsS + "s:" + ms2 + "ms";
	}

	/**
	 * Displays the highscores of a map to a player
	 * 
	 * @param map
	 * @param player
	 */
	public void displayHighscores(int map, Player player) {
		Map<String, Long> records = getRecords(map);

		player.sendMessage("\u00A78---=\u00A72Best\u00A78=\u00A72times\u00A78=\u00A77( \u00A7b" + getMapName(map)
				+ "\u00A77 by\u00A72 " + getMapCreator(map) + " \u00A77(\u00A7aMap" + map + "\u00A77) )\u00A78=---");

		boolean inTopTen = false;
		int counter = 1;
		for (String p : records.keySet()) {
			if (p.equals(player.getName())) inTopTen = true;
			if (counter == 1) player.sendMessage("\u00A7f#\u00A7e" + counter + " \u00A76" + p + " - "
					+ convertTime(records.get(p).longValue()));
			else player.sendMessage("\u00A7f#\u00A7e" + counter + " \u00A7b" + p + " - "
					+ convertTime(records.get(p).longValue()));
			counter++;
			if (counter == 11) break;
		}
		if (!inTopTen && records.containsKey(player.getName())) {
			player.sendMessage("\u00A78--\u00A7aYour\u00A78-\u00A7atime\u00A78--");

			player.sendMessage("\u00A7f#\u00A7eX \u00A76" + player.getName() + " - "
					+ convertTime(records.get(player.getName()).longValue()));

		}
	}

	public String getMapName(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapName")) {
			return getConfig().getString("Parkour.map" + mapNumber + ".mapName");
		} else {
			return "unknownMapName";
		}
	}

	public String getMapCreator(int mapNumber) {
		if (getConfig().contains("Parkour.map" + mapNumber + ".mapCreator")) {
			return getConfig().getString("Parkour.map" + mapNumber + ".mapCreator");

		} else {
			return "unknownCreator";
		}
	}

	public int getMapId(String mapName) {
		for (int i : maps) {
			if (getConfig().getString("Parkour.map" + i + ".mapName").equals(mapName)) { return i; }
		}
		return -1;
	}
}
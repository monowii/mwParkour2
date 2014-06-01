package fr.monowii.parkour2;

import fr.husky.Database;
import fr.husky.sqlite.SQLite;
import fr.monowii.parkour2.managers.PlayersManager;
import fr.husky.mysql.MySQL;
import fr.monowii.parkour2.managers.LevelsManager;
import fr.monowii.parkour2.managers.TimesManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.File;
import java.io.IOException;

public class Parkour2 extends JavaPlugin
{
    /* PERMISSIONS:
       - mwparkour2.sign - Can place special parkour sign
       - mwparkour2.leveleditor - Access to commands: /pk <new/add/removeLast/setName/setAuthors/setSpawn>
       - mwparkour2.admin - Total access
     */

    /* TODO
     * messages translation
     * better help message with /pk   (/pk help create    /pk help options   ...)
     * Move levels to sql ?
     */


    private static JavaPlugin plugin;

    private static LevelsManager levelsManager;
    private static TimesManager timesManager;
    private static PlayersManager playersManager;

    private static FileConfiguration levelsConfig;
    private static File levelsFile;

    private static Database database;
    private static boolean MySqlEnable = false;
    private static String MySqlHostname = "";
    private static String MySqlPort = "";
    private static String MySqlDatabase = "";
    private static String MySqlUser = "";
    private static String MySqlPassword = "";

    public void onEnable() {
        plugin = this;
        levelsManager = new LevelsManager();
        timesManager = new TimesManager();
        playersManager = new PlayersManager();

        loadConfigs();

        setupMysql();

        getCommand("parkour").setExecutor(new PlayersCommands());
        getServer().getPluginManager().registerEvents(new PlayersListeners(), this);

        levelsManager.loadLevels();
        timesManager.loadTimes();

        try {
            new Metrics(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        database.closeConnection();
    }


    private void loadConfigs() {
        //Levels config
        levelsFile = new File(getDataFolder(), "levels.yml");
        try {
            levelsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        levelsConfig = YamlConfiguration.loadConfiguration(levelsFile);

        //Config
        FileConfiguration cfg = getConfig();
        cfg.options().header("The default database type is SQLite");
        cfg.addDefault("mysql.enable", false);
        cfg.addDefault("mysql.hostname", "hostname");
        cfg.addDefault("mysql.port", "port");
        cfg.addDefault("mysql.database", "database");
        cfg.addDefault("mysql.user", "user");
        cfg.addDefault("mysql.password", "password");
        cfg.options().copyDefaults(true);
        saveConfig();

        MySqlEnable = cfg.getBoolean("mysql.enable");
        MySqlHostname = cfg.getString("mysql.hostname");
        MySqlPort = cfg.getString("mysql.port");
        MySqlDatabase = cfg.getString("mysql.database");
        MySqlUser = cfg.getString("mysql.user");
        MySqlPassword = cfg.getString("mysql.password");
    }

    public void setupMysql() {
        if (MySqlEnable)
            database = new MySQL(this, MySqlHostname, MySqlPort, MySqlDatabase, MySqlUser, MySqlPassword);
        else
            database = new SQLite(this, getDataFolder().toString() + File.separator + "mwparkour2.db");
        database.openConnection();
    }


    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static LevelsManager getLevelsManager() {
        return levelsManager;
    }

    public static TimesManager getTimesManager() {
        return timesManager;
    }

    public static PlayersManager getPlayersManager() {
        return playersManager;
    }


    public static FileConfiguration getLevelsConfig() {
        return levelsConfig;
    }

    public static File getLevelsFile() {
        return levelsFile;
    }


    public static Database getParkourDatabase() {
        return database;
    }

    public static boolean isMySqlEnable() {
        return MySqlEnable;
    }

}

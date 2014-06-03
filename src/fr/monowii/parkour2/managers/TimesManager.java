package fr.monowii.parkour2.managers;

import fr.monowii.parkour2.Parkour2;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;

public class TimesManager
{
    public void loadTimes() {
        try {
            Statement s = Parkour2.getParkourDatabase().getConnection().createStatement();
            s.executeUpdate("CREATE TABLE IF NOT EXISTS times(" +
                         "uuid VARCHAR(36) NOT NULL," +
                         "parkourId INT NOT NULL," +
                         "time INT NOT NULL," +
                         "name VARCHAR(16) NOT NULL," +
                         "PRIMARY KEY(uuid, parkourId)" +
                      ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<String, Long> getTimes(int parkourId, int page) {
        LinkedHashMap<String, Long> times = new LinkedHashMap<String, Long>();
        try {
            PreparedStatement ps = Parkour2.getParkourDatabase().getConnection().prepareStatement("SELECT name, time FROM times WHERE parkourId = "+parkourId+" ORDER BY time LIMIT "+page+", 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                times.put(rs.getString("name"), rs.getLong("time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return times;
    }

    public boolean hasScore(int parkourId, Player p) {
        boolean hasScore = false;
        try {
            PreparedStatement ps = Parkour2.getParkourDatabase().getConnection().prepareStatement("SELECT time FROM times WHERE uuid = '"+p.getUniqueId()+"' AND parkourId = "+parkourId);
            ResultSet rs = ps.executeQuery();
            hasScore = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasScore;
    }

    // @return -1 if no time found
    public Long getPlayerTime(int parkourId, Player p) {
        long time = -1;
        try {
            PreparedStatement ps = Parkour2.getParkourDatabase().getConnection().prepareStatement("SELECT time FROM times WHERE uuid = '"+p.getUniqueId()+"' AND parkourId = "+parkourId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                time = rs.getInt("time");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return time;
    }

    public void addPlayerTime(int parkourId, Player p, long time) {
        if (Parkour2.isMySqlEnable())
        {
            try
            {
                PreparedStatement ps = Parkour2.getParkourDatabase().getConnection().prepareStatement("INSERT INTO times (uuid, parkourId, time, name) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE time=VALUES(time), name=VALUES(name)");
                ps.setString(1, p.getUniqueId().toString());
                ps.setInt(2, parkourId);
                ps.setInt(3, (int)time);
                ps.setString(4, p.getName());
                ps.executeUpdate();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                PreparedStatement ps = Parkour2.getParkourDatabase().getConnection().prepareStatement("INSERT OR IGNORE INTO times (uuid, parkourId, time, name) VALUES (?, ?, ?, ?)");
                ps.setString(1, p.getUniqueId().toString());
                ps.setInt(2, parkourId);
                ps.setInt(3, (int)time);
                ps.setString(4, p.getName());
                ps.executeUpdate();

                PreparedStatement ps2 = Parkour2.getParkourDatabase().getConnection().prepareStatement("UPDATE times SET time = ?, name = ? WHERE uuid = ? AND parkourId = ?");
                ps2.setInt(1, (int)time);
                ps2.setString(2, p.getName());
                ps2.setString(3, p.getUniqueId().toString());
                ps2.setInt(4, parkourId);
                ps2.executeUpdate();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}

package fr.monowii.parkour2;

import java.util.*;

public class Utils
{
    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static String convertTime(long time) {
        int ms1 = (int) time;
        int secs = ms1 / 1000;
        int mins = secs / 60;
        int hours = mins / 60;

        hours %= 24;
        secs %= 60;
        mins %= 60;
        ms1 %= 1000;

        String hoursS = ""+hours;
        String secsS = ""+secs;
        String minsS = ""+mins;
        String ms2 = ""+ms1;

        if (secs < 10)
            secsS = "0" + secsS;
        if (mins < 10)
            minsS = "0" + minsS;
        if (hours < 10)
            hoursS = "0" + hoursS;

        return hoursS + "h:" + minsS + "m:" + secsS + "s:" + ms2 + "ms";
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K,V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}

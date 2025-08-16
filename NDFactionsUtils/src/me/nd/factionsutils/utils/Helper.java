package me.nd.factionsutils.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.MetadataValue;

public class Helper {
    private Helper() {
    }

    public static boolean isInteger(Object o) {
        return o instanceof Integer;
    }

    public static boolean isLong(String o) {
        try {
            Long.parseLong(o);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    @SuppressWarnings("unused")
	public static boolean isDouble(String o) {
        try {
            double d = Double.parseDouble(o);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public static boolean isByte(String input) {
        try {
            Byte.parseByte(input);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isShort(String input) {
        try {
            Short.parseShort(input);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isFloat(String input) {
        try {
            Float.parseFloat(input);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isString(Object o) {
        return o instanceof String;
    }

    public static boolean isBoolean(Object o) {
        return o instanceof Boolean;
    }

    public static String removeChar(String s, char c) {
        String r = "";
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) != c) {
                r = String.valueOf(r) + s.charAt(i);
            }
            ++i;
        }
        return r;
    }

    public static String removeFirstChar(String s, char c) {
        String r = "";
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) != c) {
                r = String.valueOf(r) + s.charAt(i);
                break;
            }
            ++i;
        }
        return r;
    }

    public static String capitalize(String content) {
        if (content.length() < 2) {
            return content;
        }
        String first = content.substring(0, 1).toUpperCase();
        return String.valueOf(first) + content.substring(1);
    }

    public static String plural(int count, String word, String ending) {
        return count == 1 ? word : String.valueOf(word) + ending;
    }

    public static String toColor(String hexValue) {
        if (hexValue == null) {
            return "";
        }
        return ChatColor.getByChar((String)hexValue).toString();
    }

    public static List<String> fromArray(String ... values) {
        ArrayList<String> results = new ArrayList<String>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    public static Set<String> fromArray2(String ... values) {
        HashSet<String> results = new HashSet<String>();
        Collections.addAll(results, values);
        results.remove("");
        return results;
    }

    public static List<Player> fromPlayerArray(Player ... values) {
        ArrayList<Player> results = new ArrayList<Player>();
        Collections.addAll(results, values);
        return results;
    }

    public static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static String[] removeFirst(String[] args) {
        List<String> out = Helper.fromArray(args);
        if (!out.isEmpty()) {
            out.remove(0);
        }
        return Helper.toArray(out);
    }

    public static String toMessage(String[] args) {
        String out = "";
        String[] stringArray = args;
        int n = args.length;
        int n2 = 0;
        while (n2 < n) {
            String arg = stringArray[n2];
            out = String.valueOf(out) + arg + " ";
            ++n2;
        }
        return out.trim();
    }

    public static String toMessage(String[] args, String sep) {
        String out = "";
        String[] stringArray = args;
        int n = args.length;
        int n2 = 0;
        while (n2 < n) {
            String arg = stringArray[n2];
            out = String.valueOf(out) + arg + ", ";
            ++n2;
        }
        return Helper.stripTrailing(out, ", ");
    }

    public static String toMessage(List<String> args, String sep) {
        String out = "";
        for (String arg : args) {
            out = String.valueOf(out) + arg + sep;
        }
        return Helper.stripTrailing(out, sep);
    }

    public static String parseColors(String msg) {
        return msg.replace("&", "§");
    }

    public static String stripColors(String msg) {
        String out = msg.replaceAll("[&][0-9a-f]", "");
        out = out.replaceAll(String.valueOf('Â'), "");
        return out.replaceAll("[§][0-9a-f]", "");
    }

    public static String getLastColorCode(String msg) {
        if ((msg = msg.replaceAll(String.valueOf('Â'), "").trim()).length() < 2) {
            return "";
        }
        String one = msg.substring(msg.length() - 2, msg.length() - 1);
        String two = msg.substring(msg.length() - 1);
        if (one.equals("§")) {
            return String.valueOf(one) + two;
        }
        if (one.equals("&")) {
            return Helper.toColor(two);
        }
        return "";
    }

    public static String cleanTag(String tag) {
        return Helper.stripColors(tag).toLowerCase();
    }

    public static String stripTrailing(String msg, String sep) {
        if (msg.length() < sep.length()) {
            return msg;
        }
        String out = msg;
        String first = msg.substring(0, sep.length());
        String last = msg.substring(msg.length() - sep.length(), msg.length());
        if (first.equals(sep)) {
            out = msg.substring(sep.length());
        }
        if (last.equals(sep)) {
            out = msg.substring(0, msg.length() - sep.length());
        }
        return out;
    }

    public static String generatePageSeparator(String sep) {
        String out = "";
        int i = 0;
        while (i < 320) {
            out = String.valueOf(out) + sep;
            ++i;
        }
        return out;
    }

    @Deprecated
    public static boolean isOnline(String playerName) {
        Collection<Player> online = Helper.getOnlinePlayers();
        for (Player o : online) {
            if (!o.getName().equalsIgnoreCase(playerName)) continue;
            return true;
        }
        return false;
    }

    public static boolean isOnline(UUID playerUniqueId) {
        Collection<Player> online = Helper.getOnlinePlayers();
        for (Player o : online) {
            if (!o.getUniqueId().equals(playerUniqueId)) continue;
            return true;
        }
        return false;
    }

    public static boolean testURL(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.connect();
            return urlConn.getResponseCode() == 200;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static String escapeQuotes(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }

    public static String toLocationString(Location loc) {
        return String.valueOf(loc.getBlockX()) + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + loc.getWorld().getName();
    }

    public static boolean isSameBlock(Location loc, Location loc2) {
        return loc.getBlockX() == loc2.getBlockX() && loc.getBlockY() == loc2.getBlockY() && loc.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean isSameLocation(Location loc, Location loc2) {
        return loc.getX() == loc2.getX() && loc.getY() == loc2.getY() && loc.getZ() == loc2.getZ();
    }

	public static Map sortByValue(Map map) {
        List<?> list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                return ((Comparable)((Map.Entry)o2).getValue()).compareTo(((Map.Entry)o1).getValue());
              }
            });
        Map<Object, Object> result = new LinkedHashMap<>();
        for (Iterator<?> it = list.iterator(); it.hasNext(); ) {
          Map.Entry entry = (Map.Entry)it.next();
          result.put(entry.getKey(), entry.getValue());
        } 
        return result;
      }

    public static boolean isVanished(Player player) {
        if (player != null && player.hasMetadata("vanished") && !player.getMetadata("vanished").isEmpty()) {
            return ((MetadataValue)player.getMetadata("vanished").get(0)).asBoolean();
        }
        return false;
    }

	public static Collection<Player> getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getDeclaredMethod("getOnlinePlayers", new Class[0]);
            Object players = method.invoke(null, new Object[0]);
            if (players instanceof Player[]) {
                return new ArrayList<Player>(Arrays.asList((Player[])players));
            }
            return (Collection)players;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Player>();
        }
    }

    public static int checkSpaceInventory(Inventory inventory) {
        int slot = 0;
        int i = 0;
        while (i < inventory.getSize()) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                ++slot;
            }
            ++i;
        }
        return slot;
    }

    public static String allArgs(int start, String[] args) {
        String temp = "";
        int i = start;
        while (i < args.length) {
            temp = String.valueOf(temp) + args[i] + " ";
            ++i;
        }
        return temp.trim();
    }
}

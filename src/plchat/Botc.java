package plchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Botc
{
    
    static final Pattern TAGPAT = Pattern.compile("<(.*?)>");
    static final Pattern TOKENPAT = Pattern.compile("§+");
    static final HashMap<String, String> namemap;
    static final HashMap<String, Integer> scoremap;
    static FileWriter os;

    static
    {
        namemap = new HashMap<>();
        namemap.put("Star Inc.", "Star");
        namemap.put("American Freightways", "AF");
        namemap.put("TransX", "TransX");
        namemap.put("GO-JEK", "GO-JEK");
        namemap.put("LAN Cargo", "LAN");
        namemap.put("President Exp", "President");
        namemap.put("Search and Rescue", "SnR");
        namemap.put("Swift Trucking", "Swift");
        namemap.put("ST Express", "ST");
        namemap.put("Wizz Freightways", "Wizz");
        namemap.put("Inverted Buffalo Express", "IBEx");
        namemap.put("Delta Logistics", "Delta");
        scoremap = new HashMap<>();
        scoremap.put("Star Inc.", 8914);
        scoremap.put("American Freightways", 7497);
        scoremap.put("TransX", 5724);
        scoremap.put("GO-JEK", 2932);
        scoremap.put("LAN Cargo", 1025);
        scoremap.put("President Exp", 827);
        scoremap.put("Search and Rescue", 698);
        scoremap.put("Swift Trucking", 508);
        scoremap.put("ST Express", 425);
        scoremap.put("Wizz Freightways", 316);
        scoremap.put("Inverted Buffalo Express", 188);
        scoremap.put("Delta Logistics", 30);
    }

    @Nullable
    static String get(int type)
    {
        if (os == null) {
            final File file = new File(Main.p.getProperty("logpath"), "botc.txt");
            try {
                os = new FileWriter(file, /*append*/ true);
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        try {
            HTTPRequest req = HTTPRequest.req("/Groups/Companies", null);
            if (req == null) {
                return null;
            }
            return parse(req.response, type);
        } catch (Exception e) {
            Logger.log(e);
            Logger.log("couldn't get bota info");
            return null;
        }
    }
    
    @Nullable
    private static String parse(@NotNull String response, int type)
    {
        int startidx = response.indexOf(
            "<h1 class=\"content-header\">Active Companies</h1>"
        ); 

        if (startidx == -1) {
            return null;
        }
        
        response = response.substring(startidx + 72);
        
        startidx = response.indexOf("</table>");
        if (startidx == -1) {
            return null;
        }
        
        response = response.substring(0, startidx);
        response = TAGPAT.matcher(response).replaceAll("§");
        response = TOKENPAT.matcher(response).replaceAll("§");
        //System.out.println(response);
        final String[] parts = response.split("§");
        int len = 0;
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].trim().isEmpty()) {
                parts[len++] = parts[i];
            }
        }
        
        final ArrayList<Comp> comps = new ArrayList<>(20);
        
        o:
        for (int i = 0; i < len; i++) {
            if ("Announcement".equals(parts[i])) {
                i++;

                while (true) {
                    if (i + 4 >= len) {
                        break o;
                    }
                    try {
                        String n = parts[i + 1];
                        String name = namemap.get(n);
                        if (name != null) {
                            Comp c = new Comp();
                            c.name = name;
                            c.hauls = Integer.parseInt(parts[i + 2]) - scoremap.get(n).intValue();
                            comps.add(c);
                        }
                    } catch (Exception e) {}
                    i += 5;
                }
            }
        }
        try {
            os.write(System.currentTimeMillis() + ";");
            for (Comp c : comps) {
                os.write(c.name + ":" + c.hauls + ",");
            }
            os.write(10);
            os.flush();
        } catch (Exception e) {
            Logger.log(e);
        }
        comps.sort(Botc::compareComp);

        for (Comp c : comps) {
            if ("SnR".equals(c.name)) {
                comps.remove(c);
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (type == 0) {
            for (Comp c : comps) {
                sb.append(", ").append(c.hauls).append(' ').append(c.name);
            }
        } else if (type == 1) {
            int j = 0;
            int score = 0;
            for (int i = comps.size() - 1; i >= 0; i--) {
                Comp c = comps.get(i);
                j++;
                if (j == 4) {
                    score = c.hauls;
                }
                if (j > 4 && c.hauls > score) {
                    break;
                }
                sb.append(", ").append(c.hauls).append(' ').append(c.name);
            }
            if (sb.length() > 0) {
                sb.insert(2, "{ff0000}rip: ");
            }
        }

        if (sb.length() == 0) {
            return null;
        }
        return sb.substring(2);
    }

    private static int compareComp(Comp a, Comp b)
    {
        return Integer.compare(b.hauls, a.hauls);
    }
    
    public static void main(String[] args) throws Exception {
        Main.init();
        ChatThread.login();
        System.out.println(get(1));
    }

    static class Comp
    {
        public String name;
        public int hauls;
    }
}

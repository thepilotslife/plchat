package plchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Bota
{
    
    static final Pattern TAGPAT = Pattern.compile("<(.*?)>");
    static final Pattern TOKENPAT = Pattern.compile("§+");
    static final HashMap<String, String> namemap;
    static final HashMap<String, Integer> scoremap;
    static FileWriter os;

    static
    {
        namemap = new HashMap<>();
        namemap.put("Jetstar Airways", "JET");
        namemap.put("Inverted Airlines", "INV");
        namemap.put("Lufthansa", "LUF");
        namemap.put("Sky Air", "SKY");
        namemap.put("Garuda Indonesia", "GAR");
        scoremap = new HashMap<>();
        scoremap.put("Jetstar Airways", 49559);
        scoremap.put("Inverted Airlines", 48908);
        scoremap.put("Lufthansa", 41031);
        scoremap.put("Sky Air", 30975);
        scoremap.put("Garuda Indonesia", 2478);
    }

    @Nullable
    static String get()
    {
        if (os == null) {
            final File file = new File(Main.p.getProperty("logpath"), "bota2020.txt");
            try {
                os = new FileWriter(file, /*append*/ true);
            } catch (IOException e) {
                Logger.log(e);
            }
        }
        try {
            return parse(HTTPRequest.req("/Groups/", null).response);
        } catch (Exception e) {
            Logger.log(e);
            Logger.log("couldn't get bota info");
            return null;
        }
    }
    
    @Nullable
    private static String parse(@NotNull String response)
    {
        int startidx = response.indexOf(
            "<h1 class=\"content-header\">Active Airlines</h1>"
        ); 

        if (startidx == -1) {
            return null;
        }
        
        response = response.substring(startidx + 47);
        
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
        
        final ArrayList<Airline> airlines = new ArrayList<>(20);
        
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
                            Airline c = new Airline();
                            c.name = name;
                            c.flights = Integer.parseInt(parts[i + 2]) - scoremap.get(n).intValue();
                            airlines.add(c);
                        }
                    } catch (Exception e) {}
                    i += 5;
                }
            }
        }
        try {
            os.write(System.currentTimeMillis() + ";");
            for (Airline c : airlines) {
                os.write(c.name + ":" + c.flights + ",");
            }
            os.write(10);
            os.flush();
        } catch (Exception e) {
            Logger.log(e);
        }
        airlines.sort(Bota::compareAirline);

        for (Airline c : airlines) {
            if ("SnR".equals(c.name)) {
                airlines.remove(c);
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Airline c : airlines) {
            sb.append(", ").append(c.flights).append(' ').append(c.name);
        }

        if (sb.length() == 0) {
            return null;
        }
        return sb.substring(2);
    }

    private static int compareAirline(Airline a, Airline b)
    {
        return Integer.compare(b.flights, a.flights);
    }
    
    public static void main(String[] args) throws Exception {
        Main.init();
        ChatThread.login();
        System.out.println(get());
    }

    static class Airline
    {
        public String name;
        public int flights;
    }
}

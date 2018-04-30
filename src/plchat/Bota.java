package plchat;

import java.util.regex.Pattern;

public class Bota
{
    
    static final Pattern TAGPAT = Pattern.compile("<(.*?)>");
    static final Pattern TOKENPAT = Pattern.compile("§+");

    @Nullable
    static String get()
    {
        try {
            return parse(HTTPRequest.req("/bota.php", null).response);
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
            "<h6 class=\"content-header\">Minimum amount of flights required"
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
        System.out.println(response);
        final String[] parts = response.split("§");
        
        final StringBuilder sb = new StringBuilder();
        int i = -1;
        for (String p : parts) {
            i++;
            
            switch (p) {
            case "2nd":
            case "3rd":
                sb.append(", ");
            case "1st":
                sb.append(parts[i + 3]);
                sb.append(" {ff9900}").append(parts[i + 5]).append("{ffffff} $");
                sb.append(Main.formatMoney(Integer.parseInt(parts[i + 7]) / 1000));
                sb.append("K");
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }
    
    public static void main(String[] args) throws Exception {
        Main.init();
        System.out.println(get());
    }

}

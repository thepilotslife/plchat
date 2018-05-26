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
        //System.out.println(response);
        final String[] parts = response.split("§");
        
        final String[] names = new String[9];
        final int[] amounts = new int[9];
        final int[] flights = new int[9];
        
        int i = 9;
        while (true) {
            // this does not work if there are > 9 airlines
            if (i > parts.length || parts[i].length() != 3) {
                break;
            }

            final int nth = parts[i].charAt(0) - '1';
            if (nth < 0 || 8 < nth) {
                break;
            }

            String name = parts[i + 3];
            final int idx = name.indexOf(' ');
            if (idx != -1) {
                name = name.substring(0, idx);
            }
            names[nth] = name;
            amounts[nth] = Integer.parseInt(parts[i + 7]);
            flights[nth] = Integer.parseInt(parts[i + 5]);

            if ("Jetstar".equals(name)) {
                flights[nth] += 5108;
                amounts[nth] += 7676999;
                for (int a = 0; a < nth; a++) {
                    if (amounts[a] < amounts[nth]) {
                        int _f = flights[nth];
                        int _a = amounts[nth];
                        for (int b = nth; b > a; b--) {
                            names[b] = names[b - 1];
                            amounts[b] = amounts[b - 1];
                            flights[b] = flights[b - 1];
                        }
                        flights[a] = _f;
                        amounts[a] = _a;
                        names[a] = "Jetstar";
                        break;
                    }
                }
            }
            
            i += 13;
        }
        
        final StringBuilder sb = new StringBuilder();
        for (i = 0; i < 3; i++) {
            if (i > 0) {
                sb.append("{ffffff} ");
            }
            sb.append(names[i]);
            sb.append(" ").append(flights[i]);
            sb.append(" {33AA33}").append(Main.formatMoney(amounts[i] / 1000));
            sb.append("K");
        }

        if (sb.length() == 0) {
            return null;
        }

        return " ~" + sb.toString();
    }
    
    public static void main(String[] args) throws Exception {
        Main.init();
        System.out.println(get());
    }

}

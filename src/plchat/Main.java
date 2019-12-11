package plchat;

import java.io.FileInputStream;
import java.util.*;

public class Main
{

    private static ChatThread chat;
    static Properties p;
    static ChatLogger chatlogger;
    static long lastmessage;
    static HashMap<String, PlayerData> pd = new HashMap<>();

    public static void main(String[] args) throws Exception
    {
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
        chatlogger = new ChatLogger();
        chat = new ChatThread(Main::chatconsumer);
        chat.start();
    }
    
    static void init() throws Exception
    {
        p = new Properties();
        try (FileInputStream is = new FileInputStream("user.properties")) {
            p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("make sure file 'user.properties' exists");
            System.out.println("  and contains keys 'name','pw','logpath'");
            return;
        }

        Logger.init();
    }
    
    static void chatconsumer(@NotNull ArrayList<ChatMessage> messages)
    {
        if (messages.size() == 0) {
            return;
        }
        
        if (lastmessage == 0) {
            lastmessage = messages.get(messages.size() - 1).time.getTime();
        }
        
        long batchlastmessage = lastmessage;
        
        final ListIterator<ChatMessage> iter = messages.listIterator(messages.size());
        while (iter.hasPrevious()) {
            final ChatMessage message = iter.previous();
            long time = message.time.getTime();
            if (time <= lastmessage) {
                continue;
            }
            
            batchlastmessage = time;
            try {
                chatlogger.log(message);
            } catch (Exception e) {
                Logger.log(e);
                Logger.log("exception while saving chat log");
            }
            
            if (message.message.startsWith("!")) {
                final String parts[] = message.message.split(" ");
                handleCommand(message, parts[0].toLowerCase(), parts);
            }
        }
        
        lastmessage = batchlastmessage;
    }
    
    private static void handleCommand(
        @NotNull ChatMessage message,
        @NotNull String command,
        @NotNull String[] params)
    {
        if ("!cmds".equals(command)) {
            chat.send(
                "- !ping !8ball !player !score !cash !groups !assets !cars !houses "
                + "!licenses !roll !interest !rinterest !missions"
            );
            return;
        }

        if ("!ping".equals(command)) {
            Random r = new Random();
            char[] color = { '{', '0', '0', '0', '0', '0', '0', '}' };
            char[] v = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
            };
            for (int i = 1; i < 7; i++) {
                color[i] = v[r.nextInt(v.length)];
            }
            chat.send(new String(color) + "pong! - hi " + message.player);
            return;
        }

        if ("!keepalive".equals(command)) {
            chat.send("" + HTTPRequest.keepaliverequests);
            return;
        }
        
        if ("!bota".equals(command)) {
            chat.send("BOTA is over, gz Jetstar, IA, AA and everyone else");
            /*
            final String bota = Bota.get();
            if (bota == null) {
                chat.send("could not get BOTA data");
                return;
            }
            chat.send(bota);
            return;
            */
        }

        if ("!botc".equals(command)) {
            final String botc = Botc.get();
            if (botc == null) {
                chat.send("could not get BOTC data");
            } else {
                chat.send(botc);
            }
            return;
        }
        
        if ("!botainfo".equals(command)) {
            chat.send(
                "Apr30-Jun2 $.5M and cape/member + $5M for airline + airline slot "
                + "(money wins)"
            );
            return;
        }

        if ("!interest".equals(command)) {
            if (params.length > 1) {
                try {
                    final int value = Integer.parseInt(params[1]);
                    final int interest = (int) (value / 1500f);
                    final String msg = String.format(
                        "$%s will generate about $%s every 60 minutes",
                        formatMoney(value),
                        formatMoney(interest)
                    );
                    chat.send(msg);
                    return;
                } catch (Exception e) {
                }
            }
            chat.send("syntax: !interest [amount]");
            return;
        }

        if ("!rinterest".equals(command)) {
            if (params.length > 1) {
                try {
                    int interest = Integer.parseInt(params[1]);
                    String m = "";
                    if (interest > 268434) {
                        interest = 268434;
                        m = "(overflow) ";
                    }
                    final int value = interest * 1500;
                    final String msg = String.format(
                        "%sto get $%s of interest you need about $%s",
                        m,
                        formatMoney(interest),
                        formatMoney(value)
                    );
                    chat.send(msg);
                    return;
                } catch (Exception e) {
                }
            }
            chat.send("syntax: !rinterest [amount]");
            return;
        }
        
        if ("!roll".equals(command)) {
            int max = 101;
            if (params.length > 1) {
                try {
                    max = Integer.parseInt(params[1]) + 1;
                    if (max < 2) {
                        max = 2;
                    }
                } catch (Exception e) {}
            }
            chat.send(message.player + " rolls " + (new Random()).nextInt(max));
            return;
        }
        
        if ("!8ball".equals(command)) {
            final String[] responses = {
                "It is certain",
                "It is decidedly so",
                "Without a doubt",
                "Yes, definitely",
                "You may rely on it",
                "As I see it, yes",
                "Most likely",
                "Yes",
                "Sign points to yes",
                "Ask again later",
                "It's better not to know",
                "Cannot predict now",
                "Don't count on it",
                "My reply is no",
                "My sources say no",
                "Very doubtful"
            };
            final int resp = new Random().nextInt(responses.length);
            chat.send(message.player + ": " + responses[resp]);
            return;
        }
        
        if ("!player".equals(command)) {
            final PlayerData data = playerCommand("player", message.player, params);

            if (data != null) {
                chat.send(String.format(
                    "player %s: %s score, last seen %s",
                    data.name,
                    data.score,
                    data.lastConnected
                ));
            }
            return;
        }
        
        if ("!score".equals(command)) {
            final PlayerData data = playerCommand("score", message.player, params);

            if (data != null) {
                chat.send(String.format(
                    "player %s: %s score, %d missions: %d'/. %s - %d'/. %s - %d'/. %s",
                    data.name,
                    data.score,
                    data.totalMissions,
                    data.shareOne,
                    data.nameOne,
                    data.shareTwo,
                    data.nameTwo,
                    data.shareThree,
                    data.nameThree
                ));
            }
            return;
        }

        if ("!missions".equals(command)) {
            if (params.length > 1) {
                String player = message.player;
                int typestart = 1;
                if (params.length > 2) {
                    player = params[1];
                    typestart = 2;
                }
                PlayerData p = getPlayerData(player);
                if (p != null) {
                    StringBuilder s = new StringBuilder(player).append(":");
                    Integer cd = p.missions.get("cargo drop");
                    if (cd != null) {
                        p.missions.put("cargodrop", cd);
                        p.missions.remove("cargo drop");
                    }
                    for (; typestart < params.length; typestart++) {
                        Integer value = p.missions.get(params[typestart]);
                        if (value == null) {
                            s.setLength(0);
                            for (String type : p.missions.keySet()) {
                                s.append('/').append(type);
                            }
                            if (s.length() > 0) {
                                chat.send(s.substring(1));
                            }
                            return;
                        }
                        s.append(' ').append(value.intValue()).append(" ").append(params[typestart]);
                    }
                    chat.send(s.toString());
                }
            }
            return;
        }

        if ("!cash".equals(command)) {
            final PlayerData data = playerCommand("cash", message.player, params);

            if (data != null) {
                chat.send(String.format(
                    "%s has $%s in hand",
                    data.name,
                    formatMoney(data.money)
                ));
            }
            return;
        }

        if ("!groups".equals(command)) {
            final PlayerData data = playerCommand("groups", message.player, params);

            if (data != null) {
                chat.send(String.format(
                    "%s is in airline %s and company %s",
                    data.name,
                    data.airline == null ? "(none)" : data.airline,
                    data.company == null ? "(none)" : data.company
                ));
            }
            return;
        }

        if ("!assets".equals(command)) {
            final PlayerData data = playerCommand("assets", message.player, params);

            if (data != null) {
                chat.send(String.format(
                    "%s has %d car(s) ($%s) and %d house(s) ($%s - %d slots) "
                    + "for a total of $%s",
                    data.name,
                    data.cars.size(),
                    formatMoney(data.totalCarCost),
                    data.houses.size(),
                    formatMoney(data.totalHouseCost),
                    data.totalHouseSlots,
                    formatMoney(data.totalCarCost + data.totalHouseCost)
                ));
            }
            return;
        }

        if ("!cars".equals(command)) {
            final PlayerData data = playerCommand("cars", message.player, params);

            if (data != null) {
                String result = data.name + " has:";
                for (PlayerData.Car car : data.cars) {
                    result += " " + car.name;
                }
                chat.send(result);
            }
            return;
        }

        if ("!houses".equals(command)) {
            final PlayerData data = playerCommand("houses", message.player, params);

            if (data != null) {
                if (data.houses.isEmpty()) {
                    chat.send(data.name + " does not own any houses!");
                    return;
                }
                String result = "";
                for (PlayerData.House house : data.houses) {
                    result += String.format(
                        " %s: $%s %d slots",
                        house.location,
                        formatMoney(house.cost),
                        house.slots
                    );
                }
                chat.send(result);
            }
            return;
        }

        if ("!licenses".equals(command)) {
            final PlayerData data = playerCommand("licenses", message.player, params);

            if (data != null) {
                final String PRE_0 = "a license for ";
                final String PRE_1 = ", ";
                String pre = PRE_0;
                final StringBuilder sb = new StringBuilder();
                sb.append(data.name).append(" has ");
                if (data.shamalLicense) {
                    sb.append(pre);
                    pre = PRE_1;
                    sb.append("shamal");
                }
                if (data.dodoLicense) {
                    sb.append(pre);
                    pre = PRE_1;
                    sb.append("dodo");
                }
                if (data.maverickLicense) {
                    sb.append(pre);
                    pre = PRE_1;
                    sb.append("maverick");
                }
                if (data.nevadaLicense) {
                    sb.append(pre);
                    pre = PRE_1;
                    sb.append("nevada");
                }
                if (!PRE_1.equals(pre)) {
                    sb.append("no licenses!");
                }
                chat.send(sb.toString());
            }
            return;
        }
    }
    
    @Nullable
    private static PlayerData playerCommand(
        @NotNull String command,
        @NotNull String player,
        @NotNull String[] params)
    {
       if (params.length < 2) {
           //chat.send("syntax: !" + command + " [playername]");
           //return null;
           return getPlayerData(player);
       }
       
       return getPlayerData(params[1]);
    }
    
    @Nullable
    private static PlayerData getPlayerData(@NotNull String player)
    {
       PlayerData data = pd.get(player);
       if (data != null &&
           System.currentTimeMillis() - data.parseTime > 1000L * 60L * 5L)
       {
           data = null;
       }
       if (data == null) {
           data = PlayerData.forName(player);
           if (data == null) {
               chat.send("could not get data for player " + player);
               return null;
           }
           data.name = player;
           pd.put(player, data);
       }
       return data;
    }
    
    @NotNull
    public static String formatMoney(int value)
    {
        String after = "";
        String before = String.valueOf(value);
        
        while (before.length() > 3) {
            if (before.length() == 4 && before.charAt(0) == '-') {
                break;
            }
            after = "," + before.substring(before.length() - 3) + after;
            before = before.substring(0, before.length() - 3);
        }
        
        return before + after;
    }
    
    static void shutdown()
    {
        Logger.log("shutdownhook");
        Logger.shutdown();
        if (chat != null) {
            chat.shutdown();
        }
        HTTPRequest.shutdown();
        if (chatlogger != null) {
            chatlogger.shutdown();
        }
    }

}

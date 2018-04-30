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
                + "!licenses"
            );
            return;
        }

        if ("!ping".equals(command)) {
            chat.send("{ff00ff}pong! - hi " + message.player);
            return;
        }
        
        if ("!bota".equals(command)) {
            final String bota = Bota.get();
            if (bota == null) {
                chat.send("could not get BOTA data");
                return;
            }
            chat.send(bota);
            return;
        }
        
        if ("!botainfo".equals(command)) {
            chat.send(
                "Apr30-Jun2 $.5M and cape/member + $5M for airline + airline slot "
                + "(money wins)"
            );
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
            final PlayerData data = playerCommand("player", params);

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
            final PlayerData data = playerCommand("score", params);

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

        if ("!cash".equals(command)) {
            final PlayerData data = playerCommand("cash", params);

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
            final PlayerData data = playerCommand("groups", params);

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
            final PlayerData data = playerCommand("assets", params);

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
            final PlayerData data = playerCommand("cars", params);

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
            final PlayerData data = playerCommand("houses", params);

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
            final PlayerData data = playerCommand("licenses", params);

            if (data != null) {
                final String PRE_0 = " a license for ";
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
        @NotNull String[] params)
    {
       if (params.length < 2) {
           chat.send("syntax: !" + command + " [playername]");
           return null;
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

package plchat;

import java.io.FileInputStream;
import java.util.*;

public class Main
{

    private static ChatThread chat;
    static Properties p;
    static ChatLogger chatlogger;
    static long lastmessage;

    public static void main(String[] args) throws Exception
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
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
        chatlogger = new ChatLogger();
        chat = new ChatThread(Main::chatconsumer);
        chat.start();
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
            
            if (message.message.equals("!ping")) {
                chat.send("pong! - hi " + message.player);
            } else if (message.message.startsWith("!8ball")) {
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
            }
        }
        
        lastmessage = batchlastmessage;
    }
    
    static void shutdown()
    {
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

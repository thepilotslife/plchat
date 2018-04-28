package plchat;

import java.io.FileInputStream;
import java.util.*;

public class Main
{

    private static ChatThread chat;
    static Properties p;
    static long lastmessage;

    public static void main(String[] args) throws Exception
    {
        p = new Properties();
        try (FileInputStream is = new FileInputStream("user.properties")) {
            p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("make sure file 'user.properties' exists");
            System.out.println("  and contains keys 'name' and 'pw'");
            return;
        }

        Logger.init();
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
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
        
        final ListIterator<ChatMessage> iter = messages.listIterator(messages.size());
        while (iter.hasPrevious()) {
            final ChatMessage message = iter.previous();
            long time = message.time.getTime();
            if (time <= lastmessage) {
                continue;
            }
            
            lastmessage = time;
            
            if (message.source == ChatMessage.SRC_DIS) {
                System.out.println(String.format(
                    "[%tH:%<tM:%<tS] <-- %s disconnected from the server",
                    message.time,
                    message.player
                ));
                continue;
            }
            if (message.source == ChatMessage.SRC_CON) {
                System.out.println(String.format(
                    "[%tH:%<tM:%<tS] --> %s connected to the server",
                    message.time,
                    message.player
                ));
                continue;
            }

            final String[] SOURCES = { "(?) ", "", "(WEB) " };
            System.out.println(String.format(
                "[%tH:%<tM:%<tS] %s<%s> %s",
                message.time,
                SOURCES[message.source],
                message.player,
                message.message
            ));
        }
    }
    
    static void shutdown()
    {
        Logger.shutdown();
        if (chat != null) {
            chat.shutdown();
        }
    }

}

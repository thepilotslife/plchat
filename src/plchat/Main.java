package plchat;

import java.io.FileInputStream;
import java.util.Properties;

public class Main
{

    private static ChatThread chat;
    static Properties p;

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
        chat = new ChatThread();
        chat.start();
    }
    
    static void shutdown()
    {
        Logger.shutdown();
        if (chat != null) {
            chat.shutdown();
        }
    }

}

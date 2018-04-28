package plchat;

import java.io.*;

class Logger
{

    private static FileOutputStream os;

    static void init() throws FileNotFoundException
    {
        final String filename = String.format(
            "log-%tY-%<tm-%<td-%<tH%<tM%<tS.txt",
            Time.calendar
        );
        os = new FileOutputStream(filename);
    }
    
    static void shutdown()
    {
        if (os != null) {
            try {
                os.close();
            } catch (IOException ignored) {}
        }
    }
    
    static void log(@NotNull Throwable t)
    {
        t.printStackTrace();
        try {
            t.printStackTrace(new PrintWriter(os));
            os.write('\n');
            os.flush();
        } catch (Throwable u) {
            u.printStackTrace();
        }
    }
    
    static void log(@NotNull String s)
    {
        System.out.println(s);
        try {
            os.write(s.getBytes());
            os.write('\n');
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

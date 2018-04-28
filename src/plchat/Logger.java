package plchat;

import java.io.*;

class Logger
{

    private static FileWriter os;

    static void init() throws IOException
    {
        final String filename = String.format(
            "log-%tY-%<tm-%<td-%<tH%<tM%<tS.txt",
            Time.getCalendar()
        );
        os = new FileWriter(filename, /*append*/ true);
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
        s = String.format(
            "[%td-%<tm-%<tH:%<tM:%<tS] %s",
            Time.getCalendar(),
            s
        );
        System.out.println(s);
        try {
            os.write(s);
            os.write('\n');
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

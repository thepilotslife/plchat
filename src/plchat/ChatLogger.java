package plchat;

import java.io.*;
import java.util.*;

class ChatLogger
{

    private FileWriter writer;
    private int lastday;
    
    ChatLogger()
    {
        ensureLogfile();
    }
    
    void log(@NotNull ChatMessage message) throws Exception
    {
        ensureLogfile();
            
        if (message.source == ChatMessage.SRC_DIS) {
            this.writer.write(String.format(
                "<font color='#ccc'>[%tH:%<tM:%<tS] "
                + "&lt;-- %s disconnected from the server</font><br/>",
                message.time,
                this.escape(message.player)
            ));
            this.writer.flush();
            return;
        }

        if (message.source == ChatMessage.SRC_CON) {
            this.writer.write(String.format(
                "<font color='#999'>[%tH:%<tM:%<tS] "
                + "--&gt; %s connected to the server</font><br/>",
                message.time,
                this.escape(message.player)
            ));
            this.writer.flush();
            return;
        }

        final String[] SOURCES = { "(?) ", "", "(WEB) " };
        this.writer.write(String.format(
            "[%tH:%<tM:%<tS] %s&lt;%s&gt; %s<br/>",
            message.time,
            SOURCES[message.source],
            this.escape(message.player),
            this.escape(message.message)
        ));
        this.writer.flush();
    }
    
    @NotNull
    private String escape(@NotNull String msg)
    {
        StringBuilder out = new StringBuilder(msg.length() * 2);
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
    
    private void ensureLogfile()
    {
        final int currentday = Time.getCalendar().get(Calendar.DAY_OF_YEAR);
        if (this.writer != null && this.lastday == currentday) {
            return;
        }
        this.lastday = currentday;
        
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (Exception e) {}
        }
        
        final String filename = String.format(
            "%tY-%<tm-%<td.html",
            Time.getCalendar().getTime()
        );
        
        final File logfile = new File(Main.p.getProperty("logpath"), filename);
        
        try {
            this.writer = new FileWriter(logfile, /*append*/ true);
        } catch (IOException e) {
            Logger.log(e);
            Logger.log("could not create chat log file");
            return;
        }
        
        try {
            this.writer.write(String.format(
                "<em>*** session open: %tH:%<tM:%<tS</em><br/>",
                Time.getCalendar().getTime()
            ));
            this.writer.flush();
        } catch (IOException e) {
            Logger.log(e);
            Logger.log("could not write chat log file session start");
        }
    }

    void shutdown()
    {
        if (this.writer != null) {
            try {
                this.writer.write(String.format(
                    "<em>*** session close: %tH:%<tM:%<tS</em><br/>",
                    Time.getCalendar().getTime()
                ));
                this.writer.flush();
            } catch (Exception e) {}
            try {
                this.writer.close();
            } catch (Exception e) {
            }
        }
    }

}

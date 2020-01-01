package plchat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

class ChatThread extends Thread
{

    static String phpsess;
    
    private Consumer<ArrayList<ChatMessage>> listener;
    /*
    private long lastbotc = System.currentTimeMillis();
    */

    ChatThread(Consumer<ArrayList<ChatMessage>> listener)
    {
        this.listener = listener;
    }

    @Override
    public void run()
    {
        while (true) {
            if (phpsess == null) {
                try {
                    login();
                    this.send("I'm up");
                } catch (IOException e) {
                    Logger.log(e);
                }
            }
            try {
                grab();
            } catch (IOException e) {
                Logger.log(e);
            }

            /*
            long time = System.currentTimeMillis();
            if (time - lastbotc > 600_000) {
                lastbotc = time;
                final String botc = Botc.get(0);
                if (botc == null) {
                    this.send("could not get BOTC data");
                } else {
                    this.send(botc);
                }
            }
            */

            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {}
        }
    }
    
    public static void login() throws IOException
    {
        final String body = String.format(
            "form_submitted=1&form_username=%s&form_password=%s"
            + "&form_autologin=1&submit=",
            URLEncoder.encode(Main.p.getProperty("name"), "UTF-8"),
            URLEncoder.encode(Main.p.getProperty("pw"), "UTF-8")
        );
        
        HTTPRequest.req("/?", body);
        if (phpsess == null) {
            Logger.log("tried to login, but did not receive a session");
        } else {
            Logger.log("logged in (probably)");
        }
    }

    void send(@NotNull String message)
    {
        try {
            if (message.length() > 110) {
                message = message.substring(0, 108) + "(..)";
            }
            message = URLEncoder.encode(message, "UTF-8");
            final String path = "/assets/chat-send.php?comment=" + message;
            HTTPRequest.req(path, null);
        } catch (Exception e) {
            Logger.log(e);
            Logger.log("failed to send chat message");
        }
    }
    
    private void grab() throws IOException
    {
        final HTTPRequest req = HTTPRequest.req("/assets/chat-output.php", null);
        final String res = req.response;
        
        if (res == null) {
            Logger.log("got null response while grabbing chat");
            return;
        }

        //System.out.println(res);

        /*
         * <!--blablahaydzleft
         * -->
         * 
         * \t<div class="chat-mg"><a>playername</a> has connected to the server<br>
           <span class='chat-stuff'> 5 minutes Ago</span> - <span class='chat-stuff'>
           In-Game Chat</span></div>
         *
         * \t<div class="chat-msg"><a>robin_be</a> - ping<br><span class='chat-stuff'>
           Recently Posted</span> - <span class='chat-stuff'>Website Chat</span></div>
         *
         * \t <div class="chat-msg"><b style="color: #FF9900;">(Admin Chat)</b> <a>
         * usr</a> - Msg<br><span class='chat-stuff'>23 Seconds Ago</span> - <span
         *  class='char-stuff'>In-Game Chat</span></div>
         * 
         * \t\t\t\t\t<div class="chat-msg"> <b style="color: #33AA33;"> (American
         *  Airlines)</b> <a>user</a> - Msg<br><span class='chat-stuff'>3 Seconds
         * Ago</span> - <span class='chat-stuff'>In-Game Chat</span></div>
         */
        
        final Date nowtime = Time.getCalendar().getTime();
        final ArrayList<ChatMessage> messages = new ArrayList<>();
        boolean isend = false;
        try {
            int start = -1;
            do {
                start = res.indexOf('\t', start + 1);
                if (start == -1) {
                    return;
                }
                
                if (res.charAt(start + 1) != '<') {
                    continue;
                }

                start += 26; // "<div class='chat-msg'><a>".length();
                
                int end = res.indexOf('\t', start + 1);
                while (end < start && end != -1) {
                    end = res.indexOf('\t', end + 1);
                }
                if (end == -1) {
                    isend = true;
                    end = res.length();
                }
                
                if (end - start < 10) {
                    continue;
                }

                String msg = res.substring(start, end);
                
                if (msg.contains("color: #FF9900;") || msg.contains("(Admin Chat)")) {
                    // admin chat
                    continue;
                }

                if (msg.contains("color: #33AA33;")) {
                    // airline chat
                    continue;
                }
                
                try {
                    final ChatMessage message = parseMessage(msg, nowtime);
                    if (message != null) {
                        messages.add(message);
                    }
                } catch (Exception e) {
                    Logger.log(e);
                    Logger.log("exception while parsing single message");
                    messages.add(ChatMessage.unk(
                        nowtime,
                        "exception while parsing single message"
                    ));
                }
            } while (!isend);
        } catch (Exception e) {
            Logger.log(e);
            Logger.log(
                "exception while parsing batch of messages, response is "
                + req.response
            );
            messages.add(ChatMessage.unk(
                nowtime,
                "exception while parsing batch of messages"
            ));
        }
        
        listener.accept(messages);
    }
    
    @Nullable
    private ChatMessage parseMessage(@NotNull String msg, @NotNull Date nowtime)
    {
        int idx;
        
        idx = msg.indexOf("</a>");
        if (idx == -1) {
            return null;
            //return ChatMessage.unk(nowtime, "couldn't find username end");
        }
        
        final String player = msg.substring(0, idx);
        final int msgstartindex = idx + 4; // "</a>".length();

        int end = msg.lastIndexOf("</span></div>");
        if (end == -1) {
            return ChatMessage.unk(nowtime, "couldn't find message end");
        }
        
        idx = msg.lastIndexOf('>', end - 1);
        if (idx == -1) {
            return ChatMessage.unk(nowtime, "couldn't find source start");
        }
        
        final String sourcestr = msg.substring(idx + 1, end);
        int source = ChatMessage.SRC_UNK;
        if ("In-Game Chat".equals(sourcestr)) {
            source = ChatMessage.SRC_IGN;
        } else if ("Website Chat".equals(sourcestr)) {
            source = ChatMessage.SRC_WEB;
        }
        
        end = idx - 35; // "</span> - <span class='chat-stuff'>".length();
        idx = msg.lastIndexOf('>', end - 1);
        if (idx == -1) {
            return ChatMessage.unk(nowtime, "couldn't find time start");
        }
        
        Date time = nowtime;
        final String timestr = msg.substring(idx + 1, end + 1);
        if (timestr.endsWith("Seconds Ago")) {
            int seconds = 0;
            try {
                seconds = Integer.parseInt(timestr.substring(0, timestr.indexOf(' ')));
            } catch (Exception e) {
                Logger.log("could not get time for '" + timestr + "'");
            }
            if (seconds != 0) {
                final Calendar c = Time.getCalendar();
                c.add(Calendar.SECOND, -seconds);
                nowtime = c.getTime();
            }
        } else if (!"Recently Posted".equals(timestr)) {
            final String skipmsg = "skipping old message: " + timestr;
            return new ChatMessage(time, "-", skipmsg, source);
        }
        
        end = idx - 28; // "<br><span class='chat-stuff'>".length();
        final String message = msg.substring(msgstartindex, end);
        
        if (message.startsWith(" has connected")) {
            source = ChatMessage.SRC_CON;
        } else if (message.startsWith(" has disconnected")) {
            source = ChatMessage.SRC_DIS;
        }

        return new ChatMessage(time, player, message.substring(3), source);
    }
    
    void shutdown()
    {
        this.interrupt();
    }
    

}

package plchat;

import java.io.*;
import java.net.*;

import static plchat.Constants.*;

class ChatThread extends Thread
{

    static Socket socket;
    static InputStream is;
    static OutputStream os;
    static String phpsess;

    @Override
    public void run()
    {
        while (true) {
            if (phpsess == null) {
                try {
                    login();
                } catch (IOException e) {
                    Logger.log(e);
                }
            }
            try {
                grab();
            } catch (IOException e) {
                Logger.log(e);
            }
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {}
        }
    }
    
    private void ensureSocket()
    {
        if (socket == null || socket.isClosed()) {
            System.out.println("socket is null or closed, opening a new one");
            try {
                socket = new Socket(PL_ADDR, 80);
                os = socket.getOutputStream();
                is = socket.getInputStream();
            } catch (IOException e) {
                Logger.log(e);
            }
        };
    }
    
    private void login() throws IOException
    {
        ensureSocket();
        final String body = String.format(
            "form_submitted=1&form_username=%s&form_password=%s"
            + "&form_autologin=1&submit=",
            URLEncoder.encode(Main.p.getProperty("name"), "UTF-8"),
            URLEncoder.encode(Main.p.getProperty("pw"), "UTF-8")
        );
        
        new HTTPRequest("?", body);
        if (phpsess == null) {
            Logger.log("tried to login, but did not receive a session");
        } else {
            Logger.log("logged in (probably)");
        }
    }
    
    private void grab() throws IOException
    {
        ensureSocket();
        final HTTPRequest req = new HTTPRequest("/assets/chat-output.php", null);
        System.out.println(req.response);
    }
    
    void shutdown()
    {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
        this.interrupt();
    }
    

}

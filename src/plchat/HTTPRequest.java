package plchat;

import java.io.*;
import java.net.*;

import static java.lang.System.arraycopy;

import static plchat.Constants.*;

public class HTTPRequest
{

    static Socket socket;
    static InputStream is;
    static OutputStream os;
    static String phpsess;
    
    private int contentLength;
    private boolean chunked;
    String response;
    
    synchronized static HTTPRequest req(
        @NotNull String path,
        @Nullable String postdata)
        throws IOException
    {
        ensureSocket();
        return new HTTPRequest(path, postdata);
    }
    
    private static void ensureSocket()
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
    
    static void shutdown()
    {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private HTTPRequest(
        @NotNull String path,
        @Nullable String postdata)
        throws IOException
    {
        this.chunked = false;
        this.contentLength = -1;

        if (postdata != null) {
            os.write("POST ".getBytes());
        } else {
            os.write("GET ".getBytes());
        }
        os.write(path.getBytes());
        os.write(" HTTP/1.1\r\n".getBytes());
        os.write("Host: thepilotslife.com\r\n".getBytes());
        if (ChatThread.phpsess != null) {
            os.write("Cookie: PHPSESSID=".getBytes());
            os.write(ChatThread.phpsess.getBytes());
            os.write("\r\n".getBytes());
        }
        if (postdata != null) {
            os.write("Content-Length: ".getBytes());
            os.write(String.valueOf(postdata.length()).getBytes());
            os.write("\r\n".getBytes());
            os.write("Content-Type: application/x-www-form-urlencoded\r\n".getBytes());
        }
        os.write("Connection: keep-alive\r\n".getBytes());
        os.write("\r\n".getBytes());
        if (postdata != null) {
            os.write(postdata.getBytes());
        }
        os.flush();
        
        final StringBuilder sb = new StringBuilder();
        int prev = 0;
        while (true) {
            int i = is.read();
            if (i == -1) {
                Logger.log("unexpected close");
                return;
            }
            sb.append((char) i);
            if (prev == '\r' && i == '\n') {
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                if (sb.length() == 0) {
                    break;
                }
                readHeaderLine(sb.toString());
                sb.setLength(0);
                prev = 0;
                continue;
            }
            prev = i;
        }
        
        if (this.chunked) {
            Logger.log("reading chunked response");
            this.readChunked();
            return;
        }

        if (this.contentLength == -1) {
            Logger.log("no content length, closing socket");
            socket.close();
            return;
        }
        
        int actuallyread = 0;
        byte[] content = new byte[this.contentLength];
        while (actuallyread != this.contentLength) {
            int bytestoread = content.length - actuallyread;
            actuallyread += is.read(content, actuallyread, bytestoread);
        }
        
        this.response = new String(content);
    }
    
    private void readChunked() throws IOException
    {
        byte[] content = new byte[0];

        while (true) {
            final StringBuilder chunksizestr = new StringBuilder();
            while (true) {
                int i = is.read();
                if (i == -1) {
                    Logger.log("unexpected close");
                }
                if (i == '\r') {
                    continue;
                }
                if (i == '\n') {
                    break;
                }
                chunksizestr.append((char) i);
            }
            
            int chunksize = Integer.parseInt(chunksizestr.toString(), 16);
            
            Logger.log("chunksize " + chunksize);
            if (chunksize == 0) {
                if (is.read() == -1 || is.read() == -1) {
                    Logger.log("unexpected close");
                }
                break;
            }

            int actuallyread = 0;
            byte[] chunkcontent = new byte[chunksize];
            while (actuallyread != chunksize) {
                int bytestoread = chunkcontent.length - actuallyread;
                actuallyread += is.read(chunkcontent, actuallyread, bytestoread);
            }
            
            byte[] newcontent = new byte[content.length + chunkcontent.length];
            if (content.length > 0) {
                arraycopy(content, 0, newcontent, 0, content.length);
            }
            arraycopy(chunkcontent, 0, newcontent, content.length, chunkcontent.length);
            content = newcontent;
            
            if (is.read() == -1 || is.read() == -1) {
                Logger.log("unexpected close");
            }
        }

        this.response = new String(content);
    }
    
    private void readHeaderLine(@NotNull String line)
    {
        //System.out.println(line);

        if (line.startsWith("Set-Cookie:")) {
            String part = line.substring(11).trim();
            int end = part.indexOf(';');
            if (end != -1) {
                part = part.substring(0, end);
                end = part.indexOf('=');
                if (end != -1) {
                    if ("PHPSESSID".equals(part.substring(0, end))) {
                        ChatThread.phpsess = part.substring(end + 1);
                        System.out.println("sessionid " + ChatThread.phpsess);
                    }
                }
            }
        }
        
        if (line.startsWith("Content-Length:")) {
            this.contentLength = Integer.parseInt(line.substring(15).trim());
        }
        
        if (line.startsWith("Transfer-Encoding: chunked")) {
            this.chunked = true;
        }
    }

}

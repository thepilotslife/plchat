package plchat;

import java.util.Date;

public class ChatMessage
{

    final static int SRC_UNK = 0;
    final static int SRC_IGN = 1;
    final static int SRC_WEB = 2;
    final static int SRC_CON = 3;
    final static int SRC_DIS = 4;
    
    static ChatMessage ign(Date time, @NotNull String player, @NotNull String message)
    {
        return new ChatMessage(time, player, message, SRC_IGN);
    }
    
    static ChatMessage web(Date time, @NotNull String player, @NotNull String message)
    {
        return new ChatMessage(time, player, message, SRC_WEB);
    }
    
    static ChatMessage unk(Date time, @NotNull String message)
    {
        return new ChatMessage(time, "-", message, SRC_UNK);
    }

    final Date time;
    final String player;
    final String message;
    final int source;

    ChatMessage(
        Date time,
        @NotNull String player,
        @NotNull String message,
        int source)
    {
        this.time = time;
        this.player = player;
        this.message = message;
        this.source = source;
    }

}

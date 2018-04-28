package plchat;

import java.net.*;

class Constants
{

    final static InetAddress PL_ADDR;
    
    static
    {
        final byte[] PL_IP = { (byte) 142, 44, (byte) 161, 46 };
        InetAddress addr = null;
        try {
            addr = Inet4Address.getByAddress(PL_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PL_ADDR = addr;
    }

}

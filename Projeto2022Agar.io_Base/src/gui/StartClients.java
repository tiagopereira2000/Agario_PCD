package gui;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;


public class StartClients {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Client c1 = new Client(
                InetAddress.getByName(null),
                Server.PORTO,
                KeyEvent.VK_UP,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_RIGHT);

        Client c2 = new Client(
                InetAddress.getByName(null),
                Server.PORTO,
                KeyEvent.VK_W,
                KeyEvent.VK_A,
                KeyEvent.VK_S,
                KeyEvent.VK_D);

        c1.clientRun();
        c2.clientRun();
    }
}

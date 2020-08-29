package FilesTransferApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class acting as a server in devices discovery with the use of udp in a local network
 */
public class UdpDiscoveryServer implements Runnable{

    //Default port on which server listens
    private int port = 6666;

    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            DatagramSocket socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            while (true) {
                //Receive a packet
                byte[] recvBuf = new byte[1600];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();

                //Flag to determine if address belongs to an interface of device which runs this code
                boolean ownAddress = false;
                Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                //iteration over device interfaces
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                    //iteration over interface IP addresses
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

                        String address = interfaceAddress.getAddress().getHostAddress();

                        if (address == null) {
                            continue;
                        }

                        if(address.equals(packet.getAddress().getHostAddress())){
                            //Received packet source address belongs to one of the current device interfaces
                            ownAddress = true;
                        }

                    }
                }

                //if discovery request comes from other device
                if (message.equals("DISCOVER_FUIFSERVER_REQUEST") && !ownAddress) {

                    byte[] sendData = "DISCOVER_FUIFSERVER_RESPONSE".getBytes();

                    //Send a response
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                }
            }
        } catch(IOException ex){
            Logger.getLogger(UdpDiscoveryServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the number of the port on which server listens
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the number of the port on which the server listens
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
}

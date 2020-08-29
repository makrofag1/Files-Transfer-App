package FilesTransferApp;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Class acting as a client in devices discovery with the use of udp in a local network
 */
public class UdpDiscoveryClient implements Runnable{

    //list keeps datagrams form discovered devices
    private ArrayList<DatagramPacket> devicesList = new ArrayList<>();

    //number of the port on which client sends discovery packets
    private int port = 6666;
    //size of the buffer for the reply packets in bytes, possibly should be equal to link MTU
    private int bufferSize = 1600;
    //time of awaiting for a reply
    private int timeout_millis = 1000;
    //time between iterations of sending discovery packets
    private int threadSleep_millis = 5000;

    @Override
    public void run() {
        while(true) {

            //Open a random port to send the package
            DatagramSocket c = null;
            try {
                c = new DatagramSocket();
                c.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println("Couldn't open DatagramSocket!");
                return;
            }

            byte[] sendData = "DISCOVER_FUIFSERVER_REQUEST".getBytes();

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = null;
            try {
                interfaces = NetworkInterface.getNetworkInterfaces();

                //Iterate over all device interfaces
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }
                    //Iterate over all interface address to send discovery packet
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }

                        // Send the broadcast package!
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
                        c.send(sendPacket);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println("No interfaces");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Wait for a response
            byte[] recvBuf = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            try {
                c.setSoTimeout(timeout_millis);
                c.receive(receivePacket);
            } catch (SocketTimeoutException e){
                //System.out.println("Timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //We have a response
            if(receivePacket.getAddress() != null){

                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();
                if (message.equals("DISCOVER_FUIFSERVER_RESPONSE")) {
                    //Add response packet from a discovered device to the list of currently visible devices
                    addDeviceDatagram(receivePacket);
                }
            }

            try {
                Thread.sleep(threadSleep_millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Close the port!
            c.close();
        }


    }

    public int getPort() {
        return port;
    }

    /**
     * Set a port number to which discovery packets will be send
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }


    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Set a byte[] buffer size where received Datagram will be saved
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getTimeout_millis() {
        return timeout_millis;
    }

    /**
     * Set a timeout value of waiting for a response on send request
     * @param timeout_millis
     */
    public void setTimeout_millis(int timeout_millis) {
        this.timeout_millis = timeout_millis;
    }


    public int getThreadSleep_millis() {
        return threadSleep_millis;
    }

    /**
     * Set sleep time in milliseconds of a thread after single client execution
     * @param threadSleep_millis
     */
    public void setThreadSleep_millis(int threadSleep_millis) {
        this.threadSleep_millis = threadSleep_millis;
    }

    /**
     * Returns a list of all response datagrams from other devices and clears a list.
     * @return
     */
    public synchronized ArrayList<DatagramPacket> getDevicesList(){
        ArrayList<DatagramPacket> tmp = new ArrayList<>(devicesList);
        devicesList.clear();
        return tmp;
    }

    /**
     * Adda received datagram packet to the list of all received datagram packets
     * @param dp
     */
    private synchronized void addDeviceDatagram(DatagramPacket dp){
        devicesList.add(dp);
    }
}

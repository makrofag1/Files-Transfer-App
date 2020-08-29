package FilesTransferApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) throws IOException {

        UdpDiscoveryClient udc = new UdpDiscoveryClient();
        UdpDiscoveryServer uds = new UdpDiscoveryServer();
        FileReceiver fr = new FileReceiver();

        Thread discoveryServer = new Thread(uds);
        Thread discoveryClient = new Thread(udc);
        Thread fileReceiverServer = new Thread(fr);

        discoveryClient.start();
        discoveryServer.start();
        fileReceiverServer.start();

        MainWindow window = new MainWindow(fr);

        new Timer().schedule(new ShowDiscoveredDevices(udc, window), 1000, 5000);



    }
}

class ShowDiscoveredDevices extends TimerTask {
    private HashMap<String, DatagramPacket> hashMap1 = new HashMap<>();
    private HashMap<String, Short> hashMap2 = new HashMap<>();
    private Short timeoutValue = 4;
    private UdpDiscoveryClient udc;
    private MainWindow window;

    public ShowDiscoveredDevices(UdpDiscoveryClient udc, MainWindow window){
        this.udc = udc;
        this.window = window;
    }

    @Override
    public void run() {
        //Take from a list
        ArrayList<DatagramPacket> devices = udc.getDevicesList();

        //Check if devices object is empty
        if (devices != null && !devices.isEmpty()) {
            for (DatagramPacket x : devices) {
                if (hashMap1.containsKey(x.getAddress().getHostAddress())) {
                    String address = x.getAddress().getHostAddress();
                    Short value = hashMap2.get(address);
                    hashMap2.replace(address, value, timeoutValue);
                } else {
                    hashMap1.put(x.getAddress().getHostAddress(), x);
                    hashMap2.put(x.getAddress().getHostAddress(), timeoutValue);
                }
            }

            if (devices.size() != hashMap2.size()) {
                ArrayList<String> absentDevices = new ArrayList<>();
                for (String x : hashMap2.keySet()) {
                    for (DatagramPacket packet : devices) {
                        if (x.equals(packet.getAddress().getHostAddress())) {
                            continue;
                        }
                        absentDevices.add(x);

                    }
                }
                for (String x : absentDevices
                ) {
                    Short value = hashMap2.get(x);
                    if (value > 1) {
                        hashMap2.replace(x, value, (short) (value - 1));
                    } else {// if hashMap2 value == 0 -> remove
                        hashMap2.remove(x);
                        hashMap1.remove(x);

                    }
                }
            }

        } else { // if devices object is empty hashMap2 values = values - 1
            for (String x : hashMap2.keySet()) {
                Short value = hashMap2.get(x);
                if (value > 1) {
                    hashMap2.replace(x, value, (short) (value - 1));
                } else {// if hashMap2 value == 0 -> remove
                    hashMap2.remove(x);
                    hashMap1.remove(x);
                    //controlSum = controlSum - 1;
                }

            }
        }

        window.setComboBoxitems(hashMap1);


        /*for (String x : hashMap1.keySet()
        ) {
            System.out.println(hashMap1.get(x).getAddress().getHostName() + " | " + x);
        }*/

    }
}

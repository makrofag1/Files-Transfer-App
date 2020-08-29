package FilesTransferApp;

import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Class takes a process of sending a file
 */
public class FileSender {
    //path to a file we want to send
    private String path;
    //ip address of the receiving end
    private String ip;
    //default port number for we send packets
    private int port = 6666;


    public FileSender(String pathToFile, String receiverIpAddress) {
        path = pathToFile;
        ip = receiverIpAddress;
    }

    public FileSender(){

    }

    /**
     * Load a file we want to send to a computer memory
     */
    //TODO consider setting a maximal file size constraint
    private byte[] loadFile(File fileToSend) {
        DataInputStream fis = null;
        try {
            fis = new DataInputStream(new FileInputStream(fileToSend));
            int count;
            byte[] buffer = new byte[(int) fileToSend.length()];
            while ((count = fis.read(buffer)) >= 0) {
                //while the end of the file wasn't meet read bytes
            }
            System.out.println("File loaded");
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //If file wasn't read return 0 bytes
        return new byte[0];

    }

    /**
     * Get the name of the file
     * @param file
     * @return
     */
    private String getFileName(File file) {
        return file.getName().split("\\.")[0];
    }

    /**
     * Get the extension of the file
     * @param file
     * @return
     */
    private String getFileExtension(File file) {
        return "." + file.getName().split("\\.")[1];
    }

    /**
     * Prepare FileToTransfer object to transfer by filling with the data
     * @param file
     * @return
     */
    private FileToTransfer prepareObject(File file) {
        FileToTransfer ftt = new FileToTransfer();
        ftt.setExtension(getFileExtension(file));
        ftt.setName(getFileName(file));
        ftt.setFile(loadFile(file));
        return ftt;
    }

    /**
     * Send a file
     * @throws IOException
     * @throws InterruptedException
     */
    public void send() throws IOException, InterruptedException {
        System.out.println("IP: " + ip + " | " + "port: " + port + " | " + "path: " + path);
        Socket client = new Socket(ip, port);
        File fileToSend = new File(path);

        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        oos.writeObject(prepareObject(fileToSend));
        Thread.sleep(1000);
        oos.flush();
        oos.close();
        client.close();
        System.out.println("Plik zostal wyslany");
    }

    /**
     * Method sends a request if receiving end will accept a file
     * @throws IOException
     */
    public void sendRequest() throws IOException {
        File file = new File(path);
        FileToTransfer ftt = new FileToTransfer();
        ftt.setRequest(true);
        ftt.setSize_bytes((int) file.length());
        ftt.setName(file.getName());
        System.out.println("path = " + path);
        System.out.println("getFileExtension(file) = " + getFileExtension(file));
        ftt.setExtension(getFileExtension(file));
        Socket client;
        try{
            client = new Socket(ip, port);
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(ftt);
            oos.flush();
            oos.close();
            client.close();
            System.out.println("Zapytanie zostalo wyslane");
        }catch(ConnectException e){
            System.out.println("Nie udalo sie nawiazac polaczenia z adresem " + ip + ":" + port);
            JOptionPane.showMessageDialog(new JFrame(),
                    "Nie udalo sie nawiazac polaczenia z adresem " + ip + ":" + port);
        }
    }

    /**
     * Method sends response to the sending end with the reply if a file will be accepted
     * @param dstIp
     * @param dstPort
     * @param accept
     * @throws IOException
     */
    public void sendResponse(String dstIp, int dstPort, boolean accept) throws IOException {
        FileToTransfer ftt = new FileToTransfer();
        ftt.setResponse(true);
        ftt.setAccept(accept);
        System.out.println("Proba wyslania odpowiedzi do " + dstIp  + ":" + port);

        Socket client = new Socket(dstIp, port);

        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        oos.writeObject(ftt);
        //Thread.sleep(1000);
        oos.flush();
        oos.close();
        client.close();
        System.out.println("Odpowiedz zostala wyslana");
    }
}

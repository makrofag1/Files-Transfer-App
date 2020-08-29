package FilesTransferApp;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class acting as a server in receiving a file, sends replies whether file was accepted or not and take the process of
 * receiving a file
 */
public class FileReceiver implements Runnable {

    // default port number on which listening takes place
    private int port = 6666;
    // variable used for stopping a server
    private boolean run = true;
    //socket for listening
    private ServerSocket server;
    private Socket socket;
    //reference to file sender class instance which is used for sending a file
    private FileSender fsReference;

    public FileReceiver() throws IOException {
        server = new ServerSocket(port);
        socket = new Socket();
    }

    /**
     * Method provides a body to Runnable interface run method used for multithreading
     */
    @Override
    public void run() {
        while(run){
            try {
                socket = server.accept();
                System.out.println("Zaakceptowano polaczenie od " + socket.getInetAddress().getHostAddress());
                deserialize();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }

        }
        try {
            server.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Method deserialize received object and saves it in the path specified in the constructor
     * @throws IOException
     * @throws ClassNotFoundException
     */
    //TODO consider to split into more functions
    private void deserialize() throws IOException, ClassNotFoundException, InterruptedException {
        //deserialize received data
        FileToTransfer ftt = null;
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ftt = (FileToTransfer) inputStream.readObject();

        //if received data is a request to accept a file
        if(ftt.isRequest()){
            //create and show option dialog
            Object[] options = {"Yes",
                    "No"};
            int n = JOptionPane.showOptionDialog(new JFrame(),
                    "Host " + socket.getInetAddress().getHostName() + " on IP address: " + socket.getInetAddress().getHostAddress() +
                            " wants to send a " + ftt.getName() + ftt.getExtension() + " with size of " + ftt.getSize_bytes() + " bytes to you.",
                    "Do you accept?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            if(n == JOptionPane.YES_OPTION){
                System.out.println("Wyslano odpowiedz akceptujaca otrzymanie pliku n: " + n + " yes_option: " + JOptionPane.YES_OPTION);
                new FileSender().sendResponse(socket.getInetAddress().getHostAddress(), socket.getPort(), true);
            }else if(n == JOptionPane.NO_OPTION){
                new FileSender().sendResponse(socket.getInetAddress().getHostAddress(), socket.getPort(), false);
                System.out.println("Wyslano odpowiedz odrzucajaca otrzymanie pliku");
            }
        }else if(ftt.isResponse() && ftt.isAccept()){//if received response accepting a file
            System.out.println("Otrzymano odpowiedz akceptujaca otrzymanie pliku");
            //send
            fsReference.send();

        }else if(ftt.isResponse() && !ftt.isAccept()){//if received response refusing a file
            System.out.println("Wyslano odpowiedz odrzucajaca otrzymanie pliku");
            //show information to a user
            JOptionPane.showMessageDialog(new JFrame(),
                    "Host " + socket.getInetAddress().getHostName() + " on IP address: " + socket.getInetAddress().getHostAddress() +
                            " refused to receive a file " + ftt.getName() + ftt.getExtension() + " with size of " + ftt.getSize_bytes());

        }else if(ftt.isTransfer()){//if received data is a file
            System.out.println("Otrzymano plik");
            //open file chooser to choose a location to save a file
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(new JFrame());

            if(returnVal == JFileChooser.APPROVE_OPTION){
                String path = fc.getSelectedFile().getAbsolutePath() + "\\";
                File file = new File(path + ftt.getName() + ftt.getExtension());
                while(!file.createNewFile()){
                    ftt.setName(ftt.getName() + "(1)");
                    file = new File(path + ftt.getName() + ftt.getExtension());
                }
                System.out.println("Utworzono plik: " + ftt.getName() + ftt.getExtension() + '\n'
                        + "W katalogu: " + path);
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    stream.write(ftt.getFile());
                    System.out.println("Zapisano bajty");
                }
            }
            //if location wasn't chosen discard file
        }

    }


    /**
     * Function returns true if main while loop works
     * @return
     */
    public boolean isRun() {
        return run;
    }

    /**
     * Function terminates listening for a file when setted to false
     * @param run
     */
    public void setRun(boolean run) {
        this.run = run;
    }

    /**
     * Get FileSender instance reference used for sending files
     * @return
     */
    public FileSender getFsReference() {
        return fsReference;
    }

    /**
     * Set FileSender instance reference used for sending a file
     * @param fsReference
     */
    public void setFsReference(FileSender fsReference) {
        this.fsReference = fsReference;
    }
}

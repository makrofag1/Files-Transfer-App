package FilesTransferApp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Class representing a device in combobox data model
 */
class Device{
    public String ip;
    public String hostName;
    public boolean isEmpty = true;

    @Override
    public String toString() {
        return hostName + " | " + ip;
    }
}

/**
 * Program window controller class
 */
public class MainWindow extends JPanel{
    private JButton chooseFileButton;
    private JButton sendButton;
    private JComboBox<Device> hostsComboBox;
    private JLabel comboBoLabel;
    private JTextField urlTextField;
    private JLabel infoLabel;
    private JFrame frame;
    private FileReceiver fileReceiverReference;

    /**
     * Class constructor requires a reference to FileReceiver instance listening for a incoming files
     * @param fileReceiverReference
     */
    public MainWindow(FileReceiver fileReceiverReference){

        this.fileReceiverReference = fileReceiverReference;

        //construct components
        chooseFileButton = new JButton ("Wybierz plik do przesłania");
        sendButton = new JButton ("Wyślij plik");
        hostsComboBox = new JComboBox<Device> ();
        comboBoLabel = new JLabel ("Wybierz komputer docelowy:");
        urlTextField = new JTextField (5);
        infoLabel = new JLabel ("");

        //adjust size and set layout
        setPreferredSize (new Dimension (944, 574));
        setLayout (null);

        //add components
        add (chooseFileButton);
        add (sendButton);
        add (hostsComboBox);
        add (comboBoLabel);
        add (urlTextField);
        add (infoLabel);

        //set component bounds (only needed by Absolute Positioning)
        chooseFileButton.setBounds (35, 155, 195, 45);
        sendButton.setBounds (35, 240, 195, 45);
        hostsComboBox.setBounds (35, 90, 240, 25);
        comboBoLabel.setBounds (35, 50, 240, 30);
        urlTextField.setBounds (270, 155, 595, 45);
        infoLabel.setBounds (270, 240, 595, 45);

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(MainWindow.this);

                if(returnVal == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();

                    urlTextField.setText(file.getPath());
                    System.out.println(file.getPath());
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //take information about device from combobox
                Device device = hostsComboBox.getItemAt(hostsComboBox.getSelectedIndex());
                //If text in a textfield showing file url isn't empty and taken device info isn't null
                if(urlTextField.getText() != null && !urlTextField.getText().equals("") && !(device == null)){
                    //prepare to send a file
                    FileSender fileSender = new FileSender(urlTextField.getText(), device.ip);
                    System.out.println("urlTextField.getText() = " + urlTextField.getText() + " device.ip = " + device.ip);
                    try {
                        //try to send a request to a receiving node to ask if receiver agree to receive a file
                        fileSender.sendRequest();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    //give this fileSender instance reference to a fileReceiver to be able to send a file in the case
                    //of positive response from the receiving end
                    fileReceiverReference.setFsReference(fileSender);
                }else{ //if file wasn't chosen or we deleted url from textfield show message that file url is empty
                    System.out.println("Pusta sciezka pliku");
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Pusta sciezka pliku. Wybierz plik, ktory chcesz wyslac");
                }



            }
        });

        frame = new JFrame ("Files Uploader");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add (this);
        frame.pack();
        frame.setVisible (true);

    }

    /**
     * Takes a HashMap with a String device IP address and corresponding DatagramPacket from that device and sets combobox items
     * @param devices
     */
    public void setComboBoxitems(HashMap<String, DatagramPacket> devices){
        hostsComboBox.removeAllItems();
        for (String x: devices.keySet()
        ) {
            Device device = new Device();
            device.hostName = devices.get(x).getAddress().getHostName();
            device.ip = x;
            device.isEmpty = false;
            hostsComboBox.addItem(device);
        }
    }
}

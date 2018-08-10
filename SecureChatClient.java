/* CS 1501
   Primitive chat client. 
   This client connects to a server so that messages can be typed and forwarded
   to all other clients.  Try it out in conjunction with ImprovedChatServer.java.
   You will need to modify / update this program to incorporate the secure elements
   as specified in the Assignment sheet.  Note that the PORT used below is not the
   one required in the assignment -- for your SecureChatClient be sure to 
   change the port that so that it matches the port specified for the secure
   server.
*/
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.math.BigInteger;

public class SecureChatClient extends JFrame implements Runnable, ActionListener {

    public static final int PORT = 8765;

    JTextArea outputArea;
    JLabel prompt;
    JTextField inputField;
    String myName, serverName;
	  Socket connection;
    SymCipher cipher;
    BigInteger E;
    BigInteger N;
    ObjectOutputStream myWriter;
    ObjectInputStream myReader;

    public SecureChatClient ()
    {
        try {

        myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
        serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
        InetAddress addr =
                InetAddress.getByName(serverName);
        connection = new Socket(addr, PORT);   // Connect to server with new
                                               // Socket

        // output
        myWriter = new ObjectOutputStream(connection.getOutputStream());
        myWriter.flush();

        // input
        myReader = new ObjectInputStream(connection.getInputStream());

        this.setTitle(myName);      // Set title to identify chatter      

        E = (BigInteger) myReader.readObject();
        N = (BigInteger) myReader.readObject();
        System.out.println("E: " + E);
        System.out.println("N: " + N);

        //Get the type of cipher to use
        String cipherString = (String) myReader.readObject();
        if (cipherString.equals("Add")) {
            cipher = new Add128();
            System.out.println("Cipher used: Add128");
        } else if (cipherString.equals("Sub")) {
            cipher = new Substitute();
            System.out.println("Cipher used: Substitute");
        }

        //Sends the encrypted key
        myWriter.writeObject(new BigInteger(1,cipher.getKey()).modPow(E, N));
        myWriter.flush();

        //Get and output key
        byte [] key = cipher.getKey();
        System.out.println("Key: ");
        for (int i = 0 ; i < key.length; i++) {
            System.out.print(key[i]);
        }
        System.out.println();

        myWriter.writeObject(cipher.encode(myName)); // send user name to server
        myWriter.flush();


        Box b = Box.createHorizontalBox();  // Set up graphical environment for
        outputArea = new JTextArea(8, 30);  // user
        outputArea.setEditable(false);
        b.add(new JScrollPane(outputArea));

        outputArea.append("Welcome to the Chat Group, " + myName + "\n");

        inputField = new JTextField("");  // This is where user will type input
        inputField.addActionListener(this);

        prompt = new JLabel("Type your messages below:");
        Container c = getContentPane();

        c.add(b, BorderLayout.NORTH);
        c.add(prompt, BorderLayout.CENTER);
        c.add(inputField, BorderLayout.SOUTH);

        Thread outputThread = new Thread(this);  // Thread is to receive strings
        outputThread.start();                    // from Server

		    addWindowListener(
            new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    { 
                        try {
                            myWriter.writeObject(cipher.encode("CLIENT CLOSING"));
                            myWriter.flush();
                        } catch (IOException IOex) {
                            System.out.println("PROBLEM CLOSING");
                        } finally {
                            System.exit(0);
                        }
                    }
                }
            );

        setSize(500, 200);
        setVisible(true);

        }
        catch (Exception e)
        {
            System.out.println("Problem starting client!");
        }
    }

    public void run()
    {
        while (true)
        {
             try {
                //Read in encrypted messasge
                byte [] encrypted = (byte[]) myReader.readObject();

                System.out.println("Encrypted message received: ");
                for (int i = 0; i < encrypted.length; i++) {
                  System.out.print(encrypted[i]);
                }
                System.out.println();
                
                //Decode message
                String currMsg = cipher.decode(encrypted);;

                outputArea.append(currMsg+"\n");
                System.out.println("Decrypted bytes: ");
                byte [] decrypted = currMsg.getBytes();
                for (int i = 0; i < decrypted.length; i++) {
                  System.out.print(decrypted[i]);
                }
                System.out.println();
                System.out.println("Message: "+ currMsg);
                System.out.println();
             }
             catch (Exception e)
             {
                System.out.println(e +  ", closing client!");
                break;
             }
        }
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e)
    {
        String currMsg = e.getActionCommand();      // Get input value
        inputField.setText("");
        try {
          String toEncode = myName + ": " + currMsg;
          byte [] originalBytes = toEncode.getBytes();
          byte [] encrypted = cipher.encode(toEncode);
          myWriter.writeObject(encrypted);
          myWriter.flush();

          System.out.println("Sent: "+ toEncode);
          System.out.println("Corresponding Bits: ");
          for (int i = 0; i < originalBytes.length; i++) {
            System.out.print(originalBytes[i]);
          }
          System.out.println();
          System.out.println("Encrypted message: ");
          for (int i = 0; i < encrypted.length; i++) {
            System.out.print(encrypted[i]);
          }
          System.out.println('\n');


        } catch (IOException ioe) {
          outputArea.append("Error occurred");
        }
    }

    public static void main(String [] args)
    {
         SecureChatClient JR = new SecureChatClient();
         JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
}
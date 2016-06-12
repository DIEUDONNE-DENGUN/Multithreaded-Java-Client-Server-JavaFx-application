/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 *
 * @author DENGUN-WATIB
 */
public class clientController implements Initializable {

    private Label label;
    @FXML
    private TextArea textAreaFieldDisplayMessage;
    @FXML
    private TextField textMessageField;
    @FXML
    private Button btnConnectServer;
    @FXML
    private Button btnDisconnectFromServer;

    private String messageText = "";

    int count = 1;

    private String messageReceived = "";
    String message;

    private static String serverName = "localhost";

    private Socket socket;
    ExecutorService executor = Executors.newCachedThreadPool();

    private int portNumber = 5000;

    private ObjectInputStream is;

    private ObjectOutputStream os;
    @FXML
    private Button sentMessageBtn;
    @FXML
    private TextField portNumberField;
    @FXML
    private TextField serverAddressField;
    @FXML
    private Label userNameLabel;

    int counter = 1;
    @FXML
    private TextArea eventClientTextArea;
    @FXML
    private TextArea userListPanelClient;
    @FXML
    private Button viewUserOnlineBtn;

    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        textAreaFieldDisplayMessage.setEditable(false);
        textMessageField.setEditable(false);
        setDisConnectBtnDisabled(true);

        sentMessageBtn.setDisable(true);
        textMessageField.setPromptText("Enter your name here");
        //executor.execute(task_connect);

        viewUserOnlineBtn.setDisable(true);
        portNumberField.appendText("5000");
        sentMessageBtn.setText("Send Username");
        serverAddressField.appendText("localhost");
        
    }
    

 
    
    
    
    Task task_connect = new Task<Void>() {
        @Override
        public Void call() throws Exception {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {

                    try {
                        connectToServerMethod();
                        setClientStreams();
                        new ClientThread().start();

                    } catch (IOException ex) {

                        //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            return null;
        }
    };

    Task set_stream = new Task<Void>() {
        @Override
        public Void call() throws Exception {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    setClientStreams();

                }
            });

            return null;
        }
    };

    private void sentMessage() {

        if (count == 1) {
            messageText = textMessageField.getText();

            userNameLabel.setText("Welcome " + messageText);

            count++;
            textMessageField.setPromptText("What's in your mind? say something ...");
            sentMessageBtn.setText("Send Message");

        } else {

            messageText = textMessageField.getText();

        }
        try {
            if (messageText.isEmpty()) {

                displayMessage("Please enter a message to sent");
            } else {

                os.writeObject(messageText);
            }
        } catch (IOException ex) {

            displayMessage("Error writing to server socket");
            //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
        }

            //displayMessage("Client >>  " + messageText);
        //displayMessage("Server >>" + "Welcome "+ name +" to the chat room. Feel free to chat and talk to everyone in the room");
        textMessageField.setText("");

    }

    @FXML
    private void connectToServer(ActionEvent event) {

        try {
            connectToServerMethod();
        } catch (IOException ex) {
            
            displayMessageEvent("There were some errors connecting to server " + ex);
            //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //set up the socket streams and start communicating with the client
        setClientStreams();
        new ClientThread().start();

    }

    public void connectToServerMethod() throws IOException {
        //this method is used to initiate socket communication with the server;

        displayMessageEvent("Attempting connection with server ...");

        try {

            try {

                portNumber = Integer.parseInt(portNumberField.getText());
            } catch (NumberFormatException e) {

                displayMessageEvent("Invalid port number value. Please specify right integer port nuumber");
            }
            serverName = serverAddressField.getText();

            if (serverName.isEmpty()) {

                displayMessageEvent("Specify the server address to connect and communicate with.");
            }
            socket = new Socket(InetAddress.getByName(serverName), portNumber);
            //print success message to client that, the connection was a success
            System.out.println("Successfully connected to server with hostname " + socket.getInetAddress().getHostName());

            displayMessageEvent("Successfully connected to server with hostname " + socket.getInetAddress().getHostName());
        } catch (UnknownHostException ex) {
            //there were some errors connecting to the system.

            System.out.println("There were some errors connecting to server " + ex);
            
            

            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setClientStreams() {
        try {
            //this line will set up the client input and output streams with the server program

            System.out.println("Setting up client streams ...");

            displayMessageEvent("Setting up client streams ...");

            os = new ObjectOutputStream(socket.getOutputStream());

            is = new ObjectInputStream(socket.getInputStream());

            System.out.println("Client streams were successfully set up");
            displayMessageEvent("Client streams were successfully set up");

            textMessageField.setEditable(true);
            setConnectBtnDisabled(true);
            setDisConnectBtnDisabled(false);
            sentMessageBtn.setDisable(false);
            portNumberField.setEditable(false);
            viewUserOnlineBtn.setDisable(false);
            serverAddressField.setEditable(false);

        } catch (IOException ex) {
            //there were some errors setting up the client streams

            System.out.println("There were some errors setting up the client streams " + ex);

            displayMessageEvent("There were some errors setting up the client streams because server is down " + ex);
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void viewOnlineUsers(ActionEvent event) {
        //this button click will be used to sent a request of the liat of all users current online

        userListPanelClient.setVisible(true);

        userListPanelClient.clear();

        try {
            os.writeObject("whois");

        } catch (IOException ex) {

            displayMessageEvent("Error requesting for the users online");
            //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void sentMessageFromTextField(javafx.scene.input.KeyEvent event) {
        
           if(event.getCode()==KeyCode.ENTER){
           
             sentMessage();
           }
    }

    class ClientThread extends Thread {

      //this method will handle the sending and reading of messages fron/to server 
        //String clientName =is.readLine().trim();
        public void run() {
            while (true) {
                try {

                    //displayMessage("Client is now ready to start reading from server ...");
                    try {

                        //this check if get the currently logged in user name from the first message he sents and read from the server
                        if (counter == 1) {

                            String name = (String) is.readObject();

                            userNameLabel.setText("Welcome " + name);
                            counter++;
                        }
                        message = (String) is.readObject();

                        //cehck if the user request for users who are online
                        if (message.contains(")")) {

                            userListPanelClient.appendText(message + "\n");
                        } else if (message.contains("the users connected at")) {

                            userListPanelClient.appendText(message + "\n");
                        }else if(message.contains("resetted")){
                                
                              displayMessage(message);
                                break;
                            }
                        
                        else {

                            displayMessage(message);

                        }

                    } catch (ClassNotFoundException ex) {

                        displayMessageEvent("Unknown message read from server");
                        // Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    System.out.println(message);

                } catch (IOException ex) {

                    System.out.println("Unknown error " + ex);
                    //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

            }//end of while loop
              disconnectedByServer();
        }
        
         
        //closeConnection();
    }
    
    
    public void disconnectedByServer(){
    
          displayMessageEvent("Closing client connection  since server has gone down...");
         setConnectBtnDisabled(false);
            setDisConnectBtnDisabled(true);
            textMessageField.setPromptText("Enter your name here");
            textMessageField.setEditable(false);
            portNumberField.setEditable(true);
            serverAddressField.setEditable(true);
            
             if(userListPanelClient.isVisible()){
            userListPanelClient.setVisible(false);
             }
             
              if(!viewUserOnlineBtn.isDisable()){
              //viewUserOnlineBtn.setDisable(true);
                  setViewUsersBtnDisabled(true);
            
              }
            sentMessageBtn.setText("Send Username");
            
           // sentMessageBtn.setDisable(true);
            setSentMessagBtnDisabled(true);

              disconnect();
             displayMessageEvent("Successfully disconnected from server program");
    
             
    }

    @FXML
    private void disconnectServer(ActionEvent event) {

        try {
             displayMessageEvent("Closing client connection ...");
            
            os.writeObject("logout");

            setConnectBtnDisabled(false);
            setDisConnectBtnDisabled(true);
            textMessageField.setPromptText("Enter your name here");
            textMessageField.setEditable(false);
            portNumberField.setEditable(true);
            serverAddressField.setEditable(true);
            userListPanelClient.setVisible(false);
            viewUserOnlineBtn.setDisable(true);
            sentMessageBtn.setText("Send Username");
            sentMessageBtn.setDisable(true);

              disconnect();
             displayMessageEvent("Successfully disconnected from server program");
             
             
            return;
        } catch (IOException ex) {

            displayMessageEvent("Error logging out of the chatroom " + ex);
            //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    	public void disconnect() {
		try { 
			if(is != null)is.close();
		}
		catch(Exception e) {
                
                    displayMessageEvent("Error closing input stream ..." +e);
                } // not much else I can do
		try {
			if(os != null)os.close();
		}
		catch(Exception e) {
                   
                    displayMessageEvent("Error closing output stream .." + e);
                } // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {
                
                    displayMessageEvent("Error closing the socket connection" + e);
                } // not much else I can do
		 
        Runtime.getRuntime().halt(0);
		
			
	}


    public void displayMessage(String message) {

        Task display_message = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        textAreaFieldDisplayMessage.appendText(message + "\n");
                    }
                });

                return null;
            }
        };

        executor.execute(display_message);

    }

    public void setDisConnectBtnDisabled(final boolean b) {

        Task diable_disconnect_btn = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        btnDisconnectFromServer.setDisable(b);
                    }
                });

                return null;
            }
        };

        executor.execute(diable_disconnect_btn);
    }

      public void setConnectBtnDisabled(final boolean b) {

        Task diable_connect_btn = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        btnConnectServer.setDisable(b);
                    }
                });

                return null;
            }
        };

        executor.execute(diable_connect_btn);
    }
    
    public void setSentMessagBtnDisabled(final boolean b) {

        Task diable_sentMessage_btn = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        sentMessageBtn.setDisable(b);
                    }
                });

                return null;
            }
        };

        executor.execute(diable_sentMessage_btn);

    }
        
       public void setViewUsersBtnDisabled(final boolean b) {

        Task diable_view_users_btn = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        viewUserOnlineBtn.setDisable(b);
                    }
                });

                return null;
            }
        };

        executor.execute(diable_view_users_btn);
    }
     
    public void displayMessageEvent(String message) {

        Task display_message_event = new Task<Void>() {
            @Override
            public Void call() throws Exception {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        eventClientTextArea.appendText(message + "\n");
                    }
                });

                return null;
            }
        };

        executor.execute(display_message_event);

    }

    @FXML
    private void sentMessagetServer(ActionEvent event) {

        sentMessage();
    }
}

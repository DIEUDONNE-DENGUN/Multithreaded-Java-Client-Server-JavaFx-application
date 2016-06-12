/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import server.ChatMessage;

/**
 * FXML Controller class
 *
 * @author DENGUN-WATIB
 */
public class serverController implements Initializable {
    @FXML
    private TextArea textAreaFieldDisplayMessage;
    @FXML
    private TextArea eventTextArea;
    @FXML
    private Button btnStartServer;
    @FXML
    private Button btnStopServer;
    @FXML
    private ListView<String> listViewUsers = new ListView<String>();;
    
   
    
    ObservableList<String> data = FXCollections.observableArrayList();
    @FXML
    private TextField portNumberServer;
    
     ExecutorService executor = Executors.newCachedThreadPool();
     
     private Server_Utility server;
     
     private SimpleDateFormat sdf;
     
      int numberLine=1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
       textAreaFieldDisplayMessage.setEditable(false);
       eventTextArea.setEditable(false);
        btnStartServer.setDisable(false);
        server = null;
        btnStopServer.setDisable(true);
        portNumberServer.appendText("5000");
        textAreaFieldDisplayMessage.appendText("Conference Chat Room \n\n");
         
        sdf = new SimpleDateFormat("HH:mm:ss");
        
        eventTextArea.appendText("Event log. \n\n");
        
       
    }    

    
    
    @FXML
    private void startServer(MouseEvent event) {
        
        runServer();
    }


    
    public void runServer(){
        int port;
        
        
           if(server != null) {
			server.stop();
			server = null;
			portNumberServer.setEditable(true);
		       setStartBtnActive(false);
                       setStopBtnActive(true);
			return;
		}
		try {
                    
			port = Integer.parseInt(portNumberServer.getText().trim());
		}
		catch(Exception er) {
			displayMessageEvent("Invalid port number");
			return;
		}
                
                // create a server object and start it
		server = new Server_Utility(port);
		new ServerRunning().start();
                btnStartServer.setDisable(true);
                btnStopServer.setDisable(false);
                portNumberServer.setEditable(false);
    
    }
   

    @FXML
    private void stopServer(MouseEvent event) {
         if(server != null) {
             
                     server.broadcast("[Server] : Server is going down now. All connections will be resetted");
			server.stop();
			server = null;
			portNumberServer.setEditable(true);
		       setStartBtnActive(false);
                       setStopBtnActive(true);
                       data.clear();
//                       eventTextArea.clear();
//                       textAreaFieldDisplayMessage.clear();
			return;
		}
        
    }
    
    
    
     public class Server_Utility {
	// a unique ID for each connection
	private int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// if I am in a GUI
	//private ServerGUI sg;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;
	

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 */
//	public Server_Utility(int port) {
//		this(port, null);
//	}
	
	public Server_Utility(int port) {
		
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
                
	}
	
	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while(keepGoing) 
			{
				// format message saying we are waiting
				displayMessageEvent("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				al.add(t);									// save it in the ArrayList
				t.start();
                                data.add(t.username);
                                 
                                listViewUsers.setItems(data);
                                
                               // displayMessageChat(listViewUsers.getItems());
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				displayMessageEvent("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			displayMessageEvent(msg);
		}
	}		
    /*
     * For the GUI to stop the server
     */
	protected void stop() {
		keepGoing = false;
		// connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/*
	 * Display an event (not a message) to the console or the GUI
	 */
	
	/*
	 *  to broadcast a message to all Clients
	 */
	public synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf ="[ "+ time +" ] :  " + message ;
		// display message on console or GUI
//		if(sg == null)
//			System.out.print(messageLf);
//		else
		displayMessageChat(messageLf);     // append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				displayMessageEvent("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
                                data.remove(i);
                           
				return;
			}
                       // listViewUsers.setItems(data);
		}
	}
	
	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 


	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
                        displayMessageEvent("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
                                
                                sOutput.writeObject("[Server] :  Please enter your name to start communicating ...");
                                
				username = (String) sInput.readObject();
                                
				displayMessageEvent(username + " just connected.");
                                  
                                   sOutput.writeObject("[Server] : "+"Welcome " +username +" to the chatroom. Chatting here is open to those online" );
                                broadcast("[Server]: New user " + username +" just joined the chatroom ");
                                
//                                for(int i = 0; i < al.size(); ++i) {
//						ClientThread ct = al.get(i);
//                                                data.add(ct.username);
//						//writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
//					}
//                                
//                                
//                                listViewUsers.setItems(data);
//                                
                              
			}
			catch (IOException e) {
				displayMessageEvent("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
                    
                    
			boolean keepGoing = true;
			while(keepGoing) {
                            
                            System.out.println("i am into it now");
				// read a String (which is an object)
				try {
				 String	cm = (String) sInput.readObject();
                                        
                                 
                                      if(cm.equalsIgnoreCase("whois")){
                                          
                                      
					// scan al the users connected
                                     
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((numberLine) + ") " + ct.username );
                                                
                                                numberLine ++;
					}
                                        
                                        numberLine=1;
                                      
                                        writeMsg("\nList of the users connected at " + sdf.format(new Date()) + "");
                                        
                                      }else if(cm.equalsIgnoreCase("logout")){
                                      
                                        displayMessageEvent(username + " disconnected from the chat room.");
                                          broadcast(" [Server] : "+username+ " has just left the chat room. Bye Bye "+ username);
					 keepGoing = false;
                                      }else{
                                         
                                          broadcast("< "+username +" > : " + cm);
                                      
                                      }
                                        System.out.println(cm);
                                        
				}
				catch (IOException e) {
					displayMessageEvent(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				displayMessageEvent("Error sending message to " + username);
				displayMessageEvent(e.toString());
			}
			return true;
		}
	}
}

    /*
	 * A thread to run the Server
	 */
	class ServerRunning extends Thread {
		public void run() {
			server.start();         // should execute until if fails
			// the server failed
		
			displayMessageEvent("Server crashed\n");
			server = null;
		}
	}
    
    public void clearTextAreaField() {

        //this method is used to clear the text area field of the server screen
        textAreaFieldDisplayMessage.clear();
    }

    
    public void displayMessageEvent(String message) {

        //this method is used to display messages from the server and clients on the server textarea field
        //display message on the textarea field         
        
        String timeNow=sdf.format(new Date());
       
        
        String time = "["+ sdf.format(new Date()) + "] :  " + message;
        Task display_message = new Task<Void>() {
        @Override
        public Void call() throws Exception {


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                     eventTextArea.appendText( time +"\n");
                    }
                });

             return null;
        }
    };           
        
        executor.execute(display_message);
         
    }
    
    
    
    public void displayMessageChat(String message){
    
          Task display_event = new Task<Void>() {
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
    
            executor.execute(display_event);
         
    }

  
    public void setStopBtnActive(final boolean enableBtn) {

        //this method will be used to disable the start button and enable the stop button when the server has already started
        btnStopServer.setDisable(enableBtn);

    }

    public void setStartBtnActive(final boolean enableBtn) {

        btnStartServer.setDisable(enableBtn);

    }
    
}

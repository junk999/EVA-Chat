package chat;

import java.io.*; 
import java.net.*; 
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import gui.ChatroomController;
import gui.OverviewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; 

public class Client  extends Application
{ 

	final static int ServerPort = 1234; 
	static DataInputStream dis;
	static DataOutputStream dos;
	static DataInputStream cdis;
	static DataOutputStream cdos;


	public static void main(String args[]) throws UnknownHostException, IOException  
	{ 
		launch(args);


	} 

	public void sendMessage(String message) {

	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// +++++++++++++++++++++++++++++++++++++++++++++
		// Layout
		// +++++++++++++++++++++++++++++++++++++++++++++



		// Load FXML file and AnchorPane
		FXMLLoader loader = new FXMLLoader(Client.class.getResource("../gui/Overview.fxml"));
		AnchorPane pane = loader.load();

		// Szene
		Scene scene = new Scene(pane);
		//create Maincontroller
		//		
		OverviewController oc = loader.getController();

		// +++++++++++++++++++++++++++++++++++++++++++++
		// Stage konfigurieren
		// +++++++++++++++++++++++++++++++++++++++++++++

		// Titel setzen
		primaryStage.setTitle("Masse Chat");
		// Szene setzen
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		// Stage anzeigen
		primaryStage.show();
		Scanner scn = new Scanner(System.in); 

		// getting localhost ip 
		//IP adress from the server
		InetAddress ip = InetAddress.getByName("192.168.178.85"); 

		// establish the connection 
		Socket s = new Socket(ip, ServerPort); 
		Socket controllSock = new Socket(ip, ServerPort);

		// obtaining input and out streams 
		dis = new DataInputStream(s.getInputStream()); 
		dos = new DataOutputStream(s.getOutputStream()); 

		cdis = new DataInputStream(controllSock.getInputStream()); 
		cdos = new DataOutputStream(controllSock.getOutputStream());

		Thread updateOnlineUsers = new Thread(new Runnable()  
		{ 
			@Override
			public void run() { 

				while (true) {

					try {

						cdos.writeUTF("getOwnUsername");
						oc.setLabelUsername(cdis.readUTF());


						cdos.writeUTF("getConnectedUsernames");
						String onlineUsers = cdis.readUTF();
						oc.updateOnlineUsers(onlineUsers);


					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// 5 Sekunden warten damit der Server nicht mit Anfragen �berh�uft wird.
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} 
			} 
		});
		updateOnlineUsers.start();


		// username thread 
		/*
     Thread sendUsername = new Thread(new Runnable()  
     { 
         @Override
         public void run() { 
             while (true) { 

                 // read the message to deliver. 
            	 String username = scn.nextLine(); 


                 try { 
                     // write on the output stream 
                     dos.writeUTF(username); 
                 } catch (IOException e) { 
                     e.printStackTrace(); 
                 } 
             } 
         } 
     });
		 */
	}

	public static void openChatroom(String user){
		FXMLLoader loader = new FXMLLoader(Client.class.getResource("../gui/Chatroom.fxml"));

		try {
			AnchorPane secondaryLayout = loader.load();
			Scene chatroomScene= new Scene(secondaryLayout);
			Stage newWindow = new Stage();
			newWindow.setTitle("Chatroom");
			newWindow.setScene(chatroomScene);
			newWindow.show();
			ChatroomController cc = loader.getController();
			cc.setChatpartner(user);

			Thread sendMessage = new Thread(new Runnable()  
			{ 
				@Override
				public void run(){ 
					while (true) { 

						if (cc.isSendClicked()) {

							try { 
								// write on the output stream 
								dos.writeUTF(cc.getMessage()+"#"+user); 
							} catch (IOException e) { 
								e.printStackTrace(); 
							} 
						}
					} 
				} 
			}); 

			// readMessage thread 
			Thread readMessage = new Thread(new Runnable()  
			{ 
				@Override
				public void run() { 

					while (true) { 
						try { 
							// read the message sent to this client 
							String msg = dis.readUTF(); 
							System.out.println(msg);
							cc.setReceivedMessage(msg);
						} catch (IOException e) { 

							e.printStackTrace(); 
						} 
					} 
				} 
			}); 
			sendMessage.start(); 
			readMessage.start();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void stop(){
	    System.out.println("Stage is closing");
	    System.exit(0);
	}
	
} 
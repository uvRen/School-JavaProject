package ServerPackage;
import java.io.IOException;
import java.net.*;

public class ListenForIncomingConnections implements Runnable {
	//variables
	private ServerSocket 	socketServer;
	private GameServer 		server;
	
	//constructor
	public ListenForIncomingConnections(ServerSocket socketServer, GameServer server) {
		this.socketServer 	= socketServer;
		this.server 		= server;
	}
	
	public void run() {
		try {
			while(true) {
				Socket client 					= socketServer.accept();
				Runnable HandleClientConnection = new HandleClientConnection(client, this.server);
				Thread t 						= new Thread(HandleClientConnection);
				t.start();
			}
		}
		catch (IOException e) {
			
		}
	}
}

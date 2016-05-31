package HelpPackage;

import java.awt.Point;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import ClientPackage.GameClient;
import ServerPackage.GameServer;

public class HandleCommunication {
	/**
	 * Sends a message to a client
	 * @param Clients socket
	 * @param Message to send
	 */
	public static void toClient(Socket clientSocket, String message) {
		try {
			PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream());
			toClient.write(message + "\n");
			toClient.flush();
		} 
		catch(IOException e) {	}
	}
	
	/**
	 * Sends a message to all connect clients to the server
	 * @param If the broadcast is about a specific player, can be null
	 * @param A list of connected clients
	 * @param Which send setting it is
	 * @param If the broadcast require additional data, can be null
	 * @param If the broadcast require additional data, can be null
	 */
	public static void broadcastToClient(String name, ArrayList<GameClient> connectedClients, GameServer.SendSetting setting, Object objectToSend1, Object objectToSend2) {
		switch(setting) {
		case PlayerMoved:
			Point oldPos = (Point)objectToSend1;
			Point newPos = (Point)objectToSend2;
			for(GameClient gc : connectedClients) {
				toClient(gc.getClientSocket(), "@114@" + name + "@" + oldPos.x + "@" + oldPos.y + "@" + newPos.x + "@" + newPos.y + "@");
			}
			break;
		case ClientConnected:
			GameServer server 				= (GameServer)objectToSend2;
			GameClient clientThatConnected 	= (GameClient)objectToSend1;
			
			//send that connection is ok to client
			toClient(clientThatConnected.getClientSocket(), "@116@");
			
			server.addTextToServerMessage(clientThatConnected.getName() + " connected");
			server.addClientToList(clientThatConnected);
			
			for(GameClient gc2 : connectedClients) {
				if(gc2.getName() != name) {
					toClient(gc2.getClientSocket(), "@104@" + name + "@");
				}
				//send all connected client to the client that connected
				toClient(clientThatConnected.getClientSocket(), "@104@" + gc2.getName() + "@");
			}
			
			break;
		case ClientDisconnected:
			GameServer server2 = (GameServer)objectToSend1;
			if(name != null) {
				server2.addTextToServerMessage(name + " disconnected");
				server2.removeClientFromList(name);
				server2.sendClientDisconnectedToGUI(name);
			}
			
			for(GameClient g : server2.getConnectedClientList()) 
				server2.sendMessageToClient(g.getClientSocket(), "@112@" + name + "@");
		
			break;
		case StartGame:
			GameServer server3 = (GameServer)objectToSend1;

			//spawn robots
			server3.getGameplaneHandler().spawnRobots();
			//spawn rubble
			server3.getGameplaneHandler().spawnRubble();
			//send start to all clients
			server3.sendStartToClients();
			break;
		case AgentsTurnToMove:
			for(GameClient gc5 : connectedClients)
				toClient(gc5.getClientSocket(), "@120@");
			break;
		case RemovePlayer:
			for(GameClient gc6 : connectedClients)
				toClient(gc6.getClientSocket(), "@122@" + name + "@");
			break;
		case AddPlayer:
			Player newPlayer = (Player)objectToSend1;
			//send the player (robot) to all clients
			for(GameClient gc3 : connectedClients) {
				toClient(gc3.getClientSocket(), "@110@" + 
												newPlayer.getName() + "@" + 
												newPlayer.getX() + "@" + 
												newPlayer.getY() + "@" + 
												newPlayer.getType().ordinal() + "@" +
												newPlayer.getAttacks() + "@" +
												newPlayer.getSafeTP() + "@");
			}
			break;
		case NextLevel:
			GameServer server4 = (GameServer)objectToSend1;
			// restart game
			server4.sendStopToClients();
			server4.getGameplaneHandler().removeRobotsAndRubble();
			server4.getGameplaneHandler().increaseLevel();
			server4.getGameplaneHandler().spawnRobots();
			server4.getGameplaneHandler().spawnRubble();
			server4.getGameplaneHandler().givePlayerLevelBonus();
			server4.sendStartToClients();
			server4.addTextToLoggingWindow("Server change level to " + server4.getLevel());
			break;
		case ChangedType:
			String nameP1 = (String)objectToSend1;
			String nameP2 = (String)objectToSend2;
			
			for(GameClient gc : connectedClients) {
				if(nameP1 != null) {
					toClient(gc.getClientSocket(), "@130@" + nameP1 + "@");
				}
				if(nameP2 != null) {
					toClient(gc.getClientSocket(), "@130@" + nameP2 + "@");
				}
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Send a message to server
	 * @param PrintWriter object that the client is using to send data to server
	 * @param Message to send
	 */
	public static void toServer(PrintWriter toServer, String message)
	{
		toServer.write(message + "\n");
		toServer.flush();
	}
	
	/**
	 * Saves all server setting to file
	 * @param Settings
	 */
	/*
	public static void writeSettingsToFile(NessesaryServerInformation serverInfo) {
		try {
			PrintStream createFile = new PrintStream(new File("settings.data"));
			//row, col, window, players, port
			createFile.println(serverInfo.getRows());
			createFile.println(serverInfo.getColumns());
			createFile.println(serverInfo.getLogginWindow());
			createFile.println(serverInfo.getPlayers());
			createFile.println(serverInfo.getRobotsPerPlayer());
			createFile.println(serverInfo.getRubble());
			createFile.println(serverInfo.getRobotsPerLevel());
			createFile.println(serverInfo.getRobotChase());
			createFile.println(serverInfo.getSafeTP());
			createFile.println(serverInfo.getAttacks());
			createFile.println(serverInfo.getRoundsPerLevel());
		}
		catch(IOException e) {}
	}
	*/
	
	/**
	 * Read all setting from file
	 * @return Server info
	 */
	/*
	public static NessesaryServerInformation getSettingsFromFile()
	{
		//default values
		NessesaryServerInformation serverInfo = new NessesaryServerInformation(20, 20, 0, 5, 1, 5, 1, 0, 1, 1, 10);
		try {
			@SuppressWarnings("resource")
			BufferedReader readFile = new BufferedReader(new FileReader("settings.data"));
			String input;
			ArrayList<Integer> info = new ArrayList<Integer>();
			while((input = readFile.readLine()) != null)
			{
				info.add(Integer.parseInt(input));
			}
			serverInfo = new NessesaryServerInformation(
					info.get(0), info.get(1), info.get(2), info.get(3), 
					info.get(4), info.get(5), info.get(6), info.get(7), 
					info.get(8), info.get(9), info.get(10));
		} 
		//if the file don't exists, create one with default values
		catch(FileNotFoundException e1) {
			try {
				PrintStream createFile = new PrintStream(new File("settings.data"));
				//row, col, window, players, port
				createFile.println("20");
				createFile.println("20");
				createFile.println("0");
				createFile.println("5");
				createFile.println("1");
				createFile.println("5");
				createFile.println("1");
				createFile.println("0");
				createFile.println("1");
				createFile.println("1");
				createFile.println("10");
			}
			catch(IOException e3) {}
		}
		catch(IOException e2) {}
		
		return serverInfo;
	}
	*/
	
	/**
	 * Find a client by name in the list of connected clients
	 * @param 	Name of client
	 * @param 	List of connected clients
	 * @return	GameClient object
	 */
	public static GameClient findClient(String name, ArrayList<GameClient> connectedClients)
	{
		for(GameClient gc : connectedClients)
		{
			String gcName = gc.getName();
			if(gcName.equals(name))
			{
				return gc;
			}
		}
		return null;
	}
	
	/**
	 * Find a client by socket in the list of connected clients
	 * @param 	Socket that the client is using
	 * @param 	List of connected clients
	 * @return	GameClient object
	 */
	public static GameClient findClient(Socket clientSocket, ArrayList<GameClient> connectedClients)
	{
		for(GameClient gc : connectedClients) {
			Socket gcSocket = gc.getClientSocket();
			if(gcSocket == clientSocket) {
				return gc;
			}
		}
		return null;
	}
}

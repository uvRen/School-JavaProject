package ServerPackage;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import ClientPackage.GameClient;
import HelpPackage.HandleCommunication;
import HelpPackage.ServerGameplane;
import UserInterfaces.ServerGUI;
import HelpPackage.Player;
import java.awt.Point;
import java.io.*;
import java.net.*;

public class GameServer {
	public enum SendSetting { PlayerMoved, 
							  ClientDisconnected, 
							  ClientConnected, 
							  StartGame, 
							  AgentsTurnToMove, 
							  RemovePlayer,
							  NextLevel,
							  ChangedType,
							  AddPlayer
							};
	private ServerSocket 					server;
	private ArrayList<GameClient> 			connectedClients;
	private Thread 							ListenForIncomingConnectionsThread;
	private NessesaryServerInformation 		serverInfo;
	private ServerGUI 						gui;
	private ServerGameplane 				handleGameplane;
	private int 							port = 5000;
	
	/**
	 * GameServer constructor.
	 */
	public GameServer() {
		this.serverInfo = new NessesaryServerInformation();
		getServerSettings();
	}
	
	/**
	 * Start up server and listen for incoming connections from clients
	 * @return	<b>True</b> if success, else <b>False</b>
	 */
	public boolean StartServer() {
		try {
			getServerSettings();
			server 									= new ServerSocket(this.port);
			connectedClients 						= new ArrayList<GameClient>();
			handleGameplane 						= new ServerGameplane(this, serverInfo.getRows(), serverInfo.getColumns());
			Runnable ListenForIncomingConnections 	= new ListenForIncomingConnections(server, this);
			ListenForIncomingConnectionsThread 		= new Thread(ListenForIncomingConnections);
			
			ListenForIncomingConnectionsThread.start();
			gui.addTextToServerMessageTextArea("Server started");
			
			//seperate logging window
			if(serverInfo.getLogginWindow() == 1) {
				this.gui.initLoggingWindow();
			}
			return true;
			
		} catch (IOException e) {
			System.out.println("Failed to initialize serversocket");
			return false;
		}
	}
	
	/**
	 * Stop the server.
	 * @return	<b>True</b> if success, else <b>False</b>
	 */
	public boolean stopServer() {
		try {
			server.close();
			ListenForIncomingConnectionsThread.interrupt();
			
			gui.addTextToServerMessageTextArea("Server stopped");
			handleGameplane = null;
			connectedClients = null;
			//SEND TO ALL CLIENTS THAT SERVER IS SHUTTING DOWN!!!//
			return true;
		}
		catch (SocketException e) {
			System.out.println("Failed to close server");
		}
		catch(Exception e2) {
			//if the server isn't running
		}
		return false;
	}
	
	/**
	 * Gets all server setting for the game
	 * @return	All settings
	 */
	public NessesaryServerInformation 	getServerInfo() 					{ return this.serverInfo; }
	/**
	 * Which port the server is running on
	 * @return	Port
	 */
	public int 							getPort() 							{ return this.port; }
	/**
	 * Gets the gameplane size as a Point, where the Point.x is rows and Point.y columns
	 * @return	Gameplane size
	 */
	public Point 						getGamePlaneSize() 					{ return new Point(serverInfo.getRows(), serverInfo.getColumns()); }
	/**
	 * Gets the list of all connected clients
	 * @return	A list of connected clients
	 */
	public ArrayList<GameClient> 		getConnectedClientList() 			{ return connectedClients; }
	/**
	 * Gets servers gameplane handler
	 * @return	ServerGamePlane handler
	 */
	public ServerGameplane 				getGameplaneHandler() 				{ return handleGameplane; }
	/**
	 * Gets how many players that are in the game
	 * @return	Number of players
	 */
	public int 							getPlayerCount()					{ return handleGameplane.playerCount(); }
	/**
	 * Gets how many players needed to start game
	 * @return	Number of players
	 */
	public int 							getRequiredPlayerCount()			{ return serverInfo.getPlayers(); }
	/**
	 * Gets how many robots that will follow each player
	 * @return	Number of robots
	 */
	public int 							getRobotsPerPlayer()				{ return serverInfo.getRobotsPerPlayer(); }
	/**
	 * Gets how many rubble piles that will be spawned in the game
	 * @return	Number of rubble piles
	 */
	public int							getRubbleCount()					{ return serverInfo.getRubble(); }
	/**
	 * Gets how many attacks a player should have as default
	 * @return	Number of attacks
	 */
	public int							getInitAttacks()					{ return serverInfo.getAttacks(); }
	/**
	 * Get how many safe teleport a player should have as default
	 * @return	Number of safe teleports
	 */
	public int							getInitSafeTeleports()				{ return serverInfo.getSafeTP(); }
	/**
	 * Get how many rounds that will be played on each level before leveling up
	 * @return	Number of round per level
	 */
	public int 							getRoundsPerLevel()					{ return serverInfo.getRoundsPerLevel(); }
	/**
	 * Get how many additional robots that will be added when leveling up
	 * @return	Number of robots
	 */
	public int							getRobotsPerLevel()					{ return serverInfo.getRobotsPerLevel(); }
	/**
	 * Get the current level in game
	 * @return	Current level
	 */
	public int							getLevel()							{ return this.handleGameplane.getLevel(); }
	/**
	 * Gets how many rows there is in the game
	 * @return	Number of rows
	 */
	public int							getRows()							{ return serverInfo.getRows(); }
	/**
	 * Gets how many columns there is in the game
	 * @return	Number of columns
	 */
	public int							getColumns()						{ return serverInfo.getColumns(); }
	/**
	 * Gets a list of all players in the game
	 * @return	List of players in game
	 */
	public CopyOnWriteArrayList<Player> getPlayerList()						{ return handleGameplane.getPlayerList(); }
	/**
	 * Gets if the game is started
	 * @return	<b>True</b> if the game is started, else <b>False</b>
	 */
	public boolean						isGameStarted()						{ return handleGameplane.isGameStarted(); }
	/**
	 * Finds a player by name that are in the game
	 * @param name	Name of player
	 * @return		Player object
	 */
	public Player						findPlayerByName(String name)		{ return handleGameplane.findPlayer(name); }
	/**
	 * Sets the GUI variable
	 * @param gui	ServerGUI variable
	 */
	public void 						setGUIvariable(ServerGUI gui) 								{ this.gui = gui; }
	/**
	 * Tells GUI that a client connected
	 * @param name	Name of client
	 */
	public void 						sendClientConnectedToGUI(String name) 						{ gui.clientConnected(name); }
	/**
	 * Tells GUI that a client disconnected
	 * @param name	Name of client
	 */
	public void 						sendClientDisconnectedToGUI(String name) 					{ gui.clientDisconnected(name); }
	/**
	 * Adds a client to 'connectedClients' list
	 * @param client	GameClient object
	 */
	public void 						addClientToList(GameClient client) 							{ connectedClients.add(client); }
	/**
	 * Add a message to logging window in GUI
	 * @param message	Message to add
	 */
	public void 						addTextToServerMessage(String message) 						{ gui.addTextToServerMessageTextArea(message); }
	/**
	 * Sends a message to client
	 * @param client	Client that should recieve message
	 * @param message	Message to send
	 */
	public void 						sendMessageToClient(Socket client, String message) 			{ HandleCommunication.toClient(client, message); }
	/**
	 * Updates the current server setting with new settings
	 * @param info	Server settings
	 */
	public void 						updateServerInfo(NessesaryServerInformation info) 			{ this.serverInfo = info; setServerSettings(); }
	/**
	 * Add a message to logging window in GUI
	 * @param message	Message to add
	 */
	public void 						addTextToLoggingWindow(String message)						{ this.gui.addTextToLoggingWindow(message); }
	/**
	 * Updates the gameplane
	 */
	public void							updateGameplane()											{ this.gui.updateGamePlane(); }
	/**
	 * Updates highscore list in GUI
	 */
	public void							updateHighscoreList()										{ this.gui.updateHighscoreList(); }
	/**
	 * Sends a message to all clients
	 * @param name		If the message is about a specific client, can be NULL
	 * @param setting	What kind of broadcast it is
	 * @param o1		Optional object, can be NULL
	 * @param o2		Optional object, can be NULL
	 */
	public void 						broadcastToClient(String name, SendSetting setting, 
								  						  Object o1, Object o2) 					{ HandleCommunication.broadcastToClient(name, connectedClients, setting, o1, o2); }
	/**
	 * Moves a player on the gameplane
	 * @param name		Players name
	 * @param oldPos	Old position
	 * @param newPos	New position
	 * @param check		If a check should be performed after moving
	 */
	public void 						movePlayerPosition(String name, Point oldPos, 
								   						   Point newPos, boolean check) 			{ this.handleGameplane.movePlayer(name, oldPos, newPos, check); }
	/**
	 * Saves all settings to file
	 */
	private void 						setServerSettings() 										{ this.serverInfo.saveValuesToFile(); }
	/**
	 * Gets all settings from file
	 */
	private void 						getServerSettings() 										{ this.serverInfo.getValuesFromFile(); }
	
	/**
	 * Send start message to all clients 
	 */
	public void sendStartToClients() {
		for(GameClient gc : connectedClients) 
			sendMessageToClient(gc.getClientSocket(), "@118@" + getLevel() + "@");
	}
	
	/**
	 * Send stop message to all clients
	 */
	public void sendStopToClients() {
		for(GameClient gc : connectedClients)
			sendMessageToClient(gc.getClientSocket(), "@128@");
	}
	
	/**
	 * Remove a client by name from the connectedClients list
	 * @param name Name of the client that should be removed
	 */
	public void removeClientFromList(String name)
	{
		GameClient gc = HandleCommunication.findClient(name, connectedClients);
		connectedClients.remove(gc);
	}
	
	/**
	 * Remove a client by socket from the connectedClients list
	 * @param socket 	Socket that the client is using
	 */
	public String removeClientFromList(Socket socket)
	{
		GameClient gc = HandleCommunication.findClient(socket, connectedClients);
		if(gc != null) {
			gui.clientDisconnected(gc.getName());
			connectedClients.remove(gc);
			return gc.getName();
		}
		return "";
	}
	
	/**
	 * Find a client in the connectedClients list
	 * @param name 	Name of client that should be found
	 * @return		GameClient object
	 */
	public GameClient findClientByName(String name)	{  
		for(GameClient gc : connectedClients) {
			if(gc.getName().equals(name))
				return gc;
		}
		return null;
	}
	
	/**
	 * Returns all the names of the clients that are connected
	 * @return	String array of all names
	 */
	public String[] getNamesFromClientList()
	{
		int size = connectedClients.size();
		String[] names = new String[size];
		for(int i = 0; i < size; i++) {
			names[i] = connectedClients.get(i).getName();
		}
		return names;
	}
	
	/**
	 * Sends a message to a client
	 * @param clientName 	Client that should receive the message
	 * @param message 		Message to send
	 */
	public void sendMessageToClient(String clientName, String message)
	{
		GameClient gc = HandleCommunication.findClient(clientName, connectedClients);
		HandleCommunication.toClient(gc.getClientSocket(), message);
	}	
	
	/**
	 * Clear all data about the game
	 */
	public void clearGame() {
		//copy highscore list before clearing all data
		Map<String, Integer> temp	= handleGameplane.getHighscoreList();
		handleGameplane				= new ServerGameplane(this, serverInfo.getRows(), serverInfo.getColumns());
		
		handleGameplane.setHighscoreList(temp);
		gui.updateGamePlane();
	}
}

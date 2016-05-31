package ClientPackage;

import java.awt.Point;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import HelpPackage.ClientGamePlane;
import HelpPackage.HandleCommunication;
import HelpPackage.Player;
import UserInterfaces.ClientGUI;

public class GameClient {
	private String 				name;
	private int 				port;
	private int 				currentLevel = 1;
	
	private Player 				playerObject;
	private Socket 				thisClientSocket;
	private Socket 				serverSocket;
	private ClientGUI 			gui;
	private ClientGamePlane 	handleGamePlane;
	private BufferedReader 		inFromServer;
	private PrintWriter 		toServer;
	private Thread 				ListenForIncomingMessageThread;
	private ArrayList<String> 	connectedClients;
	private boolean 			gameOn = false; //if the game has started or not
	
	
	/**
	 * GameClient constructor. Sets port to 5000 and initialize ClientGamePlane.
	 */
	public GameClient() {
		this.port = 5000;
		handleGamePlane = new ClientGamePlane(this);
	}
	
	public GameClient(String name, Socket socket) {
		this.name = name;
		this.thisClientSocket = socket;
		this.port = 5000;
	}
	
	/**
	 * Socket that is used to send data to client
	 * @return	Socket to send data to client
	 */
	public Socket getClientSocket() {
		return this.thisClientSocket;
	}
	
	/**
	 * Socket that is used to send data from client to server
	 * @return	Socket to send data to server
	 */
	public Socket getServerSocket() {
		return this.serverSocket;
	}
	
	/**
	 * Clients name
	 * @return	Clients name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Change the clients name
	 * @param name Clients new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Player object that is linked to client
	 * @return	Player object
	 */
	public Player getClientsPlayerObject() {
		return this.playerObject;
	}
	
	/**
	 * Connect client to server. Send message to server that client connected and also tell server the clients name.
	 * @return	<b>True</b> if the connection succeeded, else <b>False</b>
	 */
	public boolean connectToServer(String adress) {
		if(!createConnectionToServer(adress)) 	return false;
		if(!initializeStreams()) 				return false;
		if(!createThreadForCommunication())	 	return false;
		
		if(handleGamePlane == null)
			handleGamePlane = new ClientGamePlane(this);
		
		connectedClients 	= new ArrayList<String>();
		playerObject 		= new Player();
		
		//send the GameClient name to server
		sendMessageToServer("@101@" + this.name + "@");
		return true;
	}
	
	/**
	 * Disconnect client from server. Send disconnect message to server and deletes all game information
	 */
	public void disconnectFromServer() {
		//announce server that client is disconnecting
		sendMessageToServer("@103@" + this.name + "@");
		closeConnectionToServer();
		closeStreams();
		ListenForIncomingMessageThread.interrupt();
		connectedClients 	= null;
		handleGamePlane 	= null;
		gui.showLoginPanel();
	}
	
	/**
	 * Checks if the game is started
	 * @return	Return <b>True</b> if game are started, else <b>False</b>
	 */
	public boolean 			isGameStarted()											{ return gameOn; }
	/**
	 * Gets the ClientGameHandler
	 * @return	ClientGameHandler object
	 */
	public ClientGamePlane 	getGameHandler()										{ return handleGamePlane; }	
	/**
	 * Gets current level in game
	 * @return
	 */
	public int 				getLevel()												{ return this.currentLevel; }
	/**
	 * Send a message to server
	 * @param message	Message that should be sent
	 */
	public void 			sendMessageToServer(String message) 	 				{ HandleCommunication.toServer(toServer, message); }
	/**
	 * Sets GUI variable to GameServer object
	 * @param gui	GameServer GUI
	 */
	public void 			setGUIVariable(ClientGUI gui) 							{ this.gui = gui; }
	/**
	 * Add a client to the "connectedClients" 
	 * @param name	Name of client that should be added
	 */
	public void 			addClientToList(String name) 			 				{ this.connectedClients.add(name); }
	/**
	 * Removes client from "connectedClients"
	 * @param name	Name of client that should be removed
	 */
	public void 			removeClientFromList(String name) 		 				{ this.connectedClients.add(name); }
	/**
	 * Updates gameplane
	 */
	public void				updateGameplane()										{ this.gui.updateGamePlane(); }
	/**
	 * Start game for the client
	 */
	public void				startGame()												{ this.gameOn = true; this.gui.updateBottomPanelInfo(); }
	/**
	 * Stops the game for the client
	 */
	public void				stopGame()												{ this.gameOn = false; }
	/**
	 * Removes all players from the game
	 */
	public void 			removeAllPlayers()										{ this.handleGamePlane.removeAllPlayers(); }
	/**
	 * Sets that it is the agents turn to move again
	 */
	public void 			setAgentsTurnToMove()									{ this.handleGamePlane.setPlayersTurn(); }
	/**
	 * Updates the clients player objects position
	 * @param p	New position
	 */
	public void 			setPlayerPosition(Point p)								{ this.handleGamePlane.setPosition(p); }
	/**
	 * Sets the size that the gameplane should have
	 * @param r	Rows
	 * @param c	Columns
	 */
	public void				setGameplaneSize(int r, int c) 							{ this.handleGamePlane.setGameplaneSize(r, c); }
	/**
	 * Gives clients player a new score
	 * @param score	New score
	 */
	public void				assignPlayerNewScore(int score)							{ this.playerObject.giveNewScore(score); }
	/**
	 * Updates the bottom panel on the gameplane
	 */
	public void				updateGamePlaneInfo()									{ this.gui.updateBottomPanelInfo(); }
	/**
	 * Sets the game level
	 * @param level	New level
	 */
	public void 			setLevel(int level)										{ this.currentLevel = level; }
	
	/**
	 * Find the player object that is linked to the client
	 * @return	Clients player object
	 */
	public Player getPlayerObject() { 
		for(Player p : handleGamePlane.getPlayerList()) {
			if(p.getName().compareTo(this.name) == 0) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Change the label that is showing the connection status between server and client
	 * @param message New label text
	 */
	public void changeConnectionLabel(String message) {
		this.gui.setConnectionLabel(message);
	}
	
	/**
	 * Sets the size of gameplane and initialize the gameWindow
	 * @param rows 		Height
	 * @param columns 	Width
	 */
	public void initGamePlane(int rows, int columns) {
		this.gui.initGamePlane(rows, columns);
	}
	
	/**
	 * Change the text of button that is used to connect and disconnect from server 
	 * @param message New text for button
	 */
	public void changeConnectionButtonLabel(String message) {
		this.gui.setButtonText(message);
	}
	
	/**
	 * When the clients player gets killed the gameWindow gets removed and the loginWindow is shown. 
	 * The client is also disconnected from server.
	 */
	public void	killed() { 
		disconnectFromServer();
		this.gui.switchWindowToLogin(); 
	}
	
	/**
	 * Finds a player by name from the playerlist
	 * @param name	Name of player
	 * @return		Player object
	 */
	public Player findPlayer(String name) {
		for(Player p : handleGamePlane.getPlayerList()) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Kills the connection to server
	 */
	private void closeConnectionToServer()
	{
		try {
			serverSocket.close();
		}
		catch(IOException e) {
			System.out.println("Failed to close connection to server");
			e.printStackTrace();
		}
	}
	
	/**
	 * Close all streams that is used to send and receive data from server
	 */
	private void closeStreams() {
		try {
			inFromServer.close();
			toServer.close();
		}
		catch (IOException e) {
			System.out.println("Failed to close stream");
		}
	}
	
	/**
	 * Establish a connection to server
	 * @return	<b>True</b> if the connection succeeded, else <b>False</b>
	 */
	private boolean createConnectionToServer(String adress) {
		try {
			serverSocket = new Socket(adress, port);
			return true;
		}
		catch (IOException e) {
			gui.setConnectionLabel("Failed to connect to server");
			return false;
		}
	}
	
	/**
	 * Create streams to send and receive data from server
	 * @return	<b>True</b> if succeeded, else <b>False</b>
	 */
	private boolean initializeStreams() {
		try {
			inFromServer 	= new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			toServer 		= new PrintWriter(serverSocket.getOutputStream(), true);
			return true;
		}
		catch (IOException e) {
			System.out.println("Failed to initialize stream");
			return false;
		}
	}
	
	/**
	 * Creates a thread for handling data transfer between server and client
	 * @return	<b>True</b> if succeeded, else <b>False</b>
	 */
	private boolean createThreadForCommunication() {
		//create a thread that listen for incoming message from server
		Runnable ListenForIncomingMessageFromServer = new ListenForIncomingMessageFromServer(this, serverSocket);
		ListenForIncomingMessageThread 				= new Thread(ListenForIncomingMessageFromServer);
		ListenForIncomingMessageThread.start();
		if(ListenForIncomingMessageThread.isAlive()) return true;
		return false;
	}
}

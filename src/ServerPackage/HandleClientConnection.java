package ServerPackage;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import ClientPackage.GameClient;
import HelpPackage.HandleCommunication;
import HelpPackage.Player;
import HelpPackage.Player.PlayerType;
import ServerPackage.GameServer.SendSetting;

public class HandleClientConnection implements Runnable {
	private Socket 			clientSocket;
	private BufferedReader 	inFromClient;
	private PrintWriter 	toClient;
	private GameServer 		server;
	private String 			clientName;
	
	public HandleClientConnection(Socket clientSocket, GameServer server) {
		this.clientSocket 	= clientSocket;
		this.server 		= server;
		createStreams();
	}
	
	public void run() {
		String input = "";
		while(true) {
			try {
				input = inFromClient.readLine();
				if(input == null) {
					//Remove client from connectedClient from GameServer
					String clientName = server.removeClientFromList(clientSocket);
					if(clientName.length() > 0) {
						handleMessage("@103@" + this.clientName + "@");
					}
					break;
				}
				handleMessage(input);
			}
			catch(IOException e2) {
				System.out.println("Error in HandleClientConnection.java: Couldn't read buffer from client");
				break;
			}
		}
	}
	
	private void handleMessage(String input)
	{
		String code[];
		GameClient gameClient;
		code = input.split("@");
		switch(code[1])
		{
		//client connected and send client name
		case "101":
			//too many connections
			if(server.getConnectedClientList().size() >= server.getServerInfo().getPlayers()) {
				HandleCommunication.toClient(clientSocket, "@102@" + "Server is full" + "@");
				server.addTextToLoggingWindow("Client tried to connect. Server full.");
				break;
			}
			//name is already taken
			else if(!checkIfNameIsAvailable(code[2])) {
				HandleCommunication.toClient(clientSocket, "@102@" + "Name is occoupied" + "@");
				server.addTextToLoggingWindow("Client tried to connect. Name already taken.");
				break;
			}
			
			//create a GameClient
			this.clientName = code[2];
			gameClient 		= new GameClient(code[2], clientSocket);
			
			//send message to all client that a new client connected
			HandleCommunication.broadcastToClient(this.clientName, server.getConnectedClientList(), SendSetting.ClientConnected, gameClient, server);
			server.addTextToLoggingWindow("Server created new client (" + this.clientName + ")");
			break;
			
		//client disconnected
		case "103":
			HandleCommunication.broadcastToClient(this.clientName, server.getConnectedClientList(), SendSetting.ClientDisconnected, server, null);
			server.addTextToLoggingWindow("Client disconnected (" + this.clientName + ")");
			server.getGameplaneHandler().removePlayer(this.clientName);
			HandleCommunication.broadcastToClient(this.clientName, server.getConnectedClientList(), SendSetting.RemovePlayer, null, null);
			server.addTextToLoggingWindow("Server removed player (" + this.clientName + ")");
			
			// no agents left, clear all gameinfo
			if(server.getConnectedClientList().size() == 0) {
				server.clearGame();
			}
			break;
			
		//client want size of gamePlane
		case "105":
			Point gamePlaneSize 		= server.getGamePlaneSize();
			Player newPlayer 			= server.getGameplaneHandler().initPlayerToGameplane(this.clientName, 
																							Player.PlayerType.Agent,
																							server.getInitAttacks(),
																							server.getInitSafeTeleports());
			server.addTextToLoggingWindow("Client (" + this.clientName + ") requested size of gameplane");
			SendGameStartInformation(gamePlaneSize, newPlayer.getPosition());
			server.sendClientConnectedToGUI(this.clientName);
			
			//if there is enough players to start the game
			if(server.getPlayerCount() == server.getRequiredPlayerCount()) {
				HandleCommunication.broadcastToClient(null, server.getConnectedClientList(), SendSetting.StartGame, server, null);
				server.addTextToLoggingWindow("Enough players is connected. Server start game");
			}
			break;
		//client moved on gameplane
		case "107":
			Point oldPos = new Point(Integer.parseInt(code[2]), Integer.parseInt(code[3]));
			Point newPos = new Point(Integer.parseInt(code[4]), Integer.parseInt(code[5]));
			server.movePlayerPosition(clientName, oldPos, newPos, true);
			server.broadcastToClient(this.clientName, SendSetting.PlayerMoved, oldPos, newPos);
			server.addTextToLoggingWindow("Client (" + this.clientName + ") moved from (" + oldPos.x + "," + oldPos.y +
														   ") to (" + newPos.x + "," + newPos.y + ")");
			break;
		//player want a safeTeleport
		case "109":
			Player 	p;
			Point 	newPosition;
			Point 	oldPosition;
			
			p 			= server.findPlayerByName(code[2]);
			oldPosition = new Point(p.getX(), p.getY());
			newPosition = server.getGameplaneHandler().performTeleport(p.getName(), p.getPosition(), true);
			
			server.broadcastToClient(p.getName(), SendSetting.PlayerMoved, oldPosition, newPosition);
			server.sendMessageToClient(clientSocket, "@124@" + newPosition.x + "@" + newPosition.y + "@");
			
			server.addTextToLoggingWindow("Client (" + this.clientName + ") safe teleported from (" + oldPosition.x + "," + oldPosition.y +
					   ") to (" + newPosition.x + "," + newPosition.y + ")");
			break;
		//player want to short range attack
		case "111":
			ArrayList<Player> killedPlayers = new ArrayList<Player>();
			killedPlayers 					= server.getGameplaneHandler().performShortAttack(this.clientName);
			for(Player killed : killedPlayers) {
				server.broadcastToClient(killed.getName(), SendSetting.RemovePlayer, null, null);
			}
			server.getGameplaneHandler().performCheckAfterPlayerMove();
			server.addTextToLoggingWindow("Client (" + this.clientName + ") performed short range attack");
			break;
		//player want unsafe teleport
		case "113":
			Player 	p2;
			Point 	newPosition2;
			Point 	oldPosition2;
			
			p2 			= server.findPlayerByName(code[2]);
			oldPosition2 = new Point(p2.getX(), p2.getY());
			newPosition2 = server.getGameplaneHandler().performTeleport(p2.getName(), p2.getPosition(), false);
			
			server.broadcastToClient(p2.getName(), SendSetting.PlayerMoved, oldPosition2, newPosition2);
			server.sendMessageToClient(clientSocket, "@124@" + newPosition2.x + "@" + newPosition2.y + "@");
			
			server.addTextToLoggingWindow("Client (" + this.clientName + ") unsafe teleported from (" + oldPosition2.x + "," + oldPosition2.y +
					   ") to (" + newPosition2.x + "," + newPosition2.y + ")");
			break;
		default:
			server.addTextToServerMessage("Unknown command: " + code[1]);
			break;
		}
	}
	
	private void createStreams()
	{
		try
		{
			inFromClient 	= new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toClient 		= new PrintWriter(this.clientSocket.getOutputStream(), true);
		}
		catch (IOException e)
		{
			System.out.println("Failed to initialize streams");
		}
	}
	
	private void SendMessageToClient(String message) {
		try  {
			toClient.write(message + "\n");
			toClient.flush();
		} 
		catch (Exception e) {
			System.out.println("Failed to send message to client");
		}	
	}
	
	private void SendGameStartInformation(Point gamePlaneSize, Point playerStartPosition) {
		SendMessageToClient("@106@" + 
							gamePlaneSize.x + "@" + 
							gamePlaneSize.y + "@");
		
		SendMessageToClient("@108@" + 
							playerStartPosition.x + "@" + 
							playerStartPosition.y + "@" + 
							server.getInitAttacks() + "@" +
							server.getInitSafeTeleports() + "@");
		
		//broadcast to the other clients that a client connected
		for(GameClient gc2 : server.getConnectedClientList()) {
			//don't send to the client itself
			if(gc2.getClientSocket() != clientSocket)
				server.sendMessageToClient(gc2.getClientSocket(),
										   "@110@" + this.clientName + "@" + 
										   playerStartPosition.x + "@" + 
										   playerStartPosition.y + "@" + 
										   PlayerType.Agent.ordinal() + "@" + 
										   server.getInitAttacks() + "@" +
										   server.getInitSafeTeleports() + "@");
		}

		for(Player p : server.getGameplaneHandler().getPlayerList()) {
			//don't send the clients own startposition
			if(p.getName().compareTo(this.clientName) != 0)
				SendMessageToClient("@110@" + 
									p.getName() + "@" + 
									p.getX() + "@" + 
								    p.getY() + "@" + 
									p.getType().ordinal() + "@" +
								    server.getInitAttacks() + "@" + 
									server.getInitSafeTeleports() + "@");
		}
	}
	
	private boolean checkIfNameIsAvailable(String name) {
		CopyOnWriteArrayList<Player> playersOnline 	= new CopyOnWriteArrayList<Player>();
		playersOnline								= server.getPlayerList();
		
		for(Player p : playersOnline) {
			if(p.getName().equals(name))
				return false;
		}
		
		return true;
	}
}

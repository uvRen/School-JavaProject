package ClientPackage;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JOptionPane;

import HelpPackage.Player;
import HelpPackage.Player.PlayerType;

public class ListenForIncomingMessageFromServer implements Runnable{
	private GameClient 		client;
	private BufferedReader 	inFromServer;
	
	public ListenForIncomingMessageFromServer(GameClient client, Socket serverSocket)
	{
		this.client = client;
		try {
			this.inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		} 
		catch(IOException e) {
			System.out.println("Couldn't open stream to server");
		}
	}
	
	/**
	 * Listen for incoming messsages from server
	 */
	public void run()
	{
		String input = "";
		while(true) {
			try {
				if(client.getServerSocket().isConnected()) {
					input = inFromServer.readLine();
					//if the connection to the server is lost
					if(input == null) {
						System.out.println("Lost connection to server");
						break;
					}
					else
						HandleMessage(input);
				}
				else {
					break;
				}
			}
			catch (IOException e) {

			}
		}
	}
	
	/**
	 * Analys the message received from server, and execute program code depending on message code
	 * @param input	Message received from server
	 */
	private void HandleMessage(String input)
	{
		Point point;
		String code[];
		code = input.split("@");
		switch(code[1]) {
		//force disconnect 
		case "102":
			client.disconnectFromServer();
			client.changeConnectionLabel(code[2]);
			break;
		//server sends a client that should be added to the connectCLient list 
		case "104":
			client.addClientToList(code[2]);
			break;
		//server send size of gameplane
		case "106":
			int rows 	= Integer.parseInt(code[2]);
			int columns = Integer.parseInt(code[3]);
			client.initGamePlane(rows, columns);
			client.setGameplaneSize(rows, columns);
			break;
		//server sends startposition
		case "108":
			//default values for number of attacks and safe teleports
			int initAttacks = Integer.parseInt(code[4]);
			int initSafeTP 	= Integer.parseInt(code[5]);
			point 			= new Point(Integer.parseInt(code[2]), Integer.parseInt(code[3]));
			Player player 	= new Player(client.getName(), point, Player.PlayerType.Agent, initAttacks, initSafeTP);
			
			client.getClientsPlayerObject().setAttacks(initAttacks);
			client.getClientsPlayerObject().setSafeTP(initSafeTP);
			client.getClientsPlayerObject().setPosition(point.x, point.y);
			
			client.getGameHandler().addPlayerToGameplane(player);
			break;
		//adds a position to the gameplane
		case "110":
			Player.PlayerType type 	= null;
			point 					= new Point(Integer.parseInt(code[3]), Integer.parseInt(code[4]));
			
			if(Integer.parseInt(code[5]) == 0)
				type = Player.PlayerType.Agent;
			else if(Integer.parseInt(code[5]) == 1)
				type = Player.PlayerType.Robot;
			else if(Integer.parseInt(code[5]) == 2)
				type = Player.PlayerType.Rubble;
			
			//default values for number of attacks and safe teleports
			initAttacks = Integer.parseInt(code[6]);
			initSafeTP 	= Integer.parseInt(code[7]);
			
			player = new Player(code[2], point, type, initAttacks, initSafeTP);
			
			client.getGameHandler().addPlayerToGameplane(player);
			client.updateGameplane();
			break;
		//server send that a client disconnected
		case "112":
			client.removeClientFromList(code[2]);
			break;
		//player moved
		case "114":
			Point oldPos = new Point(Integer.parseInt(code[3]), Integer.parseInt(code[4]));
			Point newPos = new Point(Integer.parseInt(code[5]), Integer.parseInt(code[6]));
			client.getGameHandler().movePlayer(code[2], oldPos, newPos);
			client.updateGameplane();
			break;
		//connection OK
		case "116":
			client.changeConnectionButtonLabel("Disconnect");
			client.changeConnectionLabel("Connected");
			//request size of gameplane from server
			client.sendMessageToServer("@105@");
			break;
		//start game
		case "118":
			int currentLevel = Integer.parseInt(code[2]);
			client.setLevel(currentLevel);
			if(currentLevel > 1) {
				client.getClientsPlayerObject().addScore(5 * (currentLevel-1));
			}
			client.startGame();
			client.setAgentsTurnToMove();
			break;
		//agents turn to move
		case "120":
			client.setAgentsTurnToMove();
			break;
		case "122":
			client.getGameHandler().removePlayer(code[2]);
			client.updateGameplane();
			break;
		//players new position after teleport
		case "124":
			Point newPosition 	= new Point();
			newPosition.x 		= Integer.parseInt(code[2]);
			newPosition.y 		= Integer.parseInt(code[3]);
			
			client.setPlayerPosition(newPosition);
			break;
		//give player new score
		case "126":
			int newScore = Integer.parseInt(code[2]);
			client.assignPlayerNewScore(newScore);
			client.updateGamePlaneInfo();
			break;
		//stop game
		case "128":
			client.stopGame();
			client.removeAllPlayers();
			break;
		//player changed type
		case "130":
			Player playerToChange = client.findPlayer(code[2]);
			playerToChange.setType(PlayerType.Rubble);
			break;
		//you got killed
		case "132":
			String highscoreList = "Highscore: \n";
			for(int i = 0; i < Integer.parseInt(code[2]) * 2; i += 2) {
				int length = code[3+i].length();
				for(int j = length; j < 20; j++)
					code[3+i] += "  ";
				String row = String.format("%s: %02d\n", code[3+i], Integer.parseInt(code[4+i]));

				highscoreList += row;
			}
			JOptionPane.showMessageDialog(null, highscoreList);
			client.killed();
			break;
		//player got a reward
		case "134":
			switch(code[2]) {
			case "0":	// increase attack
				client.getClientsPlayerObject().increaseAttacks();
				break;
			case "1":	// increase safe TP
				client.getClientsPlayerObject().increaseSafeTP();
				break;
			}
			break;
		}
	}

}

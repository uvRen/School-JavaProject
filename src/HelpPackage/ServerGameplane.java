package HelpPackage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import ClientPackage.GameClient;
import HelpPackage.Player.PlayerType;
import ServerPackage.GameServer;
import ServerPackage.GameServer.SendSetting;

public class ServerGameplane {
	private CopyOnWriteArrayList<Player> 	playerList;
	private Map<String, Integer>			highscore;
	public CopyOnWriteArrayList<Point> 		occupiedPositions;
	private GameServer						server;
	private int 							rows;
	private int 							cols;
	private int 							agents;
	private int 							agentsMoved;
	private int								killRobotScore 	= 10;
	private int 							roundsPlayed 	= 1;
	private int								currentLevel	= 1;
	private boolean 						gameStarted;
	
	public ServerGameplane(GameServer server, int rows, int cols) {
		this.server 		= server;
		this.agents			= 0;
		this.agentsMoved	= 0;
		this.rows 			= rows;
		this.cols 			= cols;
		playerList 			= new CopyOnWriteArrayList<Player>();
		occupiedPositions 	= new CopyOnWriteArrayList<Point>();
		highscore			= new HashMap<String, Integer>();	
	}
	
	/**
	 * Gets the occupied positions on gameplane
	 * @return	An array of occupied positions
	 */
	public CopyOnWriteArrayList<Point> 	getOccupiedPositions() 			 			{ return this.occupiedPositions; }
	/**
	 * Gets the players in game
	 * @return	An array of players
	 */
	public CopyOnWriteArrayList<Player> getPlayerList() 							{ return this.playerList; }
	/**
	 * Gets the highscore list
	 * @return	Higscore list Map<String, Integer>
	 */
	public Map<String, Integer>			getHighscoreList()							{ return this.highscore; }
	/**
	 * Gets if the game is started
	 * @return	<b>True</b> if game is started, else <b>False</b>
	 */
	public boolean						isGameStarted()								{ return this.gameStarted; }
	/**
	 * Gets how many agents that is in the game
	 * @return	Agents count
	 */
	public int							getAgentsCount()							{ return this.agents; }
	/**
	 * Increase the level of the game
	 */
	public void							increaseLevel()								{ this.currentLevel += 1; }
	/**
	 * Sets the highscore list with an already existing one
	 * @param temp	Highscore list
	 */
	public void 						setHighscoreList(Map<String, Integer> temp) { this.highscore = temp; }
	/**
	 * Gets the current level in game
	 * @return	Current level
	 */
	public int 							getLevel()									{ return this.currentLevel; }
	
	/**
	 * Add a new player to the highscore list
	 * @param p Player that should be added
	 */
	public void addNewPlayer(Player p) {
		// if the player don't exists in the highscore list the player is added
		if(p.getType() == PlayerType.Agent) {
			if(highscore.get(p.getName()) == null) {
				highscore.put(p.getName(), 0);
			}
		}
		this.playerList.add(p); 
	}
	
	/**
	 * Find the score for a specific player in the highscore list
	 * @param name 	Name of the player
	 * @return		Score of the player, 0 if the player isn't in the higscore list
	 */
	public int getPlayerScoreFromHighscoreList(String name) {
		//if the name isn't found in the list
		if(highscore.containsKey(name))
			return 0;
		
		return highscore.get(name);
	}
	
	/**
	 * Gives all players a score bonus for surviving a level
	 */
	public void givePlayerLevelBonus() {
		for(Player p : playerList) {
			if(p.getType() == PlayerType.Agent) {
				p.addScore(5 * (currentLevel-1));
			}
		}
		server.updateHighscoreList();
		server.addTextToLoggingWindow("Server gives all players level bonus");
	}
	
	/**
	 * 
	 * @param name	Name of the player that should be found
	 * @return		Return the player object that has that name 
	 */
	public Player findPlayer(String name) {
		for(Player p : playerList) 
			if(p.getName().equals(name))
				return p;
		return null;
	}
	
	/**
	 * Server removes the player from the gameplane. If it is a agent that should be removed
	 * the server reassign the robots that was following this player to follow another player. 
	 * @param name Name of the player that should be removed
	 */
	public void removePlayer(String name) {
		for(Player p : playerList) {
			if(p.getName().equals(name)) {
				occupiedPositions.remove(p.getPosition());
				if(p.getType() == Player.PlayerType.Agent) {
					reAssignRobots(p);
					this.agents -= 1;
				}
				playerList.remove(p);
			}
		}
		server.broadcastToClient(name, SendSetting.RemovePlayer, null, null);
		server.updateGameplane();
	}
	
	/**
	 * Server move the player from a position to another. If all the agents has moved
	 * then the server moves all the robots.
	 * @param name		The name of the player that moved
	 * @param oldPos	The old position that the player moved from
	 * @param newPos	The new position that the player moved to
	 */
	public void movePlayer(String name, Point oldPos, Point newPos, boolean performCheck) {
		removePosition(oldPos);
		occupiedPositions.add(newPos);
		for(Player p : playerList) {
			if(p.getName().equals(name))
				p.setPosition(newPos);
		}
		if(performCheck)
			performCheckAfterPlayerMove();
		else
			server.broadcastToClient(name, SendSetting.PlayerMoved, oldPos, newPos);
		
		server.updateGameplane();
	}
	
	/**
	 * Server put player at a specific position without doing any check
	 * @param player	Player to move
	 * @param moveTo	Position where player should be moved to
	 */
	public void putPlayerAtPosition(Player player, Point moveTo) {
		removePosition(player.getPosition());
		occupiedPositions.add(moveTo);
		for(Player p : playerList) {
			if(p.getName().equals(player.getName()))
				p.setPosition(moveTo);
		}
	}
	
	/**
	 * Adds "agentsMoved" with '1', if all agents has moved the server moves
	 * all robots. And if all rounds are played, server changes level 
	 */
	public void performCheckAfterPlayerMove() {
		agentsMoved += 1;
		
		if(agentsMoved == agents) {
			roundsPlayed += 1;
			moveRobots();
			agentsMoved = 0;
			
			//restart game at new level after the robots has moved
			if(roundsPlayed > server.getRoundsPerLevel()) {
				HandleCommunication.broadcastToClient(null, server.getConnectedClientList(), SendSetting.NextLevel, server, null);
				roundsPlayed = 0;
			}
		}
	}
	
	/**
	 * Removes a position from the occupied positions list
	 * @param p Point that should be removed
	 */
	private void removePosition(Point p) {
		for(Point point : occupiedPositions) {
			if(p.x == point.x && p.y == point.y) {
				occupiedPositions.remove(point);
			}
		}
	}
	
	/**
	 * Removes all robots and rubble, and set agents "followingRobots" to '0'
	 */
	public void removeRobotsAndRubble() {
		for(Player p : playerList) {
			if(p.getType() != PlayerType.Agent) {
				removePosition(p.getPosition());
				removePlayer(p.getName());
			}
			else 
				p.resetRobotsFollowing();
		}
	}
	
	/**
	 * Move all agents to new position when the server change level
	 */
	public void reassignAllAgents() {
		Point safePosition = null;
		for(Player p : playerList) {
			if(p.getType() == PlayerType.Agent) {
				removePosition(p.getPosition());
				safePosition = getSafeTeleportPosition();
				p.setPosition(safePosition);
				occupiedPositions.add(safePosition);
			}
		}
	}
	
	/**
	 * Spawn a number of robots to the gameplane. The number is based on the server settings
	 */
	public void spawnRobots() {
		String robotName	= "";
		Player newPlayer	= null;
		int numberOfRobots 	= 0;
		int robotsPerPlayer = server.getRobotsPerPlayer();
		int numberOfPlayers = server.getPlayerCount();
		int robotsPerLevel	= server.getRobotsPerLevel();
		
		// calculate how many robots that should be spawned
		robotsPerPlayer = robotsPerPlayer *
						  (robotsPerLevel * 
						  currentLevel);
		
		numberOfRobots 	= robotsPerPlayer *
						  numberOfPlayers;
		
		for(int i = 0; i < numberOfRobots; i++) {
			robotName 			= "Robot" + (i+1);
			newPlayer = initPlayerToGameplane(robotName, 
								  			  PlayerType.Robot, 
								  			  0, 0);
			
			HandleCommunication.broadcastToClient(null, 
												  server.getConnectedClientList(), 
												  SendSetting.AddPlayer, 
												  newPlayer, 
												  null);
		}
	}
	
	/**
	 * Spawn a number of rubble to the gameplane. The number is based on the server settings
	 */
	public void spawnRubble() {
		String rubbleName 	= "";
		Player newPlayer 	= null; 
		int numberOfRubble 	= server.getRubbleCount();
		
		for(int i = 0; i < numberOfRubble; i++) {
			rubbleName = "Rubble" + (i+1);
			newPlayer = initPlayerToGameplane(rubbleName, 
											  PlayerType.Rubble, 
											  0, 0);
			
			HandleCommunication.broadcastToClient(null, 
												  server.getConnectedClientList(), 
												  SendSetting.AddPlayer, 
												  newPlayer, 
												  null);
		}
	}
	
	/**
	 * When new level, all agents are moved to new positions
	 */
	public void moveAllAgents() {
		Point moveTo 		= null;
		GameClient client 	= null;
		
		for(Player p : playerList) {
			if(p.getType() == PlayerType.Agent) {
				client = HandleCommunication.findClient(p.getName(), server.getConnectedClientList());
				moveTo = getSafeTeleportPosition();
				movePlayer(p.getName(), p.getPosition(), moveTo, false);
				server.sendMessageToClient(client.getClientSocket(), "@124@" + moveTo.x + "@" + moveTo.y + "@");
			}
		}
	}
	
	/**
	 * Server spawns a new player at a safe position at the gameplane. If the player type is a robot
	 * the server assign it a agent to follow.
	 * @param name		Name of the new player
	 * @param type		What type the player is: <b> Agent, Robot, Rubble </b>
	 * @param attacks	How many attacks the player should have at start
	 * @param safeTP	How many safe teleportations the player should have at start
	 * @return			Return the new player object
	 */
	public Player initPlayerToGameplane(String name, Player.PlayerType type, int attacks, int safeTP) {
		Player 			player;
		Point p 		= new Point();
		
		//find a point that isn't already occupied
		p 				= getSafeTeleportPosition();
		player 			= new Player(name, p, type, attacks, safeTP);
		
		addNewPlayer(player);
		occupiedPositions.add(p);
		
		if(type == Player.PlayerType.Agent)
			agents += 1;
		//assign the robot a player to follow
		else if(type == Player.PlayerType.Robot) 
			player.addPlayerToFollow(assignPlayerToFollow(null));
		
		server.updateGameplane();
		return player;
	}
	/**
	 * Teleport the player to a safe place. A safe place means that a robot isn't 
	 * in the grid next to the new position
	 * @param name 			The name of the player that want to teleport
	 * @param oldPos		Players current position
	 * @param safeTeleport	If the position has to be safe
	 * @return				The new position
	 */
	public Point performTeleport(String name, Point oldPos, boolean safeTeleport) {
		Point newPos 	= new Point();
		if(safeTeleport)
			newPos = getSafeTeleportPosition();
		else
			newPos = getUnsafeTeleportPosition();
		
		movePlayer(name, oldPos, newPos, true);
		return newPos;
	}
	/**
	 * Perform a short range attack, kills and removes the
	 * robots that is in range of the attack.
	 * Returns an array of robots that was killed
	 * @param name 	The name of the agent that want to attack
	 * @return 		A list of robots that was killed by agents attack
	 */
	public ArrayList<Player> performShortAttack(String name) {
		ArrayList<Player> killed 	= new ArrayList<Player>();
		Point check					= new Point();
		Point p						= new Point();
		Player player 				= findPlayer(name);
		
		p.x							= player.getX();
		p.y							= player.getY();
		check.x						= player.getX();
		check.y						= player.getY();
		
		for(int y = -1; y <= 1; y++) {
			check.y += y;
			for(int x = -1; x <= 1; x++) {
				check.x += x;
				for(Player pl : playerList) {
					if(pl.getX() == check.x && pl.getY() == check.y)
						if(pl.getType() == PlayerType.Robot) {
							killed.add(pl);
							playerKilledRobot(player);
							removePlayer(pl.getName()); 		// kill robot
						}
				}
				//restore values
				check.x	= p.x;
			}
			//restore values
			check.y = p.y;
		}
		
		
		
		
		//send the new score to the player
		if(killed.size() > 0) { // if the player killed at least one robot 
			server.sendMessageToClient(name, "@126@" + highscore.get(player.getName()) + "@");
			
			rewardPlayer(player);
		}
		
		return killed;
	}
	
	/**
	 * If the player is lucky, it will be rewarded with one attack or safe teleport
	 * @param player Player that can be rewarded
	 */
	private void rewardPlayer(Player player) {
		Random number = new Random();
		int num = number.nextInt(100);
		
		if(num >= 80 && num < 90) {
			player.increaseAttacks();
			server.addTextToLoggingWindow("Player (" + player.getName() + ") got rewarded one attack");
			server.sendMessageToClient(player.getName(), "@134@" + "0" + "@");
		}
		else if(num >= 90 && num <= 100) {
			player.increaseSafeTP();
			server.addTextToLoggingWindow("Player (" + player.getName() + ") got rewarded one safe teleport");
			server.sendMessageToClient(player.getName(), "@134@" + "1" + "@");
		}
	}
	
	/**
	 * If a player killed a robot. Scores are added to the player and the highscore list. 
	 * @param p The player that killed a robot
	 */
	private void playerKilledRobot(Player p) {
		int currentScore, newScore;
		p.decreaseRobotsFollowing();
		p.addScore(killRobotScore);
		
		currentScore 	= highscore.get(p.getName());
		newScore 		= currentScore + killRobotScore;
		
		highscore.put(p.getName(), newScore);
		server.updateHighscoreList();
		server.addTextToLoggingWindow("Player (" + p.getName() + ") killed a robot");
	}
	
	/**
	 * Returns the current player count on the server
	 * @return	Number of current players
	 */
	public int playerCount() {
		return playerList.size();
	}
	
	/**
	 * Returns a safe position. A safe place means that a robot isn't 
	 * in the grid next to the new position
	 * @return	A safe position
	 */
	private Point getSafeTeleportPosition() {
		Random number 	= new Random();
		Point p			= new Point();
		while(true) {
			int x		= number.nextInt(rows-1);	
			int y		= number.nextInt(cols-1);
			p.x			= x;
			p.y			= y;
			
			//if the point isn't in the array
			if(!occupiedPositions.contains(p)) {
				//check the surrounding of the point, need to be a "safe teleport"
				if(checkSurroundingOfPoint(p)) {
					break;
				}
			}
		}
		return p;
	}
	
	/**
	 * Returns an unsafe position. Which means that it could be a robot next to the position.
	 * @return	Unsafe position
	 */
	private Point getUnsafeTeleportPosition() {
		Random number 	= new Random();
		Point p 		= new Point();
		
		while(true) {
			int x		= number.nextInt(rows-1);	
			int y		= number.nextInt(cols-1);
			p.x			= x;
			p.y			= y;
			
			// position isn't occupied
			if(!occupiedPositions.contains(p))
				break;
		}
		
		return p;
	}
	
	/**
	 * Checks if there is a robot or a player in the surrounding of the position
	 * @param p	The position that wants to be checked
	 * @return	<b>True</b> if the surrounding is safe, else <b>False</b>
	 */
	private boolean checkSurroundingOfPoint(Point p) {
		Point check = new Point();
		check.x		= p.x;
		check.y 	= p.y;
		
		for(int y = -1; y <= 1; y++) {
			check.y += y;
			for(int x = -1; x <= 1; x++) {
				check.x += x;
				if(occupiedPositions.contains(check)) return false;
				//restore values
				check.x	= p.x;
			}
			//restore values
			check.y = p.y;
		}
		return true;
	}
	/**
	 * Moves all the robots towards the player that they are chasing. 
	 * Then send to agents that it is there turn again
	 */
	private void moveRobots() {
		for(Player p : playerList) {
			if(p.getType() == Player.PlayerType.Robot) {
				Point old = new Point(p.getX(), p.getY());
				Point newPoint = new Point(p.getX(), p.getY());
				
				occupiedPositions.remove(old);
				
				int playerX = p.getPlayerToFollow().getX();
				int playerY = p.getPlayerToFollow().getY();
				
				//move towards the agent
				if(p.getX() < playerX)
					newPoint.x += 1;
				else if(p.getX() > playerX)
					newPoint.x -= 1;
				
				if(p.getY() < playerY)
					newPoint.y += 1;
				else if(p.getY() > playerY)
					newPoint.y -= 1;
				
				p.setPosition(newPoint);
				
				//check if the robot has moved on to something
				if(occupiedPositions.contains(newPoint)) { 		// check if the position is occupied
					for(Player p2 : playerList) { 			
						if(!p.getName().equals(p2.getName())) {	// check so it not is the robot itself
							if(newPoint.equals(p2.getPosition())) {
								if(p2.getType() == PlayerType.Robot) { // if it is a robot, both should be rubble
									p2.setType(PlayerType.Rubble);
									p2.getPlayerToFollow().decreaseRobotsFollowing();
									p.setType(PlayerType.Rubble);
									p.getPlayerToFollow().decreaseRobotsFollowing();
									HandleCommunication.broadcastToClient(null, server.getConnectedClientList(), SendSetting.ChangedType, p.getName(), p2.getName());
								}
								if(p2.getType() == PlayerType.Rubble) { // if it is rubble
									p.setType(PlayerType.Rubble);
									p.getPlayerToFollow().decreaseRobotsFollowing();
									HandleCommunication.broadcastToClient(null, server.getConnectedClientList(), SendSetting.ChangedType, p.getName(), p2.getName());
								}
								else if(p2.getType() == PlayerType.Agent) {
									String send = generateSendableHighscoreList();
									server.sendMessageToClient(p2.getName(), "@132@" + highscore.size() + "@" + send);
									server.addTextToLoggingWindow("Robot killed player (" + p2.getName() + ")");
								}
							}
						}
					}
				}
				
				occupiedPositions.add(newPoint);
				server.broadcastToClient(p.getName(), SendSetting.PlayerMoved, old, newPoint);
			}	
		}
		
		//send that it is agents turn again
		server.broadcastToClient(null, SendSetting.AgentsTurnToMove, null, null);
	}
	
	/**
	 * Converts the higscore list from Map<String, String> to a sendable String
	 * @return	A sendable String
	 */
	private String generateSendableHighscoreList() {
		String toReturn = "";
		for(String s : highscore.keySet()) {
			toReturn += s + "@" + highscore.get(s) + "@";
		}
		return toReturn;
	}
	
	/**
	 * Server assign robots to follow a specific agent. It will spread out the robots equally 
	 * to all agents.
	 * @param notThisPlayer		The function will not return this player
	 * @return					Return the player that the robot should be following
	 */
	private Player assignPlayerToFollow(Player notThisPlayer) {
		int min = 100; //big number so that every other number is smaller
		Player playerToFollow = null;
		for(Player p : playerList) {
			if(notThisPlayer == null) {
				if(p.getType() == PlayerType.Agent) {
					if(p.getRobotsFollowing() < min) {
						playerToFollow = p;
						min = p.getRobotsFollowing();
					}
				}
			}
			else if(!p.getName().equals(notThisPlayer.getName())) {
				if(p.getType() == PlayerType.Agent) {
					if(p.getRobotsFollowing() < min) {
						playerToFollow = p;
						min = p.getRobotsFollowing();
					}
				}
			}
		}
		if(playerToFollow != null)
			playerToFollow.increaseRobotsFollowing();
		return playerToFollow;
	}
	
	/**
	 * If a agent disconnect from the server, all robots that was following that player should
	 * be assigned to follow another agent.
	 * @param p	All robots that was following this player should be reassign
	 */
	private void reAssignRobots(Player p) {
		for(Player player : playerList) 
			if(player.getType() == PlayerType.Robot) 
				if(player.getPlayerToFollow().getName().equals(p.getName())) 
					player.addPlayerToFollow(assignPlayerToFollow(p));
	}
}

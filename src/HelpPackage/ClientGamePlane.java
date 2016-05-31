package HelpPackage;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import ClientPackage.GameClient;
import HelpPackage.Player.PlayerType;

public class ClientGamePlane {
	
	/**
	 * Timer class that controlls if the client is moving the player before the deadline.
	 * If not, the player will perform a wait action.
	 * @author Simon
	 */
	class timeToMove extends TimerTask {
		Point startPosition = new Point();
		public timeToMove(Point p) {
			startPosition.x = p.x;
			startPosition.y = p.y;
		}
		public void run() {
			if(playersTurn && (startPosition.equals(client.getClientsPlayerObject().getPosition()))) { // if player still hasn't moved
				String toSend = "";
				toSend = movePlayer(10, client.getPlayerObject());
				client.sendMessageToServer(toSend);
			}
		}
	}
	
	private CopyOnWriteArrayList<Player> 	playerList;
	private CopyOnWriteArrayList<Point> 	occupiedPositions;
	private boolean							playersTurn = false;
	private Point							basePosition;
	private GameClient						client;
	private int								rows;
	private int								columns;
	private Timer							timer;
	
	/**
	 * ClientGamePlane constructor. Initialize all arrays that is used.
	 * @param Which client the ClientGamePlane should controll.
	 */
	public ClientGamePlane(GameClient client) {
		playerList 			= new CopyOnWriteArrayList<Player>();
		occupiedPositions 	= new CopyOnWriteArrayList<Point>();
		basePosition 		= new Point();
		this.client 		= client;
	}
	
	/**
	 * Returns an array of occupied positions
	 * @return	Occupied positions
	 */
	public CopyOnWriteArrayList<Point> 	getOccupiedPositions() 			{ return occupiedPositions; }
	/**
	 * Returns an array of players
	 * @return
	 */
	public CopyOnWriteArrayList<Player> getPlayerList() 				{ return playerList; }
	/**
	 * Sets the size of the gameplane
	 * @param r	Rows
	 * @param c	Columns
	 */
	public void							setGameplaneSize(int r, int c)	{ this.rows = r; this.columns = c; }
	
	/**
	 * Tells the agent that it is his turn to move, also starts a timer for 10 seconds.
	 */
	public void setPlayersTurn() { 
		timer = new Timer();
		timer.schedule(new timeToMove(client.getPlayerObject().getPosition()), 10000);
		this.playersTurn = true; 
	}
	
	/**
	 * Change the players position varaible
	 * @param New position
	 */
	public void setPosition(Point p) {
		client.getClientsPlayerObject().setPosition(p);
	}
	
	/**
	 * Add a player to the playerlist and mark the players position as occupied
	 * @param p	Player that should be added
	 */
	public void addPlayerToGameplane(Player p) {
		playerList.add(p);
		occupiedPositions.add(p.getPosition());
	}
	
	/**
	 * Removes a player from the playerlist
	 * @param name	The name of player that should be removed 
	 */
	public void removePlayer(String name) {
		for(Player p : playerList) {
			if(p.getName().equals(name)) {
				removePosition(p.getPosition());
				playerList.remove(p);
			}
		}
	}
	
	/**
	 * Remove an occupied position from gameplan
	 * @param p	Position that should be removed
	 */
	private void removePosition(Point p) {
		for(Point point : occupiedPositions) {
			if(p.x == point.x && p.y == point.y) {
				occupiedPositions.remove(point);
			}
		}
	}
	
	/**
	 * Server calls this method to tell the client that the server moved a player. 
	 * The client then update the position of the player 
	 * @param name			Name of the player
	 * @param oldPosition	Old position of player
	 * @param newPosition	New position of player
	 */
	public void movePlayer(String name, Point oldPosition, Point newPosition) {
		for(Player p : playerList) {
			if(p.getName().compareTo(name) == 0) {
				p.setPosition(newPosition);
				occupiedPositions.add(newPosition);
				removePosition(oldPosition);
			}
		}
	}
	/**
	 * Deletes all players (Agent, Robots, Rubble) and all information about them.
	 */
	public void removeAllPlayers() {
		for(Player p : playerList) {
			if(p.getType() == PlayerType.Agent) {
				p.resetRobotsFollowing();
			}
			if(p.getType() != PlayerType.Agent) {
				removePosition(p.getPosition());
				playerList.remove(p);
			}
		}
	}
	
	/**
	 * Handles the action for the player. Movement, teleportation and attacks etc.
	 * @param 	Which key that was pressed
	 * @param 	Which player that should perform the action
	 * @return	The message that should be sent to server.
	 */
	public String movePlayer(int key, Player player) {
		boolean allowedMove = false;
		//makes it possible to only move 1 step from the original position
		basePosition 		= new Point(client.getClientsPlayerObject().getX(), client.getClientsPlayerObject().getY());
		String toReturn 	= "";
		if(playersTurn) {
			Point oldPoint = new Point(player.getX(), player.getY());
			Point newPoint = new Point(player.getX(), player.getY());
			removePosition(oldPoint);	// remove the old position
			
			switch(key) {
			case 87: // w
			case 38: // up
				if(newPoint.y >= basePosition.y && newPoint.y > 0)
					newPoint.y -= 1;
				break;
			case 83: // s
			case 40: //down
				if(newPoint.y <= basePosition.y && newPoint.y < rows - 1)
					newPoint.y += 1;
				break;
			case 65: // a
			case 37: // left
				if(newPoint.x >= basePosition.x && newPoint.x > 0)
					newPoint.x -= 1;
				break;
			case 68: // d
			case 39: // right
				if(newPoint.x <= basePosition.x && newPoint.x < columns - 1)
					newPoint.x += 1;
				break;
			case 10: // enter
				timer.cancel();
				
				playersTurn = false;
				toReturn 	= "@107@" + basePosition.x + "@" + basePosition.y + "@" + newPoint.x + "@" + newPoint.y + "@";
				client.getClientsPlayerObject().setPosition(newPoint);
				break;
			case 84: // safe teleport
				int numberOfSafeTP 	= client.getClientsPlayerObject().getSafeTP();
				if(numberOfSafeTP > 0) {
					playersTurn = false;
					toReturn 	= "@109@" + client.getName() + "@";
					client.getClientsPlayerObject().setSafeTP(numberOfSafeTP - 1);
					client.updateGamePlaneInfo();
				}
				break;
			case 85: // unsafe teleport	
				playersTurn = false;
				toReturn 	= "@113@" + client.getName() + "@";
				break;
			case 32: // attack
				int numberOfAttacks = client.getClientsPlayerObject().getAttacks();
				if(numberOfAttacks > 0) {
					playersTurn = false;
					toReturn = "@111@" + client.getName() + "@";
					client.getClientsPlayerObject().setAttacks(numberOfAttacks - 1);
					client.updateGamePlaneInfo();
				}
				
				break;
			}
			
			// if the position the player want to move to is empty
			if(!occupiedPositions.contains(newPoint))
				allowedMove = true;
			
			// if the move the player want to perform is allowed
			if(allowedMove) {
				player.setPosition(newPoint); 		//update player position
				occupiedPositions.add(newPoint); 	//add the new position
				//update the position
				for(Player pl : playerList) {
					if(pl.getName().equals(player.getName())) {
						pl.setPosition(newPoint);
					}
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * Teleport the player from one position to another
	 * @param <b>True</b> if i should be a safe teleport
	 */
	public void performTeleport(boolean safeTeleport) {
		if(safeTeleport) {
			movePlayer(84, client.getPlayerObject());
		}
		else {
			movePlayer(85, client.getPlayerObject());
		}
	}
}

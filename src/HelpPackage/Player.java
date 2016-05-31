package HelpPackage;

import java.awt.Point;


public class Player {
	public enum PlayerType { Agent, 
							 Robot, 
							 Rubble };
	private String 		name;
	private Point 		position;
	private int 		robotsFollowing;
	private PlayerType 	type;
	private int 		score;
	private int 		safeTP;
	private int 		attacks;
	
	//robots only
	private Player playerToFollow;
	
	/**
	 * Player constructor.
	 */
	public Player() {
		position = new Point();
	}
	
	/**
	 * Player constructor.
	 * @param Players name
	 * @param Position of player
	 */
	public Player(String name, Point position) {
		this.name 				= name;
		this.position 			= position;
		this.robotsFollowing 	= 0;
	}
	
	/**
	 * Player constructor.
	 * @param Name of player
	 * @param Position of player
	 * @param Which type the player is (Agent, Robot, Rubble)
	 * @param How many default attacks the player should have
	 * @param How many default safe teleports the player should have
	 */
	public Player(String name, Point position, PlayerType type, int attacks, int safeTP) {
		this.name 				= name;
		this.position 			= position;
		this.type 				= type;
		this.robotsFollowing 	= 0;
		this.attacks			= attacks;
		this.safeTP				= safeTP;
		this.score				= 0;
	}
	
	/**
	 * Gets the players name
	 * @return	Players name
	 */
	public String 				getName() 					{ return this.name; }
	/**
	 * Gets the current position of player
	 * @return 	Players current posiiton
	 */
	public Point 				getPosition() 				{ return this.position; }
	/**
	 * Gets the PlayerType the player is (Agent, Robot, Rubble)
	 * @return	PlayerType
	 */
	public PlayerType 			getType()					{ return this.type; }
	/**
	 * Returns the player that the robot is following
	 * @return	Player that robot is following
	 */
	public Player 				getPlayerToFollow()			{ return this.playerToFollow; }
	/**
	 * Gets x-coordinate of player
	 * @return	x-coordinate of player
	 */
	public int 					getX()						{ return this.position.x; }
	/**
	 * Gets y-coordinate of player
	 * @return	y-coordinate of player
	 */
	public int 					getY()						{ return this.position.y; }
	/**
	 * Gets all the robots that is following the player
	 * @return	An array of robots 
	 */
	public int					getRobotsFollowing()		{ return this.robotsFollowing; }
	/**
	 * Gets players score
	 * @return	Players score
	 */
	public int					getScore()					{ return this.score; }
	/**
	 * Gets how many attacks the player has left
	 * @return	Number of attacks left
	 */
	public int					getAttacks()				{ return this.attacks; }
	/**
	 * Gets how many safe teleports the player has left
	 * @return	Number of safe teleports left
	 */
	public int					getSafeTP()					{ return this.safeTP; }
	/**
	 * Sets how many attacks the player should have
	 * @param attacks	Number of attacks the player should have
	 */
	public void 				setAttacks(int attacks)		{ this.attacks = attacks; }
	/**
	 * Sets how many safe teleports the player should have
	 * @param safeTP	Number of safe teleports the player should have
	 */
	public void					setSafeTP(int safeTP)		{ this.safeTP = safeTP; }
	/**
	 * Increase players attacks left by one
	 */
	public void					increaseAttacks()			{ this.attacks += 1; }
	/**
	 * Increase players safe teleports by one
	 */
	public void 				increaseSafeTP()			{ this.safeTP += 1; }
	
	
	/**
	 * Set the number of robots that is following the player to 0
	 */
	public void					resetRobotsFollowing()		{ this.robotsFollowing = 0; }
	/**
	 * Give a robot a player to follow
	 * @param p	Player that the robot should follow
	 */
	public void					addPlayerToFollow(Player p) { this.playerToFollow = p; }
	/**
	 * Set name to player
	 * @param name	Players name
	 */
	public void 				setName(String name) 		{ this.name = name; }
	/**
	 * Increase the count of how many robots is following the player
	 */
	public void 				increaseRobotsFollowing()	{ this.robotsFollowing += 1; }
	/**
	 * Decrease the count of how many robots is following the player
	 */
	public void 				decreaseRobotsFollowing()	{ this.robotsFollowing -= 1; }
	/**
	 * Add score to the current score
	 * @param score	Score that should be added
	 */
	public void 				addScore(int score)			{ this.score += score; }
	/**
	 * Gives the player a new score
	 * @param score	New score that player should have
	 */
	public void					giveNewScore(int score)		{ this.score = score; }
	/**
	 * Sets the PlayerType
	 * @param type	PlayerType
	 */
	public void					setType(PlayerType type)	{ this.type = type; }
	
	/**
	 * Sets the position variable for the player
	 * @param X-coordinate
	 * @param Y-coordinate
	 */
	public void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
	}
	
	/**
	 * Sets the position variable of the player
	 * @param Position
	 */
	public void setPosition(Point p) {
		this.position.x = p.x;
		this.position.y = p.y;
	}
}

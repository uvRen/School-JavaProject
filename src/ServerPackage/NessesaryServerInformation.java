package ServerPackage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NessesaryServerInformation implements Serializable {

	private static final long serialVersionUID = 1L;
	private int rows;
	private int columns;
	private int seperateLogginWindow;
	private int players;
	private int robotsPerPlayer;
	private int rubble;
	private int robotsPerLevel;
	private int robotChase;
	private int safeTP;
	private int attacks;
	private int roundsPerLevel;
	
	public NessesaryServerInformation() {
		
	}
	
	public NessesaryServerInformation(int rows, 
									  int columns, 
									  int seperateLogginWindow, 
									  int players, 
									  int robotsPerPlayer, 
									  int rubble, 
									  int robotsPerLevel,
									  int robotChase, 
									  int safeTP, 
									  int attacks, 
									  int roundsPerLevel) {
		this.rows 					= rows;
		this.columns 				= columns;
		this.seperateLogginWindow 	= seperateLogginWindow;
		this.players 				= players;
		this.robotsPerPlayer		= robotsPerPlayer;
		this.rubble					= rubble;
		this.robotsPerLevel			= robotsPerLevel;
		this.robotChase				= robotChase;
		this.safeTP					= safeTP;
		this.attacks				= attacks;
		this.roundsPerLevel			= roundsPerLevel;
	}
	
	public int getRows() 			{ return this.rows; }
	public int getColumns()			{ return this.columns; }
	public int getLogginWindow() 	{ return this.seperateLogginWindow; }
	public int getPlayers() 		{ return this.players; }
	public int getRobotsPerPlayer() { return this.robotsPerPlayer; }
	public int getRubble() 			{ return this.rubble; }
	public int getRobotsPerLevel() 	{ return this.robotsPerLevel; }
	public int getRobotChase() 		{ return this.robotChase; }
	public int getSafeTP() 			{ return this.safeTP; }
	public int getAttacks() 		{ return this.attacks; }
	public int getRoundsPerLevel() 	{ return this.roundsPerLevel; }
	
	/**
	 * Save all settings to file
	 */
	public void saveValuesToFile(){
		try {
			FileOutputStream 	fout 	= new FileOutputStream("serverSettings.data");
			ObjectOutputStream	oos 	= new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();
			fout.close();
		}
		catch(IOException e) {
			
		}
	}
	
	/**
	 * Gets all settings from file
	 */
	public void getValuesFromFile() {
		try {
			FileInputStream 			fin = new FileInputStream("serverSettings.data");
			ObjectInputStream			ois = new ObjectInputStream(fin);
			assignThisObjectValues(ois.readObject());
			fin.close();
			ois.close();
			
		} catch(ClassNotFoundException e) {
			
		} catch(IOException e) {
			
		}
	}
	
	private void assignThisObjectValues(Object object) {
		NessesaryServerInformation o = (NessesaryServerInformation)object;
		this.rows 					= o.getRows();
		this.columns 				= o.getColumns();
		this.seperateLogginWindow 	= o.getLogginWindow();
		this.players 				= o.getPlayers();
		this.robotsPerPlayer		= o.getRobotsPerPlayer();
		this.rubble					= o.getRubble();
		this.robotsPerLevel			= o.getRobotsPerLevel();
		this.robotChase				= o.getRobotChase();
		this.safeTP					= o.getSafeTP();
		this.attacks				= o.getAttacks();
		this.roundsPerLevel			= o.getRoundsPerLevel();
	}
	
}

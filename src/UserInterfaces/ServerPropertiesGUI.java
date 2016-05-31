package UserInterfaces;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import ServerPackage.GameServer;
import ServerPackage.NessesaryServerInformation;

public class ServerPropertiesGUI {

	private JFrame 				frame;
	private JLabel 				lblRows;
	private JSpinner 			spinner, spinner_1, spinner_2, spinner_3, spinner_4, spinner_5, spinner_6, spinner_7, spinner_8;
	private JLabel 				lblColumns;
	private JLabel 				lblNumberOfPlayers;
	private JCheckBox 			chckbxShowSeperateLogging;
	private JButton 			btnApply;
	private JButton 			btnCancel;
	private SpinnerNumberModel 	rows, cols, players, robots, rubble, robotsPerLevel, safeTP, attacks, roundsPerLevel;
	private boolean 			loggingCheckbox 	= false;
	private boolean 			robotChaseCheckbox 	= false;
	
	private GameServer 			server;
	private JLabel 				lblRobots;
	private JLabel 				lblRubble;
	private JLabel 				lblRobotsPerLevel;
	private JCheckBox 			chckbxRobotChaseNearest;
	private JLabel 				lblSafeTp;
	private JLabel 				lblAttacks;
	private JLabel 				lblRoundsPerLevel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerPropertiesGUI window = new ServerPropertiesGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerPropertiesGUI() {
		//initialize();
	}
	
	/**
	 * Gets GameServer object
	 * @param server	GameServer object
	 */
	public void recieveGameServer(GameServer server)
	{
		this.server = server;
		setValuesToComponents();
		initialize();
	}
	
	/**
	 * Sets the values of components in GUI to correct values
	 */
	private void setValuesToComponents()
	{
		NessesaryServerInformation info = server.getServerInfo();
		if(info.getLogginWindow() == 1) { loggingCheckbox = true; }
		if(info.getRobotChase() == 1) { robotChaseCheckbox = true; }
		
		rows 			= new SpinnerNumberModel(info.getRows(), 0, 100, 1);
		cols 			= new SpinnerNumberModel(info.getColumns(), 0, 100, 1);
		players 		= new SpinnerNumberModel(info.getPlayers(), 0, 20, 1);
		robots 			= new SpinnerNumberModel(info.getRobotsPerPlayer(), 0, 30, 1);
		rubble 			= new SpinnerNumberModel(info.getRubble(), 0, 20, 1);
		robotsPerLevel 	= new SpinnerNumberModel(info.getRobotsPerLevel(), 0, 10, 1);
		safeTP 			= new SpinnerNumberModel(info.getSafeTP(), 0, 10, 1);
		attacks 		= new SpinnerNumberModel(info.getAttacks(), 0, 10, 1);
		roundsPerLevel 	= new SpinnerNumberModel(info.getRoundsPerLevel(), 0, 30, 1);
		
	}
	
	/**
	 * Save chosen settings to file
	 */
	private void saveValues()
	{
		NessesaryServerInformation info;
		int checkBoxValueLogging, checkBoxValueRobotChase;
		if(chckbxShowSeperateLogging.isSelected())	 { checkBoxValueLogging 	= 1; } 	else { checkBoxValueLogging		 = 0; }
		if(chckbxRobotChaseNearest.isSelected())	 { checkBoxValueRobotChase	= 1; }	else { checkBoxValueRobotChase	 = 0; }
		
		info = new NessesaryServerInformation(
				Integer.parseInt(rows.getValue().toString()), 				//rows
				Integer.parseInt(cols.getValue().toString()), 				//columns
				checkBoxValueLogging, 										//seprate logging window
				Integer.parseInt(players.getValue().toString()), 			//required players
				Integer.parseInt(robots.getValue().toString()),				//robots per player
				Integer.parseInt(rubble.getValue().toString()), 			//rubble piles
				Integer.parseInt(robotsPerLevel.getValue().toString()), 	//addtional robots per level
				checkBoxValueRobotChase,									//robot chase nearest player
				Integer.parseInt(safeTP.getValue().toString()), 			//default safe teleports
				Integer.parseInt(attacks.getValue().toString()), 			//default attacks
				Integer.parseInt(roundsPerLevel.getValue().toString())		//rounds per level
				);
		
		server.updateServerInfo(info);
		info.saveValuesToFile();
	}
	
	private ServerPropertiesGUI GetGUI()
	{
		return this;
	}
	
	/**
	 * Initialize GUI
	 */
	private void initialize() {
		frame 						= new JFrame();
		lblRows 					= new JLabel("Rows");
		spinner 					= new JSpinner(rows);
		lblColumns 					= new JLabel("Columns");
		spinner_1 					= new JSpinner(cols);
		lblNumberOfPlayers 			= new JLabel("Players");
		spinner_2 					= new JSpinner(players);
		chckbxShowSeperateLogging 	= new JCheckBox("Show seperate logging window");
		btnApply 					= new JButton("Apply");
		btnCancel 					= new JButton("Cancel");
		spinner_3 					= new JSpinner(robots);
		spinner_4 					= new JSpinner(rubble);
		spinner_5 					= new JSpinner(robotsPerLevel);
		lblRobots 					= new JLabel("Robots");
		lblRubble 					= new JLabel("Rubble");
		lblRobotsPerLevel 			= new JLabel("Robots per level");
		chckbxRobotChaseNearest 	= new JCheckBox("Robot chase nearest player");
		lblSafeTp 					= new JLabel("Safe TP");
		lblAttacks 					= new JLabel("Attacks");
		lblRoundsPerLevel 			= new JLabel("Rounds per level");
		spinner_6 					= new JSpinner(safeTP);
		spinner_7 					= new JSpinner(attacks);
		spinner_8 					= new JSpinner(roundsPerLevel);
		
		frame.setBounds(100, 100, 300, 300);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		chckbxRobotChaseNearest.setSelected(robotChaseCheckbox);
		chckbxShowSeperateLogging.setSelected(loggingCheckbox);
		
		btnCancel.addActionListener(new ButtonListener());
		btnApply.addActionListener(new ButtonListener());
		

		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblRows)
								.addComponent(spinner, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE))
							.addGap(2)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblColumns)
								.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)))
						.addComponent(chckbxShowSeperateLogging)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblNumberOfPlayers)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblRobots, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblRubble, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblRobotsPerLevel))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spinner_3, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spinner_4, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spinner_5, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnApply)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCancel))
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
							.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(spinner_6, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblSafeTp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(spinner_7, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblAttacks, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addGroup(groupLayout.createSequentialGroup()
										.addComponent(spinner_8, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED))
									.addComponent(lblRoundsPerLevel, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)))
							.addComponent(chckbxRobotChaseNearest, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE)))
					.addGap(25))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRows)
						.addComponent(lblColumns))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxShowSeperateLogging)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfPlayers)
						.addComponent(lblRobots)
						.addComponent(lblRubble)
						.addComponent(lblRobotsPerLevel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxRobotChaseNearest)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSafeTp)
						.addComponent(lblAttacks)
						.addComponent(lblRoundsPerLevel))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(spinner_6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(spinner_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnApply)
						.addComponent(btnCancel))
					.addContainerGap())
		);
		frame.getContentPane().setLayout(groupLayout);
		frame.setVisible(true);
	}
	
	class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			switch(e.getActionCommand())
			{
			case "Apply":
				saveValues();
				GetGUI().frame.setVisible(false);
				break;
			case "Cancel":
				GetGUI().frame.setVisible(false);
				break;
			}
		}
		
	}
}

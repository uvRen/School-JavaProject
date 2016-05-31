package UserInterfaces;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import ClientPackage.GameClient;
import HelpPackage.ClientGamePlane;
import HelpPackage.Player;
import HelpPackage.Player.PlayerType;
import ServerPackage.GameServer;

public class ClientGUI {

	private JFrame 				frame, gameWindow;
	private JPanel 				loginPanel, gamePanel;
	private GamePlaneBottom 	gamePlaneBottom;
	private JMenuBar 			menuBar;
	private JMenu 				menuArkiv, menuEdit;
	private JMenuItem 			menuItemClose, menuItemProperties;
	private JLabel 				connectionStatus;
	private JTextField 			typeClientName, typeIPadress;
	private JButton 			connectButton;
	private GridBagConstraints 	gridCon;
	private ClientGamePlane 	handleGame;
	private GamePlaneGUI 		gamePlaneGUI;
	private GameClient 			client;
	private int 				rows, columns;
	
	private final int WINDOW_HEIGHT = 650, WINDOW_WIDTH = 800;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
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
	public ClientGUI() {
		client 			= new GameClient();
		gamePlaneGUI 	= new GamePlaneGUI(client);
		handleGame 		= client.getGameHandler();
		client.setGUIVariable(this);
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//initialize components
		frame 				= new JFrame();
		loginPanel 			= new JPanel(new GridBagLayout());
		gamePanel 			= new JPanel(new BorderLayout());
		gridCon 			= new GridBagConstraints();
		menuBar 			= new JMenuBar();
		menuArkiv 			= new JMenu("Arkiv");
		menuEdit 			= new JMenu("Edit");
		menuItemClose 		= new JMenuItem("Close");
		menuItemProperties 	= new JMenuItem("Properties");
		connectionStatus 	= new JLabel("Not connected");
		typeClientName 		= new JTextField(20);
		typeIPadress 		= new JTextField(20);
		connectButton 		= new JButton("Connect");
		
		//set properties to components
		frame.setBounds(300, 300, WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.setVisible(true);
		frame.setTitle("Robots Connect");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		connectionStatus.setVisible(true);
		gamePanel.setVisible(false);
		
		//add listener
		menuItemClose.addActionListener		(new MenuActionListener());
		menuItemProperties.addActionListener(new MenuActionListener());
		connectButton.addActionListener		(new ButtonListener());
		
		//add the components to the menu
		menuArkiv.add(menuItemClose);
		menuEdit.add(menuItemProperties);
		menuBar.add(menuArkiv);
		menuBar.add(menuEdit);
		
		//add components to loginPanel 		
		initLoginPanel();
		
		frame.setJMenuBar(menuBar);
		frame.setVisible(true);
	}
	
	public void setConnectionLabel(String message) 	{ connectionStatus.setText(message); }
	public void setButtonText(String message) 		{ connectButton.setText(message); }
	public void updateGamePlane()					{ gamePlaneGUI.updateGamePanel(); }
	public void updateBottomPanelInfo()				{ gamePlaneBottom.updateValues(); }
	
	public void showLoginPanel() {
		gamePanel.setVisible(false);
		gameWindow.setVisible(false);
		frame.getContentPane().remove(gamePanel);
		loginPanel.setVisible(true);
		frame.getContentPane().add(loginPanel);
		frame.setVisible(true);
		connectButton.setText("Connect");
		connectionStatus.setText("Not connected");
	}
		
	public void initGamePlane(int row, int col) {
		this.rows 		= row;
		this.columns 	= col;
		showGamePanel();
	}
	
	private void showGamePanel() {
		gameWindow 		= new JFrame("Robots");
		gamePlaneBottom = new GamePlaneBottom(client); // contains the current game info 
		
		gameWindow.setLayout(new BorderLayout());
		gameWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		gamePlaneGUI.setHeightAndWidth(rows, columns); 	// set the size of the gameplane
		
		gameWindow.addWindowListener(new FrameListener());
		gameWindow.addKeyListener	(new Key());
		
		gameWindow.add(gamePlaneGUI);
		gameWindow.add(gamePlaneBottom, BorderLayout.SOUTH);
		
		gameWindow.pack();
		gameWindow.setLocationRelativeTo(null);
		gameWindow.setVisible(true);
		
		frame.setVisible(false);						// hides the login window
	}
	
	/**
	 * Hides the gameWindow and shows the loginWindow
	 */
	public void switchWindowToLogin() {
		gameWindow.setVisible(false);
		frame.setVisible(true);
	}
	
	private void initLoginPanel()
	{
		//IP adress
		gridCon.gridx = 4;
		gridCon.gridy = 1;
		gridCon.fill = GridBagConstraints.HORIZONTAL;
		loginPanel.add(new JLabel("IP adress"), gridCon);
		gridCon.gridx = 4;
		gridCon.gridy = 2;
		loginPanel.add(typeIPadress, gridCon);
		
		//Username
		gridCon.gridx = 4;
		gridCon.gridy = 3;
		gridCon.fill = GridBagConstraints.HORIZONTAL;
		loginPanel.add(new JLabel("Username"), gridCon);
		gridCon.gridx = 4;
		gridCon.gridy = 4;
		loginPanel.add(typeClientName, gridCon);
		
		//Connect button
		gridCon.gridx = 4;
		gridCon.gridy = 5;
		gridCon.fill = GridBagConstraints.HORIZONTAL;
		loginPanel.add(connectButton, gridCon);
		
		//Connection status label
		gridCon.gridx = 4;
		gridCon.gridy = 10;
		loginPanel.add(connectionStatus, gridCon);
		frame.getContentPane().add(loginPanel);
	}
	
	class MenuActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "Close":
				client.disconnectFromServer();
				System.exit(0);
				break;
			}
		}
	}
	
	class ButtonListener implements ActionListener  {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
			case "Connect":
				if(typeClientName.getText().toString().length() > 0) {
					client.setName(typeClientName.getText().toString());
					client.connectToServer(typeIPadress.getText().toString());
				}
				break;
			case "Disconnect":
				client.disconnectFromServer();
				connectButton.setText("Connect");
				connectionStatus.setText("Not connected");
				showLoginPanel();
				break;
			}
		}	
	}

	/*
	 w 		= 87
	 a 		= 65
	 s 		= 83
	 d 		= 68
	 Enter 	= 10
	 up		= 38
	 down	= 40
	 left	= 37
	 right	= 39
	 */
	class Key implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			Player player = client.getPlayerObject();
			String sendToServer = "";
			//only possible to move if the game is on
			if(client.isGameStarted()) {
				switch(e.getKeyCode()) {
				//move player
				case 87:
				case 65:
				case 83:
				case 68:
				case 38:
				case 40:
				case 37:
				case 39:
				case 10:
					sendToServer = handleGame.movePlayer(e.getKeyCode(), player);
					if(sendToServer != "") 
						client.sendMessageToServer(sendToServer);
					updateGamePlane();
					break;
				//teleport
				case 84:
				case 85:
					sendToServer = handleGame.movePlayer(e.getKeyCode(), player);
					if(sendToServer != "") 
						client.sendMessageToServer(sendToServer);
					break;
				//attack
				case 32:
					sendToServer = handleGame.movePlayer(e.getKeyCode(), player);
					if(sendToServer != "")
						client.sendMessageToServer(sendToServer);
					break;
				default:
					System.out.println("keyCode(): " + e.getKeyCode() + "\nkeyChar(): " + e.getKeyChar());
					break;
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
	
	class FrameListener implements WindowListener {
		
		@Override
		public void windowClosed(WindowEvent e) {
			frame.setVisible(true);
			client.disconnectFromServer();
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@SuppressWarnings("serial")
	class GamePlaneBottom extends JPanel {
		private JButton			btnTeleport, btnSafeTeleport, btnAttack;
		private JLabel 			lblLevel, lblScore, lblSafeTp, lblAttacks;
		private int 			level;
		private int 			score;
		private int 			safeTP;
		private int 			attacks;
		private GameClient 		client;
		private ClientGamePlane gameHandler;
		private Player			playerObject;
		/**
		 * Create the panel.
		 */
		public GamePlaneBottom(GameClient client) {
			this.level 			= 1;
			this.score 			= 0;
			this.safeTP 		= 0;
			this.attacks 		= 0;
			this.client			= client;
			this.gameHandler	= client.getGameHandler();
			initialize();
		}
		
		public void updateValues() {
			// gets the playerObject from client
			if(playerObject == null) this.playerObject 	= client.getClientsPlayerObject();
			this.level = client.getLevel();
			
			lblLevel.setText	("Level: " 		+ this.level);
			lblScore.setText	("Score: " 		+ playerObject.getScore());
			lblSafeTp.setText	("SafeTP: " 	+ playerObject.getSafeTP());
			lblAttacks.setText	("Attacks: " 	+ playerObject.getAttacks());
		}
		
		private void initialize() {
			setLayout(new GridLayout(1, 0, 0, 0));
			
			lblLevel 		= new JLabel("Level: " 		+ this.level);
			lblScore 		= new JLabel("Score: " 		+ this.score);
			lblSafeTp 		= new JLabel("Safe TP: " 	+ this.safeTP);
			lblAttacks 		= new JLabel("Attacks: " 	+ this.attacks);
			
			btnTeleport 	= new JButton("Teleport");
			btnTeleport.setFocusable(false);
			btnTeleport.addActionListener(new ButtonListener());
			btnSafeTeleport	= new JButton("Safe TP");
			btnSafeTeleport.setFocusable(false);
			btnSafeTeleport.addActionListener(new ButtonListener());
			btnAttack		= new JButton("Attack");
			btnAttack.setFocusable(false);
			btnAttack.addActionListener(new ButtonListener());
			
			add(lblLevel);
			add(lblScore);
			add(lblSafeTp);
			add(lblAttacks);
			add(btnTeleport);
			add(btnSafeTeleport);
			add(btnAttack);
		}
		
		class ButtonListener implements ActionListener {
			String toSend = "";
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				switch(e.getActionCommand()) {
				case "Teleport":
					toSend = gameHandler.movePlayer(85, client.getPlayerObject());
					break;
				case "Safe TP":
					toSend = gameHandler.movePlayer(84, client.getPlayerObject());
					break;
				case "Attack":
					toSend = gameHandler.movePlayer(32, client.getPlayerObject());
					break;
				default:
					break;
				}
				
				client.sendMessageToServer(toSend);
				client.updateGameplane();
			}
			
		}
	}
	@SuppressWarnings("serial")
	class GamePlaneGUI extends JPanel {
		private int 					columnCount;
	    private int 					rowCount;
	    private ArrayList<Rectangle> 	cells;
	    private ArrayList<Point> 		selectedCell;
	    private GameClient				client;
	    private GameServer				server;
	    private ClientGamePlane			gameHandler;
	    private boolean					isServer = false;
	    
	    public GamePlaneGUI(GameClient client) {
	        this.cells 			= new ArrayList<>(columnCount * rowCount);
	        this.selectedCell 	= new ArrayList<Point>();
	        
	        if(client == null)
	        	isServer = true;
	        else {
		        this.client			= client;
		        this.gameHandler 	= client.getGameHandler();
	        }
	    }
	    
	    public void setHeightAndWidth(int rows, int cols) {
	    	this.rowCount 		= rows;
	    	this.columnCount 	= cols;
	    }
	    
	    public void setSelectedCell(Point p) {
	    	this.selectedCell.add(p);
	    }
	    
	    public void setServerObject(GameServer server) {
	    	this.server = server;
	    }
	    
	    public void updateGamePanel() {
	    	repaint();
	    }

	    @Override
	    public Dimension getPreferredSize() {
	        return new Dimension(rowCount * 20, columnCount * 20);
	    }

	    @Override
	    public void invalidate() {

	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        Graphics2D g2d = (Graphics2D) g.create();
	        
		    int width 			= getWidth();
		    int height 			= getHeight();

	        int cellWidth 	= width / columnCount;
	        int cellHeight 	= height / rowCount;

	        int xOffset 	= (width  - (columnCount * cellWidth)) 	/ 2;
	        int yOffset 	= (height - (rowCount 	 * cellHeight)) / 2;

	        if (cells.isEmpty()) {
	            for (int row = 0; row < rowCount; row++) {
	                for (int col = 0; col < columnCount; col++) {
	                    Rectangle cell = new Rectangle(
	                            xOffset + (col * cellWidth),
	                            yOffset + (row * cellHeight),
	                            cellWidth,
	                            cellHeight);
	                    cells.add(cell);
	                }
	            }
	        }
	        
	        g2d.setColor(Color.GRAY);
	        for (Rectangle cell : cells) {
	            g2d.draw(cell);
	        }
	        
	        if(isServer) {
	        	if(server.getPlayerList().size() > 0) {
	        		for(Point p : server.getGameplaneHandler().occupiedPositions) {
	        			int index = p.x + (p.y* columnCount);
		                Rectangle cell = cells.get(index);
		                g2d.setColor(Color.CYAN);
		                g2d.fill(cell);
	        		}
	        		for(Player player : server.getPlayerList()) {
		                int index = player.getX() + (player.getY() * columnCount);
		                Rectangle cell = cells.get(index);
		                
		                //the own player get blue color and the opponent are green
		                if(player.getType() == PlayerType.Agent)
		                	g2d.setColor(Color.BLUE);
		                
		                //robots are red
		                else if(player.getType() == Player.PlayerType.Robot)
		                	g2d.setColor(Color.RED);
		                
		                //rubble piles are black
		                else if(player.getType() == Player.PlayerType.Rubble)
		                	g2d.setColor(Color.BLACK);
		                
		                g2d.fill(cell);
		        	}
	        	}
	        }
	        else {
		        if (selectedCell != null && gameHandler.getPlayerList().size() > 0) {
		        	for(Point p2 : gameHandler.getOccupiedPositions()) {
		        		int index = p2.x + (p2.y* columnCount);
		                Rectangle cell = cells.get(index);
		                g2d.setColor(Color.CYAN);
		                g2d.fill(cell);
		        	}
		        	for(Player player : gameHandler.getPlayerList()) {
		                int index = player.getX() + (player.getY() * columnCount);
		                Rectangle cell = cells.get(index);
		                
		                //the own player get blue color and the opponent are green
		                if(client.getName().compareTo(player.getName()) == 0)
		                	g2d.setColor(Color.BLUE);
		                
		                //robots are red
		                else if(player.getType() == Player.PlayerType.Robot)
		                	g2d.setColor(Color.RED);
		                
		                //rubble piles are black
		                else if(player.getType() == Player.PlayerType.Rubble)
		                	g2d.setColor(Color.BLACK);
		                
		                else
		                	g2d.setColor(Color.GREEN);
		                
		                g2d.fill(cell);
		        	}
		        }
	        }
	        g2d.dispose();
	    }
	}
}




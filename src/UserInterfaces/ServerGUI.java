package UserInterfaces;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.*;

import ClientPackage.GameClient;
import HelpPackage.ClientGamePlane;
import HelpPackage.Player;
import HelpPackage.Player.PlayerType;
import ServerPackage.GameServer;

public class ServerGUI {
	private class UserScore {
		private String 	name;
		private String 	score;
		
		public UserScore(String name, String score) {
			this.name 	= name;
			this.score 	= score;
		}
		
		public String getName() 	{ return this.name; }
		public String getScore() 	{ return this.score; }
	}
	private JFrame 						frame, loggingWindow;
	private JPanel						bottomPanel;
	private JMenuBar 					menuBar;
	private JMenu 						menuArkiv, menuEdit;
	private JMenuItem 					menuItemStart, menuItemClose, menuItemProperties, menuItemKick;
	private JList<UserScore>			clientList;
	private JLabel 						connectionStatus;
	private JTextArea 					serverMessage, loggingTextArea;
	private JPopupMenu 					jlistPopup;
	private GameServer 					server;
	private DefaultListModel<UserScore>	names;
	private int 						itemSelectedInJList = 0;
	private int 						linesInServerMessage = 8;
	private JPanel 						panel;
	private GamePlaneGUI 				gpGUI;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
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
	public ServerGUI()
	{
		server = new GameServer();
		server.setGUIvariable(this);
		initialize();
	}
	
	/**
	 * Adds a client to connected clients list
	 * @param name	Name of client
	 */
	public void clientConnected(String name)
	{
		int score		= server.getGameplaneHandler().getPlayerScoreFromHighscoreList(name);
		UserScore us 	= new UserScore(name, Integer.toString(score));
		this.names.addElement(us);
		this.clientList.setModel(this.names);
	}
	
	/**
	 * Removes client from connected clients list
	 * @param name	Name of client
	 */
	public void clientDisconnected(String name)
	{
		this.names.removeElement(name);
		clientList.setModel(this.names);
	}
	
	/**
	 * Update gameplane
	 */
	public void updateGamePlane() {
		gpGUI.updateGamePanel();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		//creates all menu components
		frame 				= new JFrame();
		menuBar 			= new JMenuBar();
		menuArkiv 			= new JMenu("Arkiv");
		menuEdit 			= new JMenu("Edit");
		menuItemStart 		= new JMenuItem("Start server");
		menuItemClose 		= new JMenuItem("Close");
		menuItemProperties 	= new JMenuItem("Properties");
		menuItemKick 		= new JMenuItem("Kick");
		connectionStatus 	= new JLabel("Server offline");
		serverMessage 		= new JTextArea();
		jlistPopup 			= new JPopupMenu();
		bottomPanel 		= new JPanel(new BorderLayout());
		names 				= new DefaultListModel<UserScore>();
		
		UserScore us 		= new UserScore("Name", "Points");
		this.names.addElement(us);
		this.clientList 	= new JList<UserScore>(names);
		
		//custom CellRenderer to print the values from UserScore class
		this.clientList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			
			@Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel) {
                	UserScore us = (UserScore)value;
                	String toPrint = String.format("%-15s %s", us.getName(), us.getScore());
                	
                    ((JLabel) renderer).setText(toPrint);
                }
                return renderer;
            }
			
		});
		
		clientList.setVisibleRowCount(20);
		clientList.setFont(new Font("monospaced", Font.PLAIN, 12));
		
		//set listeners
		menuItemStart.addActionListener		(new MenuActionListener());
		menuItemClose.addActionListener		(new MenuActionListener());
		menuItemProperties.addActionListener(new MenuActionListener());
		menuItemKick.addActionListener		(new MenuActionListener());
		clientList.addMouseListener			(new JListMouseListener());
		
		//set properties to components
		clientList.setVisible(true);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setLayoutOrientation(JList.VERTICAL);
		serverMessage.setEnabled(false);
		serverMessage.setRows(linesInServerMessage);
		connectionStatus.setVisible(true);
		frame.setBounds(200, 200, 750, 750);
		frame.setTitle("Server");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//add the components to the menu
		menuArkiv.add(menuItemStart);
		menuArkiv.add(menuItemClose);
		menuEdit.add(menuItemProperties);
		jlistPopup.add(menuItemKick);
		
		//add components to the frame
		frame.getContentPane().add	(clientList, 		BorderLayout.LINE_END);
		bottomPanel.add				(serverMessage, 	BorderLayout.NORTH);
		bottomPanel.add				(connectionStatus, 	BorderLayout.SOUTH);
		frame.getContentPane().add	(bottomPanel, 		BorderLayout.PAGE_END);
		
		
		panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		
		menuBar.add(menuArkiv);
		menuBar.add(menuEdit);
		
		frame.setJMenuBar(menuBar);
		frame.setVisible(true);
	}
	
	/**
	 * Update highscore list on server GUI
	 */
	public void updateHighscoreList() {
		UserScore us					= null;
		names 							= new DefaultListModel<UserScore>();
		Map<String, Integer> highscore 	= server.getGameplaneHandler().getHighscoreList();
		
		//add header
		names.addElement(new UserScore("Name", "Points"));
		
		//copy highscore list from server to JList
		for(String key : highscore.keySet()) {
			us = new UserScore(key, Integer.toString(highscore.get(key)));
			names.addElement(us);
		}
		
		this.clientList.setModel(this.names);
		
	}
	
	/**
	 * Creates a new window for logging
	 */
	public void initLoggingWindow() {
		loggingWindow 				= new JFrame("Logg");
		loggingTextArea 			= new JTextArea(150, 50);
		JScrollPane loggingScroll 	= new JScrollPane(loggingTextArea);
		
		loggingWindow.setBounds(1000, 200, 400, 750);
		loggingWindow.add(loggingScroll);
		loggingWindow.setVisible(true);
	}
	
	/**
	 * Adds message to server TextArea
	 * @param Message to add
	 */
	public void addTextToServerMessageTextArea(String message)
	{
		String input 				= "";
		String messageToPrint 		= message;
		int count 					= 0;
		
		String theMessage 			= serverMessage.getText();
		BufferedReader stringReader = new BufferedReader(new StringReader(theMessage));
		try
		{
			while((input = stringReader.readLine()) != null && count < (linesInServerMessage-1))
			{
				messageToPrint += System.lineSeparator() + input;
				count++;
			}
		}
		catch(Exception e) { }
		
		serverMessage.setText(messageToPrint);
	}
	
	/**
	 * Adds text to seperate logging window
	 * @param message	Message to add
	 */
	public void addTextToLoggingWindow(String message) {
		if(loggingTextArea != null) {
			String input 				= "";
			String messageToPrint 		= message;
			
			String theMessage 			= loggingTextArea.getText();
			BufferedReader stringReader = new BufferedReader(new StringReader(theMessage));
			
			try {
				while((input = stringReader.readLine()) != null) {
					messageToPrint += System.lineSeparator() + input;
				}
			}
			catch(Exception e) { }
			
			loggingTextArea.setText(messageToPrint);
		}
	}
	
	
	class MenuActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand())
			{
			case "Start server":
				if(server.StartServer()) 
				{
					menuItemStart.setText("Stop server");
					connectionStatus.setText("Server online on port " + server.getPort());
					
					gpGUI = new GamePlaneGUI(null);
					gpGUI.setServerObject(server);
					gpGUI.setHeightAndWidth(server.getRows(), server.getColumns());
					
					panel.add(gpGUI);
					gpGUI.updateGamePanel();
				}
				break;
			case "Stop server":
				if(server.stopServer()) 
				{
					menuItemStart.setText("Start server");
					connectionStatus.setText("Server offline");
				}
				break;
			case "Close":
				server.stopServer();
				System.exit(0);
				break;
			case "Kick":
				if(itemSelectedInJList != 0)
				{
					//force disconnect message
					String message 	= "@102@" + "Kicked by server" + "@";
					
					String name 	= names.getElementAt(itemSelectedInJList).getName();
					GameClient gc 	= server.findClientByName(name);
					server.sendMessageToClient(gc.getClientSocket(), message);
					//server.SendMessageToClient(names.getElementAt(itemSelectedInJList), message);
					names.removeElementAt(itemSelectedInJList);
					itemSelectedInJList = 0;
					
					addTextToLoggingWindow("Server kicked player (" + name + ")");
				}
				break;
			case "Properties":
				ServerPropertiesGUI settings = new ServerPropertiesGUI();
				settings.recieveGameServer(server);
				break;
			}
		}
	}
	
	class JListMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if(SwingUtilities.isRightMouseButton(e))
			{
				//gets the location in the array of the item that was clicked
				itemSelectedInJList = clientList.locationToIndex(new Point(e.getX(), e.getY()));
				//shows the popup menu
				jlistPopup.show(clientList, e.getX(), e.getY());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
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

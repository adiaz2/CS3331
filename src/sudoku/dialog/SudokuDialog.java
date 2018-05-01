//Author: Alejandro Diaz & Ryan Jones & Ian Gilliam
//Assignment: CS3331 Lab 2 Sudoku: SudokuDialog.java
//Instructor: Yoonsik Cheon
//Last Modification: 3/3/2018

/*
Controls all instances of asking the user for input
Calls methods fromBoard and Board classes to create and 
update the sudoku board generated for the player.
*/

package sudoku.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;

import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import sudoku.model.Board;

/**
 * A dialog template for playing simple Sudoku games.
 * You need to write code for three callback methods:
 * newClicked(int), numberClicked(int) and boardClicked(int,int).
 *
 * @author Yoonsik Cheon
 */
@SuppressWarnings("serial")
public class SudokuDialog extends JFrame {

    /** Default dimension of the dialog. */
    private final static Dimension DEFAULT_SIZE = new Dimension(310, 430);

    private final static String IMAGE_DIR = "/image/";
    
    private Stack place = new Stack();
    private Stack removed = new Stack();

    /** Sudoku board. */
    private Board board;
    
    private LinkedList<Integer> errors;

    /** Special panel to display a Sudoku board. */
    private BoardPanel boardPanel;

    private ChatDialogUI cdUI;
    private Server servMain;
    private Client clientMain;
    private boolean isServer;
    private boolean isClient;
    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");
    
    private JToolBar toolBar = new JToolBar();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menu = new JMenu("Menu");
    
    private JPanel curButtons;// these two are necessary for updating buttons allowed. 
    private BorderLayout mainLayout = new BorderLayout(); 
    
    private JButton undoButton;
    private JButton redoButton;
    private JButton wirelessButton;
    
    private JMenuItem newGameMenuButton;
    private JMenuItem solveMenuButton;
    private JMenuItem checkMenuButton;
    
    private int lastMoveX = -1;
    private int lastMoveY = -1;
    private int lastMove = -1;
    private boolean undoPossible = false;
    
    private int redoMoveX = -1;
    private int redoMoveY = -1;
    private int redoMove = -1;
    private boolean redoPossible = false;
    
    /** Used to store the x,y coordinates that the user selects on the board. x = x_y/100; y = x_y%100;*/ 
    private int x_y;

    /** Create a new dialog. */
    public SudokuDialog() {
    		this(DEFAULT_SIZE);
    }
    
    /** Create a new dialog of the given screen dimension. */
    public SudokuDialog(Dimension dim) {
        super("Sudoku");
        setSize(dim);
        board = new Board(9);
        boardPanel = new BoardPanel(board, this::boardClicked);
        configureUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Callback to be invoked when a square of the board is clicked.
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     * Stores selected x,y coordinates in boardPanel and repaints to draw a black square over the coordinate selection
     */
    private void boardClicked(int x, int y) {
    		boardPanel.setX_y(x*100 + y); //store the selected coordinates into the boardPanel
    		repaint(); //repaint boardPanel to draw a black square over the coordinate selection
    }
    
    /**
     * Callback to be invoked when a number button is clicked.
     * @param number Clicked number (1-9), or 0 for "X".
     * @throws IOException 
     */
    private void numberClicked(int number) throws IOException{
    		//get the currently selected x and y values
    		x_y = boardPanel.getX_y(); //gets the user's currently selected coordinates
    		
    		//if the user has not selected a square, Display error message and exit function
    		if(x_y == -1) {
    			showMessage("No square selected");
    			return;
    		}
    		if(board.boardGenerated[x_y%100][x_y/100]) {
    			showMessage("This number cannot be changed");
    			return;
    		}
    			
    		
    		//if the user selects a previously filled square and chooses x, it deletes the value
    		if(board.boardInputs[x_y%100][x_y/100] != 0 && number == 0) {
    			//store the move
    			lastMoveX = x_y/100;
    			lastMoveY = x_y%100;
    			lastMove = board.boardInputs[x_y%100][x_y/100];
    			
    			int [] pushArray = {(lastMoveX),(lastMoveY), lastMove};
                place.push(pushArray);
                while(!removed.empty()){
                   removed.pop();
                }
    			
    			board.boardInputs[x_y%100][x_y/100] = number; //setting the value to 0 removes it from the board
    			
    				
    			board.undoMove(); //used to keep track of how many squares have been filled out in the board
    			repaint(); //update board with the value removed
    			return;
    		}
    		
    		
    		//check if the user's number is in the same row or column as the coordinate and is the right size
    		for(int i = 0; i<board.size; i++) {
    			if(board.boardInputs[x_y%100][i] == number || board.boardInputs[i][x_y/100] == number || number > board.size()) {
    				showMessage("Invalid Move\n"); //if we find this number in the same row or column, let the user know the move is invalid
    				return;
    			}
    		}
    		
    		//check if the user's number is in the same square
    		int col = (int) ((x_y/100) - (x_y/100)%Math.sqrt(board.size));//get to left of square that contains selected coordinate
    		int row = (int) ((x_y%100) - (x_y%100)%Math.sqrt(board.size));//get to top of square that contains selected coordinate
    		//start checking every value in the square
    		for(int i = row; i<Math.sqrt(board.size) + row; i++) { 
    			for(int j = col; j<Math.sqrt(board.size) + col; j++) {
    				if(board.boardInputs[i][j] == number) { 
    					showMessage("Invalid Move\n"); //if we find this number anywhere in the square, the move is invalid
    					return;
    				}	
    			}			
    		}
    		
    		//if everything looks good, add the value to the board
    		lastMoveX = x_y/100;
    		lastMoveY = x_y%100;
    		lastMove = board.boardInputs[x_y%100][x_y/100];
    	    
    		if(lastMove == -1) {
    		   int [] pushArray = {(lastMoveX),(lastMoveY), number};
               place.push(pushArray);
    		}else {
    		  int [] pushArray = {(lastMoveX),(lastMoveY), number};
              place.push(pushArray);
              while(!removed.empty()){
                 removed.pop();
              }
    		}
    		board.boardInputs[x_y%100][x_y/100] = number;
    		
    		if(isServer) {
				String[] move = {Integer.toString(x_y%100), Integer.toString(x_y/100), Integer.toString(number)};
				servMain.sendMessage(move);
			}else if(isClient) {
				String[] move = {Integer.toString(x_y%100), Integer.toString(x_y/100), Integer.toString(number)};
				clientMain.sendMessage(move);
			}
    		
    		showMessage(""); //clear any previous error messages
    		board.playerMove(); //update the number of squares that have been filled
    		boardPanel.setX_y(-1); //Make it so no square is selected now
    		repaint();
    		checkWin();
    		
    }
    
    public void undo() {
    	if(!(place.empty())) {
            int [] hld = (int [])place.pop();
            removed.push(hld);
            //System.out.println(hld[2]); 
	    	board.boardInputs[hld[1]][hld[0]] = 0; 
	    	repaint();
    	}
    	else
    		showMessage("Nothing to Undo");
    }
    
    public void redo() {
    	if(!(removed.empty())) {
    		int [] hld = (int [])removed.pop();
            place.push(hld);
	    	board.boardInputs[hld[1]][hld[0]] = hld[2];

	    	repaint();
    	}
    	else
    		showMessage("Nothing to Redo");
    }
    
    public int selectPort()
    { 	
    	JFrame frame = new JFrame();
    	return Integer.parseInt(JOptionPane.showInputDialog(frame.getContentPane(),"Please enter port to attempt connection","Port Selection",JOptionPane.PLAIN_MESSAGE));
    }
    
    public String selectIP()
    {
    	JFrame frame = new JFrame();
    	return JOptionPane.showInputDialog(frame.getContentPane(),"Please enter host IP to attempt connection","IP Selection",JOptionPane.PLAIN_MESSAGE);
    }
    
    public void wirelessStart() throws IOException, Exception {
    	//First lets see if a user would like to host or connect to 
    	int port = 8000;
    	String host_ip;
    	
    	boolean isBindedToPort = false;
    	int tries = 5;
    	String serverIP = InetAddress.getLocalHost().getHostAddress();
    	if(verifyHost()) {
    		while (true){
    			try {
    				if (tries <= 0){
	    				JOptionPane.showMessageDialog(null, "Error: out of binding tries","Error Message",JOptionPane.ERROR_MESSAGE);
	    				break;
	    			}
    				tries--;
		    		port = selectPort();
		    		JOptionPane.showMessageDialog(null, "Server started on port: " + Integer.toString(port) + " and IP: " + serverIP,"Server Message",JOptionPane.INFORMATION_MESSAGE);
		    		//System.out.println("Trying port:" + port);//debuggin' code
		    		//System.out.println("Num tries left: " + bindingTries);
		    		servMain = new Server(boardPanel, port);
		    			    		
		    		break;
	    		}catch (BindException e){
	    			JOptionPane.showMessageDialog(null, "Error: port already in use","Error Message",JOptionPane.ERROR_MESSAGE);
	    		}
    		}
    		isServer = true;
    		servMain.setBoard(board);
    		servMain.sendBoard(board.boardInputs);
    		servMain.sendBoard(board.solvedPuzzle);
    		servMain.start();
    		JOptionPane.showMessageDialog(null, "Connection Successfully Established!" ,"Server Message",JOptionPane.INFORMATION_MESSAGE);
    	}else {
    	   //ChatDialogUI cdUI = new ChatDialogUI();
    	   //cdUI.setVisible(true);
	       //cdUI.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    		while (true) {
    			if (tries <= 0){
    				JOptionPane.showMessageDialog(null, "Error: out of connection tries","Error Message",JOptionPane.ERROR_MESSAGE);
    				break;
    			}
				tries--;
	    		port = selectPort();
	    		host_ip = selectIP();
	    		try {
	    			clientMain = new Client(boardPanel, host_ip, port);
	    			break;
	    		}catch(ConnectException e){
	    			JOptionPane.showMessageDialog(null, "Error: connection refused","Error Message",JOptionPane.ERROR_MESSAGE);
	    		}catch(UnknownHostException e) {
	    			JOptionPane.showMessageDialog(null, "Error: unknown host: \"" + host_ip + "\"","Error Message",JOptionPane.ERROR_MESSAGE);
	    		}
    		}
    		isClient = true;
    		int[][] bI = clientMain.getBoard();
    		int[][] solution = clientMain.getBoard();
    		initializeBoard(bI, solution);
    		boardPanel.setBoard(board);
    		clientMain.setBoard(board);
    		repaint();
    		clientMain.start();
    	}   
    }

	private void initializeBoard(int[][] bI, int[][] solution){
    	board = new Board(bI.length);
    	board.boardInputs = bI;
    	board.solvedPuzzle = solution;
    	for(int i = 0; i<board.boardInputs.length; i++) {
			for(int j = 0; j<board.boardInputs.length; j++) {
				if(board.boardInputs[i][j] != 0)
					board.boardGenerated[i][j] = true;
				else
					board.boardGenerated[i][j] = false;
			}
		}
    }
    
    /**This will help to determine if a user wants to connect to the server or just a chat dialog. */
    private boolean verifyHost() {
       JFrame frame = new JFrame();
       String[] options = new String[2];
       options[0] = new String("HOST");
       options[1] = new String("CLIENT");
       int opt = JOptionPane.showOptionDialog(frame.getContentPane(),"Please select if you would like to host or not.","Wireless Connection!", 0,JOptionPane.YES_NO_OPTION,null,options,null);
       if(opt == JOptionPane.YES_OPTION){
          return true;
       }else{
          return false;
       }

    }
    public void checkWin() {
    	//if user successfully completes the board, congratulate and offer to start new game
		if(board.isSolved()) {
			int reply = JOptionPane.showConfirmDialog(null, "Congratulations!!!\nStart a New Game?");
			if (reply == JOptionPane.YES_OPTION)
		    {
				board = new Board(board.size); //keeps the same size of current board
		        boardPanel.setBoard(board); //makes the board panel use a new board
		        repaint();
		    }
		}
    }
    
    /**
     * Callback to be invoked when a new button is clicked.
     * If the current game is over, start a new game of the given size;
     * otherwise, prompt the user for a confirmation and then proceed
     * accordingly.
     * @param size Requested puzzle size, either 4 or 9.
     * @throws IOException 
     */
    private void newClicked(int size) throws IOException {
    		//if board has not been solved, ask the user if they want to quit and start a new game
    		if(!board.isSolved()) {
    			int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit the current game and start a new one?");
    			//if user says no, don't do anything else
    			if (reply == JOptionPane.NO_OPTION || reply == JOptionPane.CANCEL_OPTION)
    		    {
    		      return;
    		    }
    		}
    		if(isServer) {
    			//sendSolution(1);
    			String[] s = {"new"};
    			servMain.sendMessage(s);
    			if(servMain.newGameDeclined)
    				return;
    			//clear out the board and add a new board of the requested size
        		boardPanel.removeAll();
        		board = new Board(size);
        		boardPanel.setBoard(board);
    			servMain.sendBoard(board.boardInputs);
        		servMain.sendBoard(board.solvedPuzzle);
    		}
    		else if(isClient) {
    			//sendSolution(2);
    			String[] s = {"new"};
    			clientMain.sendMessage(s);
    			if(clientMain.newGameDeclined)
    				return;
    			//clear out the board and add a new board of the requested size
        		boardPanel.removeAll();
        		board = new Board(size);
        		boardPanel.setBoard(board);
    			clientMain.sendBoard(board.boardInputs);
        		clientMain.sendBoard(board.solvedPuzzle);
    		}
        //These two commented out will enable it to show less buttons when a new game is selected, 
        // however the screen when a new game is made will be blanked until you try to widen or shrink the screen 
        //then it will show up properly. 
        //removeCurButtons();
        //configureUI();
        repaint();
    }
    
    /**
     * Display the given string in the message bar.
     * @param msg Message to be displayed.
     */
    private void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configure the UI. */
    private void configureUI() {

        setIconImage(createImageIcon("sudoku.png").getImage());
        setLayout(mainLayout);
        
        curButtons = makeControlPanel();
        // boarder: top, left, bottom, right
        curButtons.setBorder(BorderFactory.createEmptyBorder(8,16,0,16));
        add(curButtons, BorderLayout.NORTH);
        
        JPanel board = new JPanel();
        board.setBorder(BorderFactory.createEmptyBorder(8,16,0,16));
        board.setLayout(new GridLayout(1,1));

        board.add(boardPanel);
        add(board, BorderLayout.CENTER);
        
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        add(msgBar, BorderLayout.SOUTH);
    }
    /** Determines what size of board the user will play with*/
    private int verifySize() {
        JFrame frame = new JFrame();
        String[] options = new String[2];
        options[0] = new String("4x4");
        options[1] = new String("9x9");
        int opt = JOptionPane.showOptionDialog(frame.getContentPane(),"Please select what size of board","New Game!", 0,JOptionPane.YES_NO_OPTION,null,options,null);
        if(opt == JOptionPane.YES_OPTION){
           return 4;
        }else{
           return 9;
        }

     }
    /**Removes all current buttons for no overlay */
    private void removeCurButtons(){
        curButtons = null;
        remove(mainLayout.getLayoutComponent(BorderLayout.NORTH));
        remove(mainLayout.getLayoutComponent(BorderLayout.CENTER));
     }
    /** Create a control panel consisting of new and number buttons. */
    private JPanel makeControlPanel() {
    	
    	// buttons labeled 1, 2, ..., 9, and X.
    	JPanel numberButtons = new JPanel(new FlowLayout());
    	int maxNumber = board.size() + 1;
    	for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            button.setFocusPainted(false);
            button.setMargin(new Insets(0,2,0,2));
            button.addActionListener(e -> {
				try {
					numberClicked(number);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
            if(i <= board.size || i == maxNumber) {
               numberButtons.add(button);
            }
    	}
;



       JMenuBar menuBar = new JMenuBar();

       JMenu menu = new JMenu("Game");
       menu.setMnemonic(KeyEvent.VK_G);
       menu.getAccessibleContext().setAccessibleDescription("Game menu");
       menuBar.add(menu);

   	 
   	   
  	   

       // This adds the first menu item.
       JMenuItem menuItem = new JMenuItem("New game", KeyEvent.VK_N);
       try{
          Image img = ImageIO.read(getClass().getResource("image/playImage.png"));
          menuItem.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }

       menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
       menuItem.addActionListener(e -> {
          try {
			newClicked(verifySize());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       });

       menuItem.getAccessibleContext().setAccessibleDescription("Play a new game");
       menu.add(menuItem);


       //This adds the second menu item
       JMenuItem menuItem2 = new JMenuItem("Solve Puzzle", KeyEvent.VK_S);
       try{
          Image img = ImageIO.read(getClass().getResource("image/solveImage.png"));
          menuItem2.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }
       menuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
       menuItem2.addActionListener(e -> {
		try {
			buttonPressed(2);
		} catch (IOException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
	});

       menuItem2.getAccessibleContext().setAccessibleDescription("Print Board Solved.");
       menu.add(menuItem2);


       // THis adds the third menu item
       JMenuItem menuItem3 = new JMenuItem("Determine if solvable", KeyEvent.VK_I);
       try{
          Image img = ImageIO.read(getClass().getResource("image/isSolve.png"));
          menuItem3.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }
       menuItem3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
       menuItem3.addActionListener(e -> {
		try {
			buttonPressed(3);
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
	});
       menuItem3.getAccessibleContext().setAccessibleDescription("Find if solvable.");
       menu.add(menuItem3);

       // THis adds the fourth menu item
       JMenuItem menuItem4 = new JMenuItem("Undo previous move.", KeyEvent.VK_Z);
       try{
          Image img = ImageIO.read(getClass().getResource("image/undoImage.png"));
          menuItem4.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }
       menuItem4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
       menuItem4.addActionListener(e -> undo());
       menuItem4.getAccessibleContext().setAccessibleDescription("Undo the previous move made.");
       menu.add(menuItem4);

       // THis adds the fifth menu item
       JMenuItem menuItem5 = new JMenuItem("Replace the previous undo.", KeyEvent.VK_R);
       try{
          Image img = ImageIO.read(getClass().getResource("image/replaceImage.png"));
          menuItem5.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }
       menuItem5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
       menuItem5.addActionListener(e -> redo());
       menuItem5.getAccessibleContext().setAccessibleDescription("Replace the previous undo");
       menu.add(menuItem5);

       // THis adds the wireless menu option. 
       JMenuItem menuItem6 = new JMenuItem("Connect wirelessly to another player.", KeyEvent.VK_Q);
       try{
          Image img = ImageIO.read(getClass().getResource("image/wireless.png"));
          menuItem6.setIcon(new ImageIcon(img));
       }catch(Exception e){
          System.out.println("Error with image");
       }
       menuItem6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
       menuItem6.addActionListener(e -> {
		try {
			wirelessStart();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	});
       menuItem6.getAccessibleContext().setAccessibleDescription("Connect to another player.");
       menu.add(menuItem6);


       JToolBar toolBar = new JToolBar("Sudoku");//int top, int left, int bottom, int right
       toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,0));
       menu.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
       toolBar.setAlignmentX(CENTER_ALIGNMENT);
       menu.setAlignmentX(CENTER_ALIGNMENT);
       // buttons labeled 1, 2, ..., 5
       for (int i = 0; i <= 5; i++) {
          switch(i){
             case(0):
                JButton button = new JButton();
                button.setMargin(new Insets(0,0,0,0));
                try{
                   Image img = ImageIO.read(getClass().getResource("image/playImage.png"));
                   button.setIcon(new ImageIcon(img));
                }catch(Exception e){
                   System.out.println("Error with image");
                }
                button.setFocusPainted(false);

                button.setFocusPainted(false);
                button.addActionListener(e -> {
                      try {
						newClicked(verifySize());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                   });

                button.setToolTipText("Play a new game");
                toolBar.add(button);
                break;
             case(1):JButton button2 = new JButton();
                button2.setMargin(new Insets(0,0,0,0));
                try{
                   Image img = ImageIO.read(getClass().getResource("image/solveImage.png"));
                   button2.setIcon(new ImageIcon(img));
                }catch(Exception e){
                   System.out.println("Error with image");
                }
                button2.setFocusPainted(false);

                button2.setFocusPainted(false);
                button2.addActionListener(e -> {
					try {
						buttonPressed(2);
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
				});

                button2.setToolTipText("Solve The Puzzle");
                toolBar.add(button2);
                break;
             case(2):JButton button3 = new JButton();
                button3.setMargin(new Insets(0,0,0,0));
                try{
                   Image img = ImageIO.read(getClass().getResource("image/isSolve.png"));
                   button3.setIcon(new ImageIcon(img));
                }catch(Exception e){
                   System.out.println("Error with image");
                }
                button3.setFocusPainted(false);

                button3.setFocusPainted(false);
                button3.addActionListener(e -> {
					try {
						buttonPressed(3);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				});

                button3.setToolTipText("See if the puzzle is solvable");
                toolBar.add(button3);
                break;
             case(3):JButton button4 = new JButton();
                button4.setMargin(new Insets(0,0,0,0));
                try{
                   Image img = ImageIO.read(getClass().getResource("image/undoImage.png"));
                   button4.setIcon(new ImageIcon(img));
                }catch(Exception e){
                   System.out.println("Error with image");
                }
                button4.setFocusPainted(false);

                button4.setFocusPainted(false);
                button4.addActionListener(e ->  undo());

                button4.setToolTipText("Undo previous move.");
                toolBar.add(button4);
                break;
             case(4):JButton button5 = new JButton();
                button5.setMargin(new Insets(0,0,0,0));
                try{
                   Image img = ImageIO.read(getClass().getResource("image/replaceImage.png"));
                   button5.setIcon(new ImageIcon(img));
                }catch(Exception e){
                   System.out.println("Error with image");
                }
                button5.setFocusPainted(false);

                button5.setFocusPainted(false);
                button5.addActionListener(e -> redo());

                button5.setToolTipText("Replace removed value.");
                toolBar.add(button5);
                break;
             case(5): JButton button6 = new JButton();
             button6.setMargin(new Insets(0,0,0,0));
             try{
                Image img = ImageIO.read(getClass().getResource("image/wireless.png"));
                button6.setIcon(new ImageIcon(img));
             }catch(Exception e){
                System.out.println("Error with image");
             }
             button6.setFocusPainted(false);

             button6.setFocusPainted(false);
             button6.addActionListener(e ->  {
				try {
					wirelessStart();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});

             button6.setToolTipText("Connect wirelessly to another player!!!");
             toolBar.add(button6);
             break;
          }

       }// for loop



       JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(menuBar);
        content.add(toolBar);
    	  //content.add(newButtons);
        content.add(numberButtons);

        return content;
    }
    	
    void sendSolution(int choice) throws IOException {
    	for(int i = 0; i<board.size; i++) {
    		for(int j = 0; j<board.size; j++) {
    			String[] move = {Integer.toString(i), Integer.toString(j), Integer.toString(board.boardInputs[i][j])};
    			if(isServer) {
    				servMain.sendMessage(move);
    			}
    			else if(isClient) {
    				clientMain.sendMessage(move);
    			}
    		}
    	}
    }

    
    //handles when a tool bar button is pressed
    public void buttonPressed(int choice) throws IOException {
    	switch(choice) 
    	{
    	case 1:  newClicked(board.size());
        	break;
    	case 2:  board.solve();
    		if(isServer) {
    			//sendSolution(1);
    			String[] s = {"solve"};
    			servMain.sendMessage(s);
    		}
    		else if(isClient) {
    			//sendSolution(2);
    			String[] s = {"solve"};
    			clientMain.sendMessage(s);
    		}
        	break;
    	case 3:  errors = board.check();
    		boardPanel.setErrors(errors);
    		if(errors.size() == 0)
    			showMessage("No Errors Found!");
    		if(isServer) {
    			//sendSolution(1);
    			String[] s = {"check"};
    			servMain.sendMessage(s);
    		}
    		else if(isClient) {
    			//sendSolution(2);
    			String[] s = {"check"};
    			clientMain.sendMessage(s);
    		}
        	break;
    	default:
    		break;
    	}
    	repaint();
    	checkWin();
    }

    /** Create an image icon from the given image file. */
    private ImageIcon createImageIcon(String filename) {
        URL imageUrl = getClass().getResource(IMAGE_DIR + filename);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl);
        }
        return null;
    }
	

    public static void main (String[] args) {
        new SudokuDialog();
    }
}

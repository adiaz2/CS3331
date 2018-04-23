//Author: Alejandro Diaz
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
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

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
    

    /** Sudoku board. */
    private Board board;
    
    private LinkedList<Integer> errors;

    /** Special panel to display a Sudoku board. */
    private BoardPanel boardPanel;

    /** Message bar to display various messages. */
    private JLabel msgBar = new JLabel("");
    
    private JToolBar toolBar = new JToolBar();
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menu = new JMenu("Menu");
    
    private JButton undoButton;
    private JButton redoButton;
    
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
     */
    private void numberClicked(int number){
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
    			redoPossible = false;
    			undoPossible = true;
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
    		redoPossible = false;
    		undoPossible = true;
    		board.boardInputs[x_y%100][x_y/100] = number;
    		showMessage(""); //clear any previous error messages
    		board.playerMove(); //update the number of squares that have been filled
    		boardPanel.setX_y(-1); //Make it so no square is selected now
    		repaint();
    		
    		checkWin();
    		
    }
    
    public void undo() {
    	if(undoPossible) {
	    	redoMoveX = lastMoveX;
	    	redoMoveY = lastMoveY;
	    	redoMove = board.boardInputs[lastMoveY][lastMoveX];
	    	board.boardInputs[lastMoveY][lastMoveX] = lastMove;
	    	undoPossible = false;
	    	redoPossible = true;
	    	repaint();
    	}
    	else
    		showMessage("Nothing to Undo");
    }
    
    public void redo() {
    	if(redoPossible) {
	    	lastMoveX = redoMoveX;
	    	lastMoveY = redoMoveY;
	    	lastMove = board.boardInputs[redoMoveY][redoMoveX];
	    	board.boardInputs[redoMoveY][redoMoveX] = redoMove;
	    	redoPossible = false;
	    	undoPossible = true;
	    	repaint();
    	}
    	else
    		showMessage("Nothing to Redo");
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
     */
    private void newClicked(int size) {
    		//if board has not been solved, ask the user if they want to quit and start a new game
    		if(!board.isSolved())
    		{
    			int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit the current game and start a new one?");
    			//if user says no, don't do anything else
    			if (reply == JOptionPane.NO_OPTION || reply == JOptionPane.CANCEL_OPTION)
    		    {
    		      return;
    		    }
    		}
    		//clear out the board and add a new board of the requested size
    		boardPanel.removeAll();
        board = new Board(size);
        boardPanel.setBoard(board);
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
        setLayout(new BorderLayout());
        
        JPanel buttons = makeControlPanel();
        // boarder: top, left, bottom, right
        buttons.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        add(buttons, BorderLayout.NORTH);
        
        JPanel board = new JPanel();
        board.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        board.setLayout(new GridLayout(1,1));
        board.add(boardPanel);
        add(board, BorderLayout.CENTER);
        
        msgBar.setBorder(BorderFactory.createEmptyBorder(10,16,10,0));
        add(msgBar, BorderLayout.SOUTH);
    }
      
    /** Create a control panel consisting of new and number buttons. */
    private JPanel makeControlPanel() {
    	JPanel newButtons = new JPanel(new FlowLayout());
        JButton new4Button = new JButton("New (4x4)");
        for (JButton button: new JButton[] { new4Button, new JButton("New (9x9)")}) {
        	button.setFocusPainted(false);
            button.addActionListener(e -> {
                newClicked(e.getSource() == new4Button ? 4 : 9);
            });

            newButtons.add(button);
    	}
               
    	newButtons.setAlignmentX(LEFT_ALIGNMENT);
        
    	makeToolbar();
    	newButtons.add(toolBar);
    	newButtons.add(menuBar);
    	//frame.setJMenuBar(menuBar);
    	
    	// buttons labeled 1, 2, ..., 9, and X.
    	JPanel numberButtons = new JPanel(new FlowLayout());
    	int maxNumber = board.size() + 1;
    	for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            button.setFocusPainted(false);
            button.setMargin(new Insets(0,2,0,2));
            button.addActionListener(e -> numberClicked(number));
    		numberButtons.add(button);
    	}
    	numberButtons.setAlignmentX(LEFT_ALIGNMENT);

    	JPanel content = new JPanel();
    	content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(newButtons);
        content.add(numberButtons);
        return content;
    }
    
    public void makeToolbar() {
    	menu.setMnemonic(KeyEvent.VK_M);
    	menuBar.add(menu);
    	undoButton = new JButton("Undo");
    	redoButton = new JButton("Redo");
    	
    	newGameMenuButton = new JMenuItem("New Game");
    	solveMenuButton = new JMenuItem("Solve");
    	checkMenuButton = new JMenuItem("Check");
    	Image img;
		try {
			img = ImageIO.read(getClass().getResource(IMAGE_DIR + "undo.png"));
			undoButton.setIcon(new ImageIcon(img));
			img = ImageIO.read(getClass().getResource(IMAGE_DIR + "redo.png"));
			redoButton.setIcon(new ImageIcon(img));
			img = ImageIO.read(getClass().getResource(IMAGE_DIR + "newGame.png"));
			newGameMenuButton.setIcon(new ImageIcon(img));
			img = ImageIO.read(getClass().getResource(IMAGE_DIR + "solve.png"));
			solveMenuButton.setIcon(new ImageIcon(img));
			img = ImageIO.read(getClass().getResource(IMAGE_DIR + "check.png"));
			checkMenuButton.setIcon(new ImageIcon(img));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	//ImageIcon img = new ImageIcon(IMAGE_DIR + "sudoku.png");
    	
    	undoButton.setToolTipText("Play a new game.");
    	newGameMenuButton.setMnemonic(KeyEvent.VK_N);
    	newGameMenuButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    	toolBar.add(undoButton);
    	menu.add(newGameMenuButton);
    	undoButton.addActionListener(h -> undo());
    	newGameMenuButton.addActionListener(d -> buttonPressed(1));
    	
    	redoButton.setToolTipText("Solve the current puzzle.");
    	solveMenuButton.setMnemonic(KeyEvent.VK_S);
    	solveMenuButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    	toolBar.add(redoButton);
    	menu.add(solveMenuButton);
    	redoButton.addActionListener(j -> redo());
    	solveMenuButton.addActionListener(d -> buttonPressed(2));
    	
    	checkMenuButton.setMnemonic(KeyEvent.VK_C);
    	checkMenuButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    	menu.add(checkMenuButton);
    	checkMenuButton.addActionListener(d -> buttonPressed(3));
    	
    }
    //handles when a tool bar button is pressed
    public void buttonPressed(int choice) {
    	switch(choice) 
    	{
    	case 1:  newClicked(board.size());
        	break;
    	case 2:  board.solve();
        	break;
    	case 3:  errors = board.check();
    		boardPanel.setErrors(errors);
    		if(errors.size() == 0)
    			showMessage("No Errors Found!");
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

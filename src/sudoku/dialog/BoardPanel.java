//Author: Alejandro Diaz
//Assignment: CS3331 Lab 2 Sudoku: BoardPanel.java
//Instructor: Yoonsik Cheon
//Last Modification: 3/3/2018

/*
Creates a GUI version of sudoku for the user to play on. 
Controls all instances of outputting information for the user
*/
package sudoku.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JPanel;

import sudoku.model.Board;


/**
 * A special panel class to display a Sudoku board modeled by the
 * {@link sudoku.model.Board} class. You need to write code for
 * the paint() method.
 *
 * @see sudoku.model.Board
 * @author Yoonsik Cheon
 */
@SuppressWarnings("serial")
public class BoardPanel extends JPanel {
	
	
	public interface ClickListener {
		
		/** Callback to notify clicking of a square. 
		 * 
		 * @param x 0-based column index of the clicked square
		 * @param y 0-based row index of the clicked square
		 */
		void clicked(int x, int y);
	}
	
    /** Background color of the board. */
	private static final Color boardColor = new Color(247, 223, 150);

    /** Board to be displayed. */
    private Board board;

    /** Width and height of a square in pixels. */
    private int squareSize;
    
    private int highlightSize;
    
    /** Holds the currently selected square */
    public int x_y = -1;
    
    public LinkedList<Integer> errors;
    

    /** Create a new board panel to display the given board. */
    public BoardPanel(Board board, ClickListener listener) {
        this.board = board;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	int xy = locateSquaree(e.getX(), e.getY());
            	if (xy >= 0) {
            		listener.clicked(xy / 100, xy % 100);
            	}
            }
        });
    }

    /** Set the board to be displayed. */
    public void setBoard(Board board) {
    	this.board = board;
    }
    
    /**
     * Given a screen coordinate, return the indexes of the corresponding square
     * or -1 if there is no square.
     * The indexes are encoded and returned as x*100 + y, 
     * where x and y are 0-based column/row indexes.
     */
    private int locateSquaree(int x, int y) {
    	if (x < 0 || x > board.size * squareSize
    			|| y < 0 || y > board.size * squareSize) {
    		return -1;
    	}
    	int xx = x / squareSize;
    	int yy = y / squareSize;
    	return xx * 100 + yy;
    }
    
    /**stores the x and y values of the square the user selected */
    void setX_y(int x_y) {
		this.x_y = x_y;
	}
    
    //returns the currently selected x and y values
    int getX_y() {
		return x_y;
	}
    
    public void setErrors(LinkedList<Integer> e) {
    	errors = e;
    }
    public void clearErrors() {
    	errors = null;
    }

    /** Draw the associated board. */
    @Override
    public void paint(Graphics g) {
        super.paint(g); 

        // determine the square size
        Dimension dim = getSize();
        squareSize = Math.min(dim.width, dim.height) / board.size;
        highlightSize = squareSize-1;
        // draw background
        g.setColor(boardColor);
        g.fillRect(0, 0, squareSize * board.size, squareSize * board.size);
        
        //Draw Grid
        for(int i = 0; i<=board.size; i++) {
        		if(i%Math.sqrt(board.size) == 0) {
        			g.setColor(Color.black);
        		}
        		else {
        			g.setColor(Color.gray);
        		}
        		g.drawLine(0, squareSize *i, board.size*squareSize, squareSize *i);
        		g.drawLine(squareSize *i, 0, squareSize *i, board.size*squareSize);
        }
        
        for(int i = 0; i<board.size; i++) {
    		for(int j = 0; j<board.size; j++) {
    			g.setColor(Color.LIGHT_GRAY);
    			if(board.boardGenerated[i][j]) {
    				g.fillRect(squareSize *(j), squareSize * (i), highlightSize, highlightSize);
    			}
    		}
        }
        
        //if the user currently has selected a square highlights it with a pink square
        if(x_y > -1) {
        		g.setColor(Color.pink);
			g.fillRect(squareSize *(x_y/100), squareSize * (x_y%100), highlightSize, highlightSize);
        }
        if(errors != null) {
	        for(int i = 0; i<errors.size(); i++) {
	        	g.setColor(Color.RED);
	        	g.fillRect(squareSize *(errors.get(i)/100), squareSize * (errors.get(i)%100), highlightSize, highlightSize);
	        }
	        errors = null;
        }
        for(int i = 0; i<board.size; i++) {
        		for(int j = 0; j<board.size; j++) {
        			g.setColor(Color.black);
        			if(board.boardInputs[i][j] != 0)
        				g.drawString(Integer.toString(board.boardInputs[i][j]), squareSize/3 + squareSize *j, squareSize*5/8 + squareSize *i);
        		}
        	}
        
        
        
    }

}

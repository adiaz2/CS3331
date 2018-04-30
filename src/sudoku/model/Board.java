//Author: Alejandro Diaz
//Assignment: CS3331 Lab 2 Sudoku: Board.java
//Instructor: Yoonsik Cheon
//Last Modification: 3/3/2018

/*
Stores all information relating to the board including previous inputs to the board,
size, and if it has been solved. Accessed by main to get information
*/


package sudoku.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

/** An abstraction of Sudoku puzzle. */
public class Board {
	private int numEmpty; //used to count number of empty squares left. when 0, the board is solved	
	public int[][] solvedPuzzle; //stores the solution of the current puzzle
	public int[][] boardInputs; //2D array that keeps track of all the values the user has put into the board
	public boolean[][] boardGenerated;
	public String fileName;
	public int randPuzzle;
	public int col;
	public int row;
    /** Size of this board (number of columns/rows). */
    public final int size;
    FileReader fileReader;
    BufferedReader bufferedReader;

    /** Create a new board of the given size. **/
    public Board(int size){
        this.size = size;
		numEmpty = size*size; //there are n^2 squares in the board
		newPuzzle();
    }
    
    public Board(int[][] b){
        this.size = b.length;
		numEmpty = b.length*b.length; //there are n^2 squares in the board
		boardInputs = b;
    }

    /** Return the size of this board. */
    public int size() {
    	return size;
    }
    
    /** Return the number of empty squares in the board **/
    public int getNumEmpty(){
    		return numEmpty;
    }
    public int getNumFilled(){
		return (size*size)-numEmpty;
    }
    /** Keep track of squares that have been filled. */
	public void playerMove() {
		numEmpty--;
	}
	/** Keep track of squares that have been emptied */
	public void undoMove() {
		numEmpty++;
	}
	public void setValue(int x, int y, int val) {
		if(x>=0 && x<size && y>=0 && y<size)
			boardInputs[x][y] = val;
	}
	public int getValue(int x, int y) {
		return boardInputs[x][y];
	}
	
	/** Check if board is solved by checking if every square in the board is filled */
	public boolean isSolved() {
		return numEmpty==0;
	}
	
	public void newPuzzle(){
		try {
			solvedPuzzle = new int[size][size]; //reset the current puzzle
			boardGenerated = new boolean[size][size];
			//read in from the appropriate file
			fileName = "puzzles" + size + ".txt";
	        fileReader = new FileReader(fileName);
	        bufferedReader = new BufferedReader(fileReader);
	        
	        //pick a random puzzle
	        Random rand = new Random();
	        randPuzzle = rand.nextInt(60);
	        
	        //find the column and row of that puzzle
			col = randPuzzle%4;
			row = randPuzzle/4;

			//go down to the first row of the puzzle
			for(int i = 0; i<row*(size+1); i++) {
				bufferedReader.readLine();
			}
			for(int i = 0; i<size; i++) {
				//go to the first column of the puzzle
				for(int k = 0; k<col*(size+1); k++) {
					bufferedReader.read();
				}
				for(int j = 0; j<size; j++) {
					solvedPuzzle[i][j] = bufferedReader.read()-48;
				}
				bufferedReader.readLine(); //go to the next line
			}
			
			bufferedReader.close();
			
			boardInputs = new int[size][size]; //reset values on board
			
			int numClues;
			int i = 0;
			
			if(size == 9)
				numClues = 30;
			else
				numClues = 8;
			
			while(i<numClues) {
				col = rand.nextInt(size);
				row = rand.nextInt(size);
				if(boardInputs[row][col] == 0) {
					boardGenerated[row][col] = true;
					boardInputs[row][col] = solvedPuzzle[row][col];
					numEmpty--;
					i++;
				}	
			}
		}
		catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
        }
	}

	
	public void solve() {
		boardInputs = solvedPuzzle;
		numEmpty = 0;
	}

	public LinkedList<Integer> check() {
		LinkedList<Integer> errors = new LinkedList<Integer>();
		for(int i = 0; i<size; i++) {
			for(int j = 0; j<size; j++) {
				if(boardInputs[i][j] != 0 && boardInputs[i][j] != solvedPuzzle[i][j]) {
					errors.add(j*100 + i);
					System.out.println("error at row: " + (i+1) + " col: " + (j+1));
				}
			}
		}
		return errors;
	}

}

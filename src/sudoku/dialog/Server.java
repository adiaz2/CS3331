package sudoku.dialog;


import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.awt.*;

import sudoku.model.Board;
public class Server extends Thread
{
	ServerSocket sersock;
    Socket sock;
    BufferedReader keyRead;
    OutputStream ostream;
    PrintWriter pwrite;
    InputStream istream;
    BufferedReader receiveRead;
    BoardPanel boardPanel;
    Board board;
    private LinkedList<Integer> errors;
    boolean gotMsg = false;

	public Server(BoardPanel bP) throws Exception {
		boardPanel = bP;
		sersock = new ServerSocket(3000);
	    sock = sersock.accept( );
	    // reading from keyboard (keyRead object)
	    keyRead = new BufferedReader(new InputStreamReader(System.in));
        
	    ostream = sock.getOutputStream();
	    pwrite = new PrintWriter(ostream, true);
	    istream = sock.getInputStream();
	    receiveRead = new BufferedReader(new InputStreamReader(istream));

	}

	public void sendMessage(String[] move) throws IOException {
		   for(int i = 0; i<move.length; i++) {
			   pwrite.println(move[i]);
			   pwrite.flush();
		   }       
	   }
	
   public void sendBoard(int[][] board) throws IOException {
	   pwrite.println(board.length);
	   pwrite.flush();
	   for(int i = 0; i<board.length; i++) {
		   for(int j = 0; j<board.length; j++) {
			   pwrite.println(board[i][j]);
		   	   pwrite.flush();
		   }
	   }       
   }
   public void setBoard(Board b) {
	   board = b;
   }
   public int[][] getBoard() throws Exception{
	   int[][] board;
	   String received = "";
	   while(!gotMsg)
	   { 
		   if((received = receiveRead.readLine()) != null) //receive from server
		   {
			   gotMsg = true;
	       }
	    }
	   gotMsg = false;
	   int size = Integer.parseInt(received);
	   board = new int[size][size];
	   int i = 0;
	   int j = 0;
	   while(i!=size) {
		   if((received = receiveRead.readLine()) != null) //receive from server
		   {
			   board[i][j] = Integer.parseInt(received);
			   if(j==(size-1))
				   i++;
			   j = (j+1)%size;
	       }   
	   }
	   return board;
   }
   
   public void run() {
	   String received = "";
	   int i = 0;
	   int[] move = new int[3];
	   System.out.println("Set the bar");
	   while(true) {
		   try {
			if((received = receiveRead.readLine()) != null) //receive from server
			   {
					if(received.equals("solve")) {
						board.solve();
						boardPanel.repaint();
					}
					else if(received.equals("check")) {
						errors = board.check();
			    		boardPanel.setErrors(errors);
			    		boardPanel.repaint();
					}
					else if(received.equals("new")) {
						boardPanel.removeAll();
						int[][] bI = getBoard();
			    		int[][] solution = getBoard();
			    		initializeBoard(bI, solution);
			    		boardPanel.setBoard(board);
			    		boardPanel.repaint();
					}
					else {
				   move[i] = Integer.parseInt(received);
				   i++;
				   if(i == 3) {
					   if(board.boardInputs[move[0]][move[1]] != move[2]) {
						   board.boardInputs[move[0]][move[1]] = move[2];
					   }
					   board.playerMove();
					   i=0;
					   System.out.println("WE ARE THE BAR");
					   boardPanel.repaint();
				   }
					}
			   }
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
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
}		
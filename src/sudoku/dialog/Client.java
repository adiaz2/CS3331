package sudoku.dialog;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.awt.*;
import javax.swing.*;

import sudoku.model.Board;
public class Client extends Thread
{
	Socket sock;
    // reading from keyboard (keyRead object)
    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
    // sending to client (pwrite object)
    OutputStream ostream;
    PrintWriter pwrite;
    boolean gotMsg = false;
    boolean newGameDeclined = false;
    boolean isClient;
    private LinkedList<Integer> errors;

    // receiving from server ( receiveRead  object)
    InputStream istream;
    BufferedReader receiveRead;
    Board board;
    BoardPanel boardPanel;
    
	public Client(BoardPanel bP, String host_ip, int port) throws Exception, IOException {
		boardPanel = bP;
		sock = new Socket(host_ip, port);
	    // reading from keyboard (keyRead object)
		isClient = true;
	    keyRead = new BufferedReader(new InputStreamReader(System.in));
	    // sending to client (pwrite object)
	    ostream = sock.getOutputStream();
	    pwrite = new PrintWriter(ostream, true);

        // receiving from server ( receiveRead  object)
        InputStream istream = sock.getInputStream();
        receiveRead = new BufferedReader(new InputStreamReader(istream));
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
	   
	   public void getMove(Board board) throws IOException {
		   int i = 0;
		   int[] move = new int[3];
		   String received = "";
		   while(i<3) {
			   if((received = receiveRead.readLine()) != null) //receive from server
			   {
				   move[i] = Integer.parseInt(received);
				   i++;
		       } 
		   }
		   board.boardInputs[move[0]][move[1]] = move[2];
		   
	   }
	   public void sendMessage(String[] move) throws IOException {
		   for(int i = 0; i<move.length; i++) {
			   pwrite.println(move[i]);
			   pwrite.flush();
		   }       
	   }
	   public void setBoard(Board b) {
		   board = b;
	   }
	   
	   public void run() {
		   String received = "";
		   int i = 0;
		   int[] move = new int[3];
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
					}else if(received.equals("new")) {
						int reply = JOptionPane.showConfirmDialog(null, "Request for new game, accept?");
		    			//if user says no, don't do anything else
		    			if (reply == JOptionPane.NO_OPTION || reply == JOptionPane.CANCEL_OPTION)
		    		    {
		    				isClient = true;
		    				return;
		    		    }
						boardPanel.removeAll();
						int[][] bI = getBoard();
			    		int[][] solution = getBoard();
			    	 	initializeBoard(bI, solution);
			    		boardPanel.setBoard(board);
			    		boardPanel.repaint();
					}
					/*
					else if(received.equals("declined new game"))
					{
						newGameDeclined = true;
						return;
					}
					else if(received.equals("accepted new game"))
					{
						newGameDeclined = false;
						return;
					}
					*/
					else {
					   move[i] = Integer.parseInt(received);
					   i++;
					   if(i == 3) {
						   board.boardInputs[move[0]][move[1]] = move[2];
						   board.playerMove();
						   i=0;
						   boardPanel.repaint();
					   }
					}
				   }
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				JOptionPane.showMessageDialog(null, "Error: connection reset","Error Message",JOptionPane.ERROR_MESSAGE);
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




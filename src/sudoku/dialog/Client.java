package sudoku.dialog;

import java.io.*;
import java.net.*;
import java.io.Serializable;

import sudoku.model.Board;
public class Client implements Serializable
{
	Socket sock;
    // reading from keyboard (keyRead object)
    BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
    // sending to client (pwrite object)
    OutputStream ostream;
    PrintWriter pwrite;
    boolean gotMsg = false;

    // receiving from server ( receiveRead  object)
    InputStream istream;
    BufferedReader receiveRead;
    
	public Client() throws Exception, IOException {
		sock = new Socket("localhost", 3000);
	    // reading from keyboard (keyRead object)
		
	    keyRead = new BufferedReader(new InputStreamReader(System.in));
	    // sending to client (pwrite object)
	    ostream = sock.getOutputStream();
	    pwrite = new PrintWriter(ostream, true);

        // receiving from server ( receiveRead  object)
        InputStream istream = sock.getInputStream();
        receiveRead = new BufferedReader(new InputStreamReader(istream));
        System.out.println("Start the chitchat, type and press Enter key");	   
	     }
	
	   public void sendMessage(Socket sock, String[] move) throws IOException {
		   OutputStream ostream = sock.getOutputStream();
		   PrintWriter pwrite = new PrintWriter(ostream, true);
		   for(int i = 0; i<move.length; i++) {
			   pwrite.println(move[i]);
		   }
	       pwrite.flush();
	   }
	   
	   public int[][] getBoard() throws Exception{

		   int[][] board;
		   String received = "";
		   while(!gotMsg)
		   { 
			   System.out.println("Hey");
			   if((received = receiveRead.readLine()) != null) //receive from server
			   {
				   System.out.println("Hen");
				   gotMsg = true;
		       }
		    }
		   System.out.println("Now");
		   int size = Integer.parseInt(received);
		   board = new int[size][size];
		   int i = 0;
		   int j = 0;
		   while(i!=(size-1) || j!=(size-1)) {
			   if((received = receiveRead.readLine()) != null) //receive from server
			   {
				   System.out.println(j + "   " + i);
				   board[i][j] = Integer.parseInt(received);
				   if(j==(size-1))
					   i = (i+1)%size;
				   j = (j+1)%size;
		       }   
		   }
		   System.out.println("forever");
		   return board;
	   }
}




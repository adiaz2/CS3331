package sudoku.dialog;


import java.io.*;
import java.net.*;

import sudoku.model.Board;
public class Server
{
	ServerSocket sersock;
    Socket sock;
    // reading from keyboard (keyRead object)
    BufferedReader keyRead;
    // sending to client (pwrite object)
    OutputStream ostream;
    PrintWriter pwrite;
    // receiving from server ( receiveRead  object)
    InputStream istream;
    BufferedReader receiveRead;

	public Server() throws Exception {
		sersock = new ServerSocket(3000);
	    sock = sersock.accept( );
	    // reading from keyboard (keyRead object)
	    keyRead = new BufferedReader(new InputStreamReader(System.in));
	    // sending to client (pwrite object)
        
	    ostream = sock.getOutputStream();
	    pwrite = new PrintWriter(ostream, true);
	    // receiving from server ( receiveRead  object)
	    istream = sock.getInputStream();
	    receiveRead = new BufferedReader(new InputStreamReader(istream));
	   // String receiveMessage, sendMessage;
//	    while(true) {
//	    	if((receiveMessage = receiveRead.readLine()) != null)
//	          {
//	             System.out.println(receiveMessage);
//	          }
//	          sendMessage = keyRead.readLine();
//	          pwrite.println(sendMessage);
//	          pwrite.flush();
//	    }
	}

//   public static void main(String[] args) throws Exception
//   {
//      System.out.println("Server  ready for chatting");  
//      String receiveMessage, sendMessage;
//      while(true)
//      {
//         
//      }
//   }
	public void sendMessage(String[] move) throws IOException {
		   for(int i = 0; i<move.length; i++) {
			   pwrite.println(move[i]);
			   pwrite.flush();
		   }       
	   }
	
   public void sendBoard(int[][] board) throws IOException {
//	   ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
//	   os.writeObject(board);
	   pwrite.println(board.length);
	   pwrite.flush();
	   for(int i = 0; i<board.length; i++) {
		   for(int j = 0; j<board.length; j++) {
			   pwrite.println(board[i][j]);
		   	   pwrite.flush();
		   	   System.out.println("Hello");
		   }
	   }       
   }
}		
package sudoku.dialog;

import java.io.*;
import java.net.*;
public class EchoClient {
   public static void main(String[] args) {
       String host = (args.length > 0 ? args[0] : "localhost");
       try {
         Socket socket = new Socket(host, 8000);
         BufferedReader in = new BufferedReader(new InputStreamReader(
              socket.getInputStream()));
         PrintWriter out = new PrintWriter(new OutputStreamWriter(
              socket.getOutputStream()));
         for (int i = 1; i <= 10; i++) {
        	   System.out.println("Sending: line " + i);
        	   System.out.println("line " + i);
        	   out.flush();
        	}
        	out.print("BYE");
        	out.flush();

        	// receive data from server
        	String str = null;
        	while ((str = in.readLine()) != null) {
        	   System.out.println(str);
        	}
         socket.close();
       } catch (Exception e) {
         e.printStackTrace();
       }
   }
}
package sudoku.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** 
 * An abstraction of a TCP/IP socket for sending and receiving 
 * Sudoku game messages. This class allows two players to communicate 
 * with each other through a socket and solve Sudoku puzzles together.
 * It is assumed that a socket connection is already established between 
 * the players.
 * 
 * <p>
 * This class supports a few different types of messages. 
 * Each message is one line of text, a sequence of characters
 * ended by the end-of-line character, and consists of a header and a body.
 * A message header identifies a message type and ends with a ":", e.g.,
 * "fill:". A message body contains the content of a message. If it 
 * contains more than one element, they are separated by a ",",
 * e.g., "1,2,3". There are seven different messages as defined below.
 * </p>
 * 
 * <ul>
 *     <li>join: -- request to join the peer's current game</li>
 *     <li>join_ack: n [,s,b] -- acknowledge a join request, where n (response)
 *         is either 0 (declined) or 1 (accepted), s is a board size, and b
 *         is a sequence of non-empty squares of a board, each encoded as:
 *         x,y,v,f (x, y: 0-based column/row indexes, v: contained value, 
 *         f: 1 if the value is given/fixed or 0 if filled by the user.
 *         The size (s) and board (b) are required only when n is 1.</li>
 *     <li>new: s,b -- request to start a new game, where s is a board size,
 *         and b is a board encoded in the same way as the join_ack message.</li>
 *     <li>new_ack: n -- ack new game request, where n (response) is
 *         either 0 (declined) or 1 (accepted).</li>
 *     <li>fill: x, y, v -- fill a square, where x and y are 0-based 
 *         column/row indexes of a square and v is a number.</li>
 *     <li>fill_ack: x, y, v -- acknowledge a fill message.</li>
 *     <li>quit: -- leaves a game by ending the connection.</li>
 * </ul>
 *
 *<p>
 * Two players communicate with each other as follows. 
 * One of the players (client) connects to the other (server) 
 * and requests to join the current game of the server; the player who 
 * initiates the connection must send a join message, 
 * as the other player will be waiting for it.
 * If the server accepts the request, it sends its puzzle (board) to the client. 
 * Now, both players can solve the shared puzzle by sending and receiving a series 
 * of fill and fill_ack messages. A player may quit a shared game or make a request
 * to play a new shared game by sending a new puzzle.
 * </p>
 *
 * 1. Joining a game (accepted).
 * <pre>
 *  Client        Server
 *    |------------>| join: -- request to join a game
 *    |<------------| join_ack:1,9,0,0,2,1,... -- accept the request
 *    |------------>| fill:3,4,2 -- client fill
 *    |<------------| fill_ack:3,4,2 -- server ack
 *    |<------------| fill:2,3,5 -- server fill
 *    |------------>| fill_ack:2,3,5 -- client ack
 *    ...
 * </pre>
 * 
 * 2. Joining a game (declined)
 * <pre>
 *  Client        Server
 *    |------------>| join: -- request to join a game
 *    |<------------| join_ack:0 -- decline the request (disconnected!)
 * </pre> 
 *
 * 3. Starting a new game (accepted)
 * <pre>
 *  Client        Server
 *    |------------>| join: -- request to join a game
 *    |<------------| join_ack:1,9,0,0,2,1,... -- accept the request
 *    ...
 *    |------------>| new: 9,1,1,2,1,... -- request for a new game
 *    |<------------| new_ack:1 -- accept the request
 *    |<------------| fill:3,3,5 -- server fill
 *    |------------>| fill_ack:3,3,5 -- client ack
 *    ...
 * </pre>
 * 
 * 4. Starting a new game (declined)
 * <pre>
 *  Client        Server
 *    |------------>| join: -- request to join a game
 *    |<------------| join_ack:1,9,0,0,2,1,... -- accept the request
 *    ...
 *    |------------>| new: 9,1,1,2,1,... -- request for a new game
 *    |<------------| new_ack:0 -- decline the request (disconnected!)
 * </pre>
 *
 * 5. Quitting a game
 * <pre>
 *  Client        Server
 *    |------------>| join: -- request to join a game
 *    |<------------| join_ack:1,9,0,0,2,1,... -- accept the request
 *    ...
 *    |------------>| quit: -- quit the game (disconnected!)
 * </pre>
 * 
 * <p>
 * To receive messages from the peer, register a {@link MessageListener}
 * and then call the {@link #receiveMessagesAsync()} method as shown below.
 * This method creates a new thread to receive messages asynchronously.
 * </p>
 * 
 * <pre>
 *  Socket socket = ...;
 *  NetworkAdapter network = new NetworkAdapter(socket);
 *  network.setMessageListener(new NetworkAdapter.MessageListener() {
 *      public void messageReceived(NetworkAdapter.MessageType type, int x, int y, int z, int[] others) {
 *        switch (type) {
 *          case JOIN: ... 
 *          case JOIN_ACK: ... // x (response), y (size), others (board)
 *          case NEW: ...      // x (size), others (board)
 *          case NEW_ACK: ...  // x (response)
 *          case FILL: ...     // x (x), y (y), z (number)
 *          case FILL_ACK: ... // x (x), y (y), z (number)
 *          case QUIT: ...
 *          ...
 *        }
 *      }
 *    });
 *
 *  // receive messages asynchronously
 *  network.receiveMessagesAsync();
 * </pre>

 * <p>
 * To send messages to the peer, call the <code>writeXXX</code> methods. 
 * These methods run asynchronously, and messages are sent
 * in the order they are received by the <code>writeXXX</code> methods.
 * </p>
 * 
 * <pre>
 *  network.writeJoin();
 *  network.writeFill(1,2,3);
 *  ...
 *  network.close();
 * </pre>
 *
 * @author cheon
 * @see MessageType
 * @see MessageListener
 */

public class NetworkAdapter {

    /** Different type of game messages. */
    public enum MessageType { 
        
        /** Quit the game. This message has the form "quit:". */
        QUIT ("quit:"), 
        
        /** Request to join an existing game. This message has the form "join:". */
        JOIN ("join:"), 
        
        /** 
         * Acknowledgement of a join request. This message has the form 
         * "join_ack: n, [,size,board]", where n (response) is either 0 (declined) 
         * or 1 (accepted), size is the board size, and board is a sequence of 
         * non-empty squares of the board, each encoded as: x,y,v,f (where 
         * x, y: 0-based column/row indexes, v: number, f: 1 if the value
         * is given/fixed or 0 if entered by the user.
         */
        JOIN_ACK ("join_ack:"), 

        /** 
         * Request to play a new game. This message has the form "new: size,board",
         * size is the board size and board is a sequence of non-empty squares 
         * of the board, each encoded as: x,y,v,f (where x, y: 0-based column/row indexes, 
         * v: number, f: 1 if the value is given/fixed or 0 if entered by the user.
         */
        NEW ("new:"), 
        
        /** 
         * Acknowledgement of a new game request. This message has the form "new_ack: n",
         * where n (response) is either 0 (declined) or 1 (accepted).
         */
        NEW_ACK ("new_ack:"), 
        
        /** 
         * Request to fill a number in the board. This message has the form "fill: x,y,v",
         * where x and y are 0-based column/row indexes of a square and v is a number to fill
         * in the square.
         */
        FILL ("fill:"), 
        
        /** 
         * Acknowledgement of a fill message. This message has the form "fill_ack: x,y,v",
         * where x and y are 0-based column/row indexes of a square and v is a number to fill
         * in the square.
         */
        FILL_ACK ("fill_ack:"), 
        
        /** Connection closed. To notify when the socket is closed. */
        CLOSE (null), 
        
        /** Unknown message received. */
        UNKNOWN (null);
        
        /** Message header. */
        private final String header;
        
        MessageType(String header) {
            this.header = header;
        }

    };

    /** Called when a message is received. */
    public interface MessageListener {

        /** 
         * To be called when a message is received. 
         * The type of the received message along with optional content
         * (x, y, z and others) are provided as arguments.
         * 
         * @param type Type of the message received
         * @param x First argument
         * @param y Second argument
         * @param z Third argument
         * @param others Additional aruguments
         */
        void messageReceived(MessageType type, int x, int y, int z, int[] others);
    }

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    /** To be notified when a message is received. */
    private MessageListener listener;
    
    /** Asynchronous message writer. */
    private MessageWriter messageWriter;
    
    /** Reader connected to the peer to read messages from it. */
    private BufferedReader in;
    
    /** Writer connected to the peer to write messages to it. */
    private PrintWriter out;
    
    /** If not null, log all messages sent and received. */
    private PrintStream logger;

    /** Associated socket to communicate with the peer. */
    private Socket socket;
    
    /** 
     * Create a new network adapter to read messages from and to write
     * messages to the given socket.
     * 
     * @param socket Socket to read and write messages.
     */
    public NetworkAdapter(Socket socket) {
        this(socket, null);
    }
    
    /** 
     * Create a new network adapter. Messages are to be read from and 
     * written to the given socket. All incoming and outgoing 
     * messages will be logged on the given logger.
     * 
     * @param socket Socket to read and write messages.
     * @param logger Log all incoming and outgoing messages.
     */
    public NetworkAdapter(Socket socket, PrintStream logger) {
    	this.socket = socket;
        this.logger = logger;
        messageWriter = new MessageWriter();
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    /** Return the associated socket.
     * @return Socket associated with this adapter.
     */
    public Socket socket() {
    	return socket;
    }
    
    /** Close the IO streams of this adapter. Note that the socket
     * to which the streams are attached is not closed by
     * this method. */
    public void close() {
        try {
            // close "out" first to break the circular dependency
            // between peers.
            out.close();  
            in.close();
            messageWriter.stop();
        } catch (Exception e) {
        }
    }

    /**
     * Register the given messageListener to be notified when a message
     * is received.
     * 
     * @param listener To be notified when a message is received.
     *
     * @see MessageListener
     * @see #receiveMessages()
     * @see #receiveMessagesAsync()
     */
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    /**
     * Start accepting messages from this network adapter and
     * notifying them to the registered listener. This method blocks
     * the caller. To receive messages synchronously, use the
     * {@link #receiveMessagesAsync()} method that creates a new
     * background thread.
     *
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessagesAsync()
     */
    public void receiveMessages() {
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                if (logger != null) {
                    logger.format(" < %s\n", line);
                }
                parseMessage(line);
            }
        } catch (IOException e) {
        }
        notifyMessage(MessageType.CLOSE);
    }
    
    /**
     * Start accepting messages asynchronously from this network
     * adapter and notifying them to the registered listener.
     * This method doesn't block the caller. Instead, a new
     * background thread is created to read incoming messages.
     * To receive messages synchronously, use the
     * {@link #receiveMessages()} method.
     *
     * @see #setMessageListener(MessageListener)
     * @see #receiveMessages()
     */
    public void receiveMessagesAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessages();
            }
        }).start();
    }

    /** Parse the given message and notify to the registered listener. */
    private void parseMessage(String msg) {
        if (msg.startsWith(MessageType.QUIT.header)) {
                notifyMessage(MessageType.QUIT);
        } else if (msg.startsWith(MessageType.JOIN_ACK.header)) {
            parseJoinAckMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.JOIN.header)) {
            notifyMessage(MessageType.JOIN);
        } else if (msg.startsWith(MessageType.NEW_ACK.header)) {
        	parseNewAckMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.NEW.header)) {
        	parseNewMessage(msgBody(msg));
        } else if (msg.startsWith(MessageType.FILL_ACK.header)) {
            parseFillMessage(MessageType.FILL_ACK, msgBody(msg));
        } else if (msg.startsWith(MessageType.FILL.header)){
            parseFillMessage(MessageType.FILL, msgBody(msg));
        } else {
            notifyMessage(MessageType.UNKNOWN);
        }
    }

    /** Parse and return the body of the given message. */
    private String msgBody(String msg) {
        int i = msg.indexOf(':');
        if (i > -1) {
            msg = msg.substring(i + 1);
        }
        return msg;
    }

    /** Parse and notify the given play_ack message body. */
    private void parseJoinAckMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: join_ack 0
        	int response = parseInt(parts[0].trim());
        	if (response == 0) {
        		notifyMessage(MessageType.JOIN_ACK, 0);
        		return;
        	}
        	if (response == 1 && parts.length >= 2) {
        		// message: join_ack 1 size squares
        		int size = parseInt(parts[1].trim());
        		if (size > 0) {
        			int[] others = new int[parts.length - 2];
        			for (int i = 2; i < parts.length; i++) {
        				others[i-2] = parseInt(parts[i]);
        			}
        			notifyMessage(MessageType.JOIN_ACK, 1, size, others);
        			return;
        		}
        	}
        }
        notifyMessage(MessageType.UNKNOWN);
    }
    
    /** Parse and notify the given new_ack message body. */
    private void parseNewAckMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: new_ack response
        	int response = parseInt(parts[0].trim());
        	response = response == 0 ? 0 : 1;
        	notifyMessage(MessageType.NEW_ACK, response);
        	return;
        }
        notifyMessage(MessageType.UNKNOWN);
    } 
    
    /** Parse and notify the given play_ack message body. */
    private void parseNewMessage(String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 1) {
        	// message: new size squares
        	int size = parseInt(parts[0].trim());
        	if (size > 0) {
        		int[] others = new int[parts.length - 1];
        		for (int i = 1; i < parts.length; i++) {
        			others[i-1] = parseInt(parts[i]);
        		}
        		notifyMessage(MessageType.NEW, size, others);
        		return;
        	}
        }
        notifyMessage(MessageType.UNKNOWN);
    }    

    /** 
     * Parse the given string as an int; return -1 if the input
     * is not well-formed. 
     */
    private int parseInt(String txt) {
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /** Parse and notify the given move or move_ack message. */
    private void parseFillMessage(MessageType type, String msgBody) {
        String[] parts = msgBody.split(",");
        if (parts.length >= 3) {
            int x = parseInt(parts[0].trim());
            int y = parseInt(parts[1].trim());
            int v = parseInt(parts[2].trim());
            notifyMessage(type, x, y, v);
        } else {
            notifyMessage(MessageType.UNKNOWN);
        }
    }

    /** Write the given message asynchronously. */
    private void writeMsg(String msg) {
        messageWriter.write(msg);
    }
    
    /**
     * Write a join message asynchronously.
     *
     * @see #writeJoinAck()
     * @see #writeJoinAck(int, int...)
     */
    public void writeJoin() {
        writeMsg(MessageType.JOIN.header);
    }

    /**
     * Write a "declined" join_ack message asynchronously.
     *
     * @see #writeJoin()
     */
    public void writeJoinAck() {
        writeMsg(MessageType.JOIN_ACK.header + "0");
    }
    
    /**
     * Write an "accepted" join_ack message asynchronously. 
     * 
     * @param size Size of the board
     * @param squares Non-empty squares of the board. Each square is represented
     *   as a tuple of (x, y, v, f), where x and y are 0-based column/row indexes,
     *   v is a non-zero number, and f is a flag indicating whether the number
     *   is given (1) or entered by the user (0).
     *
     * @see #writeJoin()
     */
    public void writeJoinAck(int size, int... squares) {
    	StringBuilder builder = new StringBuilder(MessageType.JOIN_ACK.header);
    	builder.append("1,"); 
    	builder.append(size);
    	for (int v: squares) {
    		builder.append(",");
    		builder.append(v);
    	}
        writeMsg(builder.toString());
    }
    
    /**
     * Write a new game message asynchronously.
     * 
     * @param size Size of the board
     * @param squares Non-empty squares of the board. Each square is represented
     *   as a tuple of (x, y, v, f), where x and y are 0-based column/row indexes,
     *   v is a non-zero number, and f is a flag indicating whether the number
     *   is given (1) or entered by the user (0).
     *
     * @see #writeNewAck(boolean)
     */
    public void writeNew(int size, int... squares) {
    	StringBuilder builder = new StringBuilder(MessageType.NEW.header);
    	builder.append(size);
    	for (int v: squares) {
    		builder.append(",");
    		builder.append(v);
    	}
        writeMsg(builder.toString());    	
    }
    
    /**
     * Write an new_ack message asynchronously. 
     * 
     * @param response True for accepted; false for declined.
     *
     * @see #writeNew(int, int...)
     */
    public void writeNewAck(boolean response) {
        writeMsg(MessageType.NEW_ACK.header + toInt(response));
    }
    
    /** Convert the given boolean flag to an int. */
    private int toInt(boolean flag) {
        return flag ? 1: 0;
    }
    
    /**
     * Write a fill message asynchronously. 
     * 
     * @param x 0-based column index of the square
     * @param y 0-based row index of the square
     * @param number Filled-in number
     *
     * @see #writeFillAck(int, int, int)
     */
    public void writeFill(int x, int y, int number) {
        writeMsg(String.format("%s%s,%s,%s", MessageType.FILL.header, x, y, number));
    }

    /**
     * Write a fill_ack message asynchronously.
     *
     * @param x 0-based column index of the square
     * @param y 0-based row index of the square
     * @param number Filled-in number
     * 
     * @see #writeFill(int, int, int)
     */
    public void writeFillAck(int x, int y, int number) {
        writeMsg(String.format("%s%s,%s,%s", MessageType.FILL_ACK.header, x, y, number));
    }
    
    /** Write a quit (gg) message (to quit the game) asynchronously. */
    public void writeQuit() {
        writeMsg(MessageType.QUIT.header);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type) {
        listener.messageReceived(type, 0, 0, 0, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x) {
        listener.messageReceived(type, x, 0, 0, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x, int[] others) {
        listener.messageReceived(type, x, 0, 0, others);
    }
    
    /** Notify the listener the receipt of the given message. */
    private void notifyMessage(MessageType type, int x, int y, int v) {
        listener.messageReceived(type, x, y, v, EMPTY_INT_ARRAY);
    }
    
    /** Notify the listener the receipt of the given message type. */
    private void notifyMessage(MessageType type, int x, int y, int[] others) {
        listener.messageReceived(type, x, y, 0, others);
    }
  
    /** 
     * Write messages asynchronously. This class uses a single 
     * background thread to write messages asynchronously in a FIFO
     * fashion. To stop the background thread, call the stop() method.
     */
    private class MessageWriter {
        
        /** Background thread to write messages asynchronously. */
        private Thread writerThread;
        
        /** Store messages to be written asynchronously. */
        private BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        /** Write the given message asynchronously on a new thread. */
        public void write(final String msg) {
            if (writerThread == null) {
                writerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                String m = messages.take();
                                out.println(m);
                                out.flush();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                });
                writerThread.start();
            }

            synchronized (messages) {
                try {
                    messages.put(msg);
                    if (logger != null) {
                        logger.format(" > %s\n", msg);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        
        /** Stop this message writer. */
        public void stop() {
            if (writerThread != null) {
                writerThread.interrupt();
            }
        }
    }
}
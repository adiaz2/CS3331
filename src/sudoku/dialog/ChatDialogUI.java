package sudoku.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

/** A simple chat dialog. */
@SuppressWarnings("serial")
    public class ChatDialogUI extends JDialog {
    
	/** Default dimension of chat dialogs. */
	private final static Dimension DIMENSION = new Dimension(400, 400);
    
	private JButton connectButton;
	private JButton sendButton;
	private JTextField serverEdit;
	private JTextField portEdit;
	private JTextArea msgDisplay;
	private JTextField msgEdit;
	public Socket clientSocket;
    
	/** Create a main dialog. */
	public ChatDialogUI() {
	    this(DIMENSION);
	}
	
	public void displayText(String str) {
		msgDisplay.setText(str + "\n" + msgDisplay.getText());
	}
    
	/** Create a main dialog of the given dimension. */
	public ChatDialogUI(Dimension dim) {
	    super((JFrame) null, "Sudoku Server");
	    configureGui();
	    setSize(dim);
	    //setResizable(false);
	    connectButton.addActionListener(e -> {
			try {
				connectClicked(e);
			} catch (IOException f) {
				// TODO Auto-generated catch block
				f.printStackTrace();
			}
		});
	    sendButton.addActionListener(this::sendClicked);
	    setLocationRelativeTo(null);
	}
    
	/** Configure UI of this dialog. */
	private void configureGui() {
	    JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
	    connectButton = new JButton("Connect");
	    connectButton.setFocusPainted(false);
	    serverEdit = new JTextField("localhost", 18);
	    portEdit = new JTextField("8000", 4);
	    connectPanel.add(connectButton);
	    connectPanel.add(serverEdit);
	    connectPanel.add(portEdit);
        
	    msgDisplay = new JTextArea(10, 30);
	    msgDisplay.setEditable(false);
	    DefaultCaret caret = (DefaultCaret)msgDisplay.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // autoscroll
	    JScrollPane msgScrollPane = new JScrollPane(msgDisplay);

	    JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
	    msgEdit = new JTextField("Enter a message.", 27);
	    sendButton = new JButton("Send");
	    sendButton.setFocusPainted(false);
	    sendPanel.add(msgEdit);
	    sendPanel.add(sendButton);
        
	    setLayout(new BorderLayout());
	    add(connectPanel, BorderLayout.NORTH);
	    add(msgScrollPane, BorderLayout.CENTER);
	    add(sendPanel, BorderLayout.SOUTH);
	}
    
	/** Callback to be called when the connect button is clicked. 
	 * @throws IOException */
	private void connectClicked(ActionEvent event) throws IOException {
		String str = "";
		try {
		ServerSocket server = new ServerSocket(8000);
		Socket s = server.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	    PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
	    
		while ((str = in.readLine()) != null) {  
			System.out.println("Hello");
			displayText("Hello");
		    
		    
		    out.print("Welcome to the Java EchoServer!");
		    out.println("Enter BYE to exit.");
		    out.flush();
		    
		    while ((str = in.readLine()) != null) {
			out.println("Received: " + str);
			displayText("Received: " + str);
			out.println("Hello: " + str);
			out.flush();
			if (str.trim().equals("BYE")) {
			    break;
				}
		    }
		}
	    } catch (ConnectException e) {
			msgDisplay.setText("Unable to Connect to server: " + serverEdit.getText() + " Port: " + portEdit.getText() + "\n" + msgDisplay.getText());
			e.printStackTrace();
		} catch (UnknownHostException e) {
			msgDisplay.setText("Unknown Host" + "\n" + msgDisplay.getText());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	 /** Callback to be called when the send button is clicked. */
	 private void sendClicked(ActionEvent event) {
	     
		 try {
			 PrintStream out = new PrintStream(clientSocket.getOutputStream());
		     /* communicate with server */
		     Supplier<String> userInput = () -> msgEdit.getText();
			 
			 Stream.generate(userInput)
			.map(s -> {
			    out.println(s);
			    msgDisplay.setText("Server response: " + s);
			    if(!"quit".equalsIgnoreCase(s)){
			        msgEdit.getText();
			    }
			    return s;
			})
			.allMatch(s -> !"quit".equalsIgnoreCase(s));
			 
		 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }

	/** Show the given message in a dialog. */
	private void warn(String msg) {
	    JOptionPane.showMessageDialog(this, msg, "Sudoku Server", JOptionPane.PLAIN_MESSAGE);        
	}
    
}
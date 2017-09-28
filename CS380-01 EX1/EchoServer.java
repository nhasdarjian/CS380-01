import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    String address = socket.getInetAddress().getHostAddress();
                    System.out.printf("Client connected: %s%n", address);
                    
                    InputStream is = socket.getInputStream();
            		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            		BufferedReader br = new BufferedReader(isr); 			// read in from client
            		
                    OutputStream os = socket.getOutputStream();
                    PrintStream out = new PrintStream(os, true, "UTF-8"); 	// output to client
                    
                    out.printf("Hi %s, thanks for connecting!%n", address); // send confirmation
                    
                    String i = "";
            		while (!i.equals("exit"))
                    {
                    	i = br.readLine();									// read input from client
                    	System.out.println("Client sent \"" + i + "\"");	// shows text in server
                    	out.println(i);										// sends result to client
                    }
                    System.out.printf("Client disconnected: %s%n", address);
                }
            }
        }
    }
}

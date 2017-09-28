import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 22222)) {
            
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr); 			// read in from server
            
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8"); 	// output to server
            
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(input); 			// read user input
            
            System.out.println(br.readLine()); 						// print connection confirmation
            
            String i = "";
            while (!i.equals("exit"))
            {
            	System.out.print("Client> ");
            	i = in.readLine(); 									// read input from user
            	out.println(i); 									// send user input to server
            	System.out.println("Server> " + br.readLine()); 	//print result from server
           	}
           	
            
        }
    }
}


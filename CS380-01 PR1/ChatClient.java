import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public final class ChatClient {

	public static void kill(Socket s)
	{
		System.err.println("Disconnected from server.");
		try { s.close(); } catch (Exception e) {} 					// close socket, free username
		System.exit(0);												// kill threads
	}

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182", 38001)) // connect to server
		{ 
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr); 			// read in from server
            
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8"); 	// output to server
            
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(input); 			// read user input
            
            Runnable serverIn = () ->								// handle server input
            {
            	String i = "";
            	while (true)
            	{
            		try
            		{
            			i = br.readLine();							// read server input
            			
            			if (!i.equals(null)) 
            			{
            				System.out.println(i);					// print if not null
            				switch (i)								// special cases
            				{
            					case "Name in use.":			
            					case "Connection idle for 1 minute, closing connection.":
            						kill(socket);					// close application
								default: break;						// if regular message do nothing
            				}
            			} else kill(socket);						// input is null, disconnected
					} catch (Exception e) {}
            	}
            };
            
            new Thread(serverIn).start();							// start handling input
            
			System.out.print("Enter a username: ");
            String i = "";
			while (!i.equals("exit"))
            {
            	i = in.readLine(); 									// read input from user
            	out.println(i); 									// send user input to server
           	}
			
           	kill(socket);
			
        } catch (Exception e) { System.out.println("Error connecting to server."); }
    }
}


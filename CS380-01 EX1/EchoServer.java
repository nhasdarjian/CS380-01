import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.EventQueue;

public final class EchoServer {

	public EchoServer()
	{
		try (ServerSocket serverSocket = new ServerSocket(22222))
        {
            while (true)
            {
                try (Socket socket = serverSocket.accept()) {
                    new Thread(new Connection(socket)).start();
                }
                catch (Exception e) { System.out.println(e); }
            }
        }
        catch (Exception e) { System.out.println(e); }
	}
	
	private class Connection implements Runnable
	{
		private Socket s;
		public Connection(Socket socket)
		{
			this.s = socket;
			System.out.println("Socket closed in constructor: " + s.isClosed());
		}
		public void run()
		{
			try {
				System.out.println("Socket closed in runnable: " + s.isClosed());
				String address = s.getInetAddress().getHostAddress();
            	System.out.printf("Client connected: %s%n", address);
            	        
            	InputStream is = s.getInputStream();
       			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      			BufferedReader br = new BufferedReader(isr); 			// read in from client
          			
            	OutputStream os = s.getOutputStream();
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
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
    public static void main(String[] args) throws Exception
    {
        EventQueue.invokeLater(() -> new EchoServer());
    }
}

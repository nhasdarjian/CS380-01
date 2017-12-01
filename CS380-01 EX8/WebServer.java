/*
*	Nathan Asdarjian
*	Alfredo Ceballos
*	CS380 - Computer Networks
*	Exercise 8
*	Prof. Nima Davarpanah
*/

import java.io.*;
import java.net.*;

public class WebServer
{
	private class Client extends Thread
	{
		Socket socket;
		String[] in;
		String temp;
		BufferedReader br, fbr;
		PrintWriter pw;
		
		public Client(Socket s)
		{
			this.socket = s;
			try
			{
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				pw = new PrintWriter(socket.getOutputStream(), true);
			}
			catch (IOException e)
			{
				System.out.println(e);
			}
		}
		
		public void run()
		{
			try
			{
				while (!br.ready())
				{
					try
					{
						this.sleep(500);
					}
					catch (InterruptedException e) {}
				}
				
				temp = br.readLine();
				System.out.println(temp);
				in = temp.split("\\s");
				
				while (br.ready()) System.out.println(br.readLine());
				if (in[0].equals("GET"))
				{
					if (in[1].equals("/")) in[1] = "/index.html";
					File f = new File("www" + in[1]);
					
					if (f.exists())
					{
						String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: " + f.length() + "\r\n";
						pw.println(response);
						System.out.print(response);
					} 
					else
					{
						f = new File("www/error.html");
						String response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: " + f.length() + "\r\n";
						pw.println(response);
						System.out.print(response);
					}
					
					BufferedReader fbr = new BufferedReader(new FileReader(f));
					while ((temp = fbr.readLine()) != null)
					{
						pw.println(temp);
						System.out.print(temp + "\r\n");
					}
					
					/*
					else
					{
						String response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length: 126\r\n";
						pw.println(response);
						System.out.print(response);
						response = "<html>\r\n<head>\r\n<title>Not Found</title>\r\n</head>\r\n<body>Sorry, the object you requested was not found.</body>\r\n</html>";
						pw.println(response);
						System.out.println(response);
					}
					*/
				}
			}
			catch (IOException e)
			{
				System.out.println(e);
			}
		}
	}
	
	public WebServer()
	{
		try
		{
			ServerSocket ss = new ServerSocket(8080);
			while (true)
			{
				Client r = new Client(ss.accept());
				r.start();
			}
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}
	
	public static void main(String[] args)
	{	
		new WebServer();
	}
}


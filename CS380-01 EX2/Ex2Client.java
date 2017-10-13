import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;

public class Ex2Client
{ 
	public static void main(String[] args)
	{
		try (Socket socket = new Socket("18.221.102.182", 38102))
		{
			System.out.println("CS380-01 EX2\nNathan Asdarjian\n\nConnected to server.\n");
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            OutputStream os = socket.getOutputStream();
			
			byte temp = 0;					// server input temp
			byte[] b = new byte[100];	// stores server inputs
			int count = 0; 				// helps position byte array 
			
			
			System.out.println("Received bytes: ");
			for (int i = 0; i < 200; i++)	// 200 hex values read in (100 bytes)
			{
				temp = (byte) is.read();	// read in a server value
				
				if (i % 2  == 0) b[count] = (byte) (temp << 4); // bit shift 4 times if "first" value
				else											// if "second" value,
				{
					b[count] += temp;							// add new byte to last position in b
					System.out.printf("0x%02X ", b[count]);		// format input as hex, print to screen
					count++;									// move to new position in b
				}
				if (i % 20 == 19) System.out.println();			// 10 bytes to a line		
			}
			
			CRC32 crc = new CRC32();							// create new CRC32 instance
			crc.update(b);										// generate a CRC value for the byte array
			Long l = crc.getValue();							// store in variable l
			System.out.println("\nGenerated CRC32: " + Long.toHexString(l).toUpperCase()); // print to screen
			
			byte b1 = (byte) ((l & 0xff000000) >> 24);			// assign every 8 bits to a byte
			byte b2 = (byte) ((l & 0x00ff0000) >> 16);			
			byte b3 = (byte) ((l & 0x0000ff00) >> 8);			
			byte b4 = (byte)  (l & 0x000000ff);					
			
			os.write( b1 );										// send each byte back to server
			os.write( b2 );
			os.write( b3 );
			os.write( b4 );
			
			temp = (byte) is.read();							// read the result from the server
			System.out.print("CRC32 compare: ");	
			if (temp != 0) System.out.println("Success" + " (" + temp + ")\n"); // if result != 0, success
			else System.out.println("Failure");
			
			socket.close();										// close connection with server
			System.out.println("Disconnected from server.");
		} 
		catch (Exception e)
		{
			System.out.println(e);								// used for debugging
		}
	}
}
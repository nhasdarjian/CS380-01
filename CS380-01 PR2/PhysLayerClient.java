import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;

public class PhysLayerClient
{ 
	public static byte[] decode4B5B(String s)			// decodes 5B to 4B
	{
		byte[] b = new byte[s.length() / 5];			// storage for result
		for (int k = 0; k < b.length; k++)
		{
			switch(s.substring(k * 5, (k + 1) * 5))		// splits into chunks of 5
			{
				case "11110":
					b[k] = 0;
					break;
				case "01001":
					b[k] = 1;
					break;
				case "10100":
					b[k] = 2;
					break;
				case "10101":
					b[k] = 3;
					break;
					
				case "01010":
					b[k] = 4;
					break;
				case "01011":
					b[k] = 5;
					break;
				case "01110":
					b[k] = 6;
					break;
				case "01111":
					b[k] = 7;
					break;
						
				case "10010":
					b[k] = 8;
					break;
				case "10011":
					b[k] = 9;
					break;
				case "10110":
					b[k] = 10;
					break;
				case "10111":
					b[k] = 11;
					break;
				
				case "11010":
					b[k] = 12;
					break;
				case "11011":
					b[k] = 13;
					break;
				case "11100":
					b[k] = 14;
					break;
				case "11101":
					b[k] = 15;
					break;
					
				default:
					System.out.println("Invalid sequence!");
					break;					
			}				// conversion table applied
		}
		return b;
	}

	public static String decodeNRZI(String s)				// decodes NRZI
	{
		String t = "";
		for (int i = 0; i < s.length(); i++)
		{
			if (i != 0)										// if not first element
			{
				if (s.charAt(i) != s.charAt(i-1)) t += "1"; // if changed, then 1
				else t += "0";								// no change, 0
			
			}
			else t += s.charAt(0);							// first element is the same
		}
		return t;
	}
	
	public static void main(String[] args)
	{
		try (Socket socket = new Socket("18.221.102.182", 38002))
		{
			System.out.println("CS380-01 EX2\nNathan Asdarjian\n\nConnected to server.\n");
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            OutputStream os = socket.getOutputStream();
			
			byte b = 0;						// temp var for storing inputs
			float baseline = 0;				// baseline for comparing inputs
			byte[] data = new byte[64];		// stores server inputs
			byte[] output = new byte[32];	// stores result
			String serverInput = "";		// stores bytes in String form (easier to split)
			
			for (int i = 0; i < 64; i++)						// 64 bytes read in
			{
				b = (byte) is.read();							// read from server
				baseline += (b & 0xff);							// sum all of the unsigned inputs
			}
			baseline /= 64;										// get average of inputs
			System.out.println("Baseline: " + baseline + "\n");
			
			for (int j = 0; j < 320; j++)						// 320 bytes read in
			{
				b = (byte) is.read();
				if ((b & 0xff) < baseline) serverInput += "0";	// byte converted to high/low
				else serverInput += "1";
			}
			
			serverInput = decodeNRZI(serverInput);				// undo NRZI
			data = decode4B5B(serverInput);						// get 4b representation
			
			for (int l = 0; l < 32; l++)
			{
				output[l] = (byte) (data[l * 2] << 4);			// concatenate 4b into 1B
				output[l] += data[(l * 2) + 1];
			}
			
			System.out.println("Decoded bytes read in from server:");
			for (int m = 0; m < 32; m++)
			{
				System.out.printf("0x%02X ", (output[m] & 0xff));
				if (m % 4 == 3) System.out.println();			// print data (with formatting)
			}
			
			os.write(output);
			
			System.out.print("\nDecoding status: ");
			if (is.read() == 1) System.out.println("Success!");	// if server -> 1, then good
			else System.out.println("Failure");					// else failure
			
			is.close();											// close streams
			os.close();
			socket.close();										// close connection with server
			System.out.println("\nDisconnected from server.");
		} 
		catch (Exception e)
		{
			System.out.println(e);								// used for debugging
		}
	}
}
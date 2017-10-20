import java.io.*;
import java.net.Socket;

public class Ex3Client
{
	public static short checksum(byte[] bytes)
	{
		int b = (bytes.length + (bytes.length % 2)) / 2;	// pairs of bytes
		short s;											// temp short
		int sum = 0;										// 32 bit storage space
		
		for (short i = 0; i < b; i++) {						// sum pairs of bytes
			
			s = (short) ((bytes[i * 2] & 0xFF) << 8);		// align bytes in short
			if ((i * 2) + 1 < bytes.length) s += (bytes[(i * 2) + 1] & 0xFF);
			sum += (s & 0xFFFF);							// add new short to sum
			if ((sum & 0xFFFF0000) > 0)						// if overflow,
			{
				sum &= 0x0000FFFF;							// drop first 16 bits
				sum++;										// wrap around overflow
			}
		}
		
		s = (short) ~(sum & 0x0000FFFF);					// one's complement
		return s;											// return checksum
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("CS380-01 EX3\nNathan Asdarjian\n");
		
		try (Socket socket = new Socket("18.221.102.182", 38103))
		{
			BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
			OutputStream os = socket.getOutputStream();
			
			byte b = (byte) is.read();						// read byte from server
			byte[] bytes = new byte[(b & 0xFF)];			// make array of bytes size b 
			System.out.println("Reading " + (b & 0xFF) + " bytes...");
			
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) is.read();				// read byte, save to array
				System.out.printf("%02X",(bytes[i] & 0xFF));	
				if (i % 4 == 3) System.out.print(" ");
				if (i % 16 == 15) System.out.println();		// display formatting
			}
			
			System.out.println('\n');						// display formatting
			
			short s = checksum(bytes);						// checksum
			System.out.printf("Checksum: 0x%04X\n", s);
			byte u = (byte) ((s & 0xFF00) >> 8);			// get upper byte
			byte l = (byte) ((s & 0x00FF));					// get lower byte
			
			os.write(u);									// send bytes
			os.write(l);
			
			b = (byte) is.read();							// read server response
			System.out.print("Checksum status: ");
			if (b == 1) System.out.println("Success");		// if 1, success
			else System.out.println("Failure");				// if 0, failure
		}
	}
}
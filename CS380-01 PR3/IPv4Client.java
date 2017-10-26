import java.io.*;
import java.net.*;

public class IPv4Client
{
	public static short checksum(byte[] bytes)
	{
		int b = (bytes.length + (bytes.length % 2)) / 2;	// pairs of bytes
		short s;											// temp short
		int sum = 0;										// 32 bit storage space
		
		for (short i = 0; i < b; i++) {						// sum pairs of bytes
			
			s = (short) ((bytes[i * 2] & 0xFF) << 8);		// align bytes in short
			if ((i * 2) + 1 < bytes.length) {
				s += (bytes[(i * 2) + 1] & 0xFF);
			}
			
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
	
	public static byte[] genData(int n)	// create array of size n containing random bytes
	{
		int b = (int) Math.pow(2, (n + 1));	// array size is a power of 2
		byte[] data = new byte[b]; 			// create array
		for (int i = 0; i < data.length; i++) data[i] = (byte) (Math.random() * 255);
		return data;
	}
	
	public static byte[] IPv4Packet(byte[] data) throws Exception
	{
		byte[] header = new byte[20];
			
		header[0] = 69;	// 01000101 : Version 0100, HLen 0101
		header[1] = 0;	// TOS
		
		short length = (short) (data.length + header.length);
		header[2] = (byte) ((length & 0xFF00) >> 8);
		header[3] = (byte) (length & 0x00FF);	// Length of packet
		
		header[4] = 0;
		header[5] = 0;	// Identification	
		header[6] = 64; // 01000000 : Flags 010, Offset 00000
		header[7] = 0;	// Offset
		header[8] = 50; // TTL : 50 seconds
		header[9] = 6;	// Protocol : 0x06 for TCP
						// skip checksum until after header is filled

		URL getIP = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(getIP.openStream(), "UTF-8"));
		String[] src = in.readLine().split("\\.");	// fetch client's external IP address
		for (int i = 0; i < src.length; i++) {
			int j = Integer.parseInt(src[i]);		// convert to byte value
			header[i + 12] = (byte) j;				// source address bytes
		}
		
		header[16] = (byte) 18;
		header[17] = (byte) 221;
		header[18] = (byte) 102;
		header[19] = (byte) 182;					// destination address bytes
		
		short cks = checksum(header);				// checksum of header
		header[10] = (byte) ((cks & 0xFF00) >> 8);
		header[11] = (byte) (cks & 0x00FF);
		
		// Store header and data in a singular byte array
		byte[] packet = new byte[header.length + data.length];
		for (int j = 0; j < header.length; j++) {packet[j] = header[j];}
		for (int k = 0; k < data.length; k++) {packet[header.length + k] = data[k];}
		
		return packet;
	}
	
	public static void main(String[] args) throws Exception
	{
		try (Socket socket = new Socket("18.221.102.182", 38003))
		{
			// read/write to socket
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			OutputStream os = socket.getOutputStream();
			
			System.out.println("CS380-01 PR3\nNathan Asdarjian\nAlfredo Ceballos\n");
			
			for(int i = 0; i < 12; i++) {			// 12 packets to be sent:
				byte[] data = genData(i);			// create random data array,
				System.out.printf("Data length% 5d: ", data.length);
				byte[] packet = IPv4Packet(data);	// create IPv4 packet with data,
				os.write(packet);					// send packet,
				System.out.println(is.readLine());	// print result
			}
		}	
	}
}
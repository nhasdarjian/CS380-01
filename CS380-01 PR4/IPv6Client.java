import java.io.*;
import java.net.*;

public class IPv6Client
{
	
	public static byte[] genData(int n)	// create array of size n containing random bytes
	{
		int b = (int) Math.pow(2, (n + 1));	// array size is a power of 2
		byte[] data = new byte[b]; 			// create array
		for (int i = 0; i < data.length; i++) data[i] = (byte) (Math.random() * 255);
		return data;
	}
	
	public static byte[] IPv6Packet(byte[] data, byte[] destAddress) throws Exception
	{
		byte[] header = new byte[40];
		
		header[0] = (byte) 96; // 01100000 : Version 0x06, Traffic Class 0x00
		header[1] = (byte) 0;	// First four bytes traffic class, next four flow label
		header[2] = (byte) 0;
		header[3] = (byte) 0;	// Flow label
		
		short s = (short) data.length;
		header[4] = (byte) ((s & 0xFF00) >> 8);
		header[5] = (byte) (s & 0x00FF);		// Payload length
		
		header[6] = (byte) 17;	// Next header - UDP Protocol = 17
		header[7] = (byte) 20; // Hop limit - 20
		
		for (int i = 0; i < 10; i++) header[i + 8] = 0; // IPv4 to IPv6 has 10 empty bytes

		header[18] = (byte) -1;	// IPv4 to IPv6 has FFFF before IPv4 value
		header[19] = (byte) -1;
		
		URL getIP = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(getIP.openStream(), "UTF-8"));
		String[] src = in.readLine().split("\\.");	// fetch client's external IP address
		for (int i = 0; i < src.length; i++) {
			int j = Integer.parseInt(src[i]);		// convert to byte value
			header[i + 20] = (byte) j;				// source address bytes
		}
		
		for (int j = 0; j < 10; j++) header[j + 24] = 0;// IPv4 to IPv6 has 10 empty bytes
		
		header[34] = (byte) -1;
		header[35] = (byte) -1;
		
		for (int k = 0; k < 4; k++) header[36 + k] = destAddress[k]; // dest address
		
		byte[] packet = new byte[header.length + data.length]; // put header and data in one array
		for (int k = 0; k < header.length; k++) {packet[k] = header[k];}
		for (int l = 0; l < data.length; l++) {packet[header.length + l] = data[l];}
		return packet;
	}
	
	public static void main(String[] args) throws Exception
	{
		try (Socket socket = new Socket("18.221.102.182", 38004))
		{
			// read/write to socket
			BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
			OutputStream os = socket.getOutputStream();
			
			System.out.println("CS380-01 PR4\nNathan Asdarjian\n");
			
			for(int i = 0; i < 12; i++) {			// 12 packets to be sent:
				byte[] data = genData(i);			// create random data array,
				System.out.printf("Data length% 5d: 0x", data.length);
				byte[] destAddress = socket.getInetAddress().getAddress();
				byte[] packet = IPv6Packet(data, destAddress);	// create IPv6 packet with data,
				os.write(packet);					// send packet,
				
				for (int j = 0; j < 4; j++) System.out.printf("%02X", is.read());	// print result
				System.out.println();
				
			}
		}	
	}
}
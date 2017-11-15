import java.io.*;
import java.net.*;

public class UDPClient
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
	
	public static byte[] IPv4Packet(byte[] data, byte[] dest) throws Exception
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
		header[9] = 17;	// Protocol : 0x06 for TCP
						// skip checksum until after header is filled

		URL getIP = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(getIP.openStream(), "UTF-8"));
		String[] src = in.readLine().split("\\.");	// fetch client's external IP address
		for (int i = 0; i < src.length; i++) {
			int j = Integer.parseInt(src[i]);		// convert to byte value
			header[i + 12] = (byte) j;				// source address bytes
		}
		
		for (int j = 0; j < dest.length; j++) header[j + 16] = dest[j];
		
		short cks = checksum(header);				// checksum of header
		header[10] = (byte) ((cks & 0xFF00) >> 8);
		header[11] = (byte) (cks & 0x00FF);
		
		// Store header and data in a singular byte array
		byte[] packet = new byte[header.length + data.length];
		for (int k = 0; k < header.length; k++) packet[k] = header[k];
		for (int l = 0; l < data.length; l++) packet[header.length + l] = data[l];
		
		return packet;
	}
	
	public static short UDPchecksum(byte[] ipv4, byte[] udp)
	{
		byte[] b = new byte[udp.length + 12];
		for (int i = 0; i < 8; i++) b[i] = ipv4[i + 12]; // src/dest addresses
		b[8] = 0;
		b[9] = ipv4[9];
		b[10] = udp[4];
		b[11] = udp[5];
		
		for (int i = 0; i < udp.length; i++) b[i + 12] = udp[i];
		return checksum(b);
	}
	
	public static byte[] UDPPacket(byte[] data, byte[] dest)
	{
		short s = (short) data.length;
		byte[] b = new byte[data.length + 8];
		b[0] = (byte) 208;
		b[1] = (byte) 13;
		b[2] = dest[0];
		b[3] = dest[1];
		b[4] = (byte) ((data.length & 0xFF00) >> 8);
		b[5] = (byte) (data.length & 0x00FF);
		b[6] = 0;
		b[7] = 0;
		for (int i = 0; i < data.length; i++) b[i + 8] = data[i];
		return b;
	}
	
	public static void main(String[] args) throws Exception
	{
		try (Socket socket = new Socket("18.221.102.182", 38005))
		{
			// read/write to socket
			BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
			OutputStream os = socket.getOutputStream();
			
			System.out.println("CS380-01 PR5\nNathan Asdarjian\n");
			byte[] dest = socket.getInetAddress().getAddress();
			
			byte[] packet1 = IPv4Packet( new byte[] { (byte) 222, (byte) 173, (byte) 190, (byte) 239 } , dest);
			System.out.print("Handshake:        0x");
			os.write(packet1);
			for (int i = 0; i < 4; i++) System.out.printf("%02X", is.read());
			System.out.println();
			byte[] p = new byte[] { (byte) is.read(), (byte) is.read() };
			short port = (short) (((p[0] & 0xFF) << 8) + (p[1] & 0xFF));
			System.out.println("  Port number: " + (port & 0xFFFF));
			float avg = 0;
			for (int j = 0; j < 12; j++)
			{
				byte[] data = genData(j);
				System.out.printf("Data length% 5d: 0x", data.length);
				byte[] udp = UDPPacket(data, p);
				byte[] ipv4 = IPv4Packet(udp, dest);
				short s = UDPchecksum(ipv4, udp);
				ipv4[26] = (byte) ((s & 0xFF00) >> 8);
				ipv4[27] = (byte) (s & 0x00FF);
				long t = System.currentTimeMillis();
				os.write(ipv4);
				for (int k = 0; k < 4; k++) System.out.printf("%02X", is.read());
				t = System.currentTimeMillis() - t;
				System.out.println("\n  RTT: " + t + "ms");
				avg += t;
			}
			avg /= 12;
			System.out.printf("Average RTT: %.2fms",avg);
		}	
	}
}
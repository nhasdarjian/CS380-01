import java.io.*;
import java.net.*;
import java.security.*;
import java.util.zip.*;
import javax.crypto.*;

public class FileTransfer
{
	public static void makeKeys()
	{
		try
		{
			System.out.println("\nGenerating keys...");
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(4096);
			KeyPair keyPair = gen.genKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("public.bin"))))
			{
				oos.writeObject(publicKey);
				System.out.println("Public key saved to directory.");
			}
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("private.bin"))))
			{
				oos.writeObject(privateKey);
				System.out.println("Private key saved to directory.");
			}						
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void server(String privateKey, String port)
	{
		try (ServerSocket ss = new ServerSocket(Integer.parseInt(port)))
		{
			while (true)
			{
				try (Socket s = ss.accept())
				{
					try (ObjectInputStream fis = new ObjectInputStream(new FileInputStream(new File(privateKey))))
					{
						boolean connected = true;
						ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
						ObjectInputStream is = new ObjectInputStream(s.getInputStream());
						
						Key sessionKey;
						Cipher c = null;
						FileOutputStream fos = null;
						long chunkCount, count = 0;
						int chunkSize;
						String fileName = "";
						Message m = null;
						
						CRC32 crc = new CRC32();
						
						while (connected)
						{
							m = (Message) is.readObject();
							System.out.print(m);
							switch (m.toString())
							{
								case "START":
									try
									{
										StartMessage startM = (StartMessage) m;
										String[] dir = startM.getFile().split("/");
										fileName = dir[dir.length - 1];
										fos = new FileOutputStream(new File("in/" + fileName));
										
										chunkCount = startM.getSize();
										chunkSize = startM.getChunkSize();
									
										c = Cipher.getInstance("RSA");
										c.init(Cipher.UNWRAP_MODE, (Key) fis.readObject()); 
										sessionKey = c.unwrap(startM.getEncryptedKey(), "AES", Cipher.SECRET_KEY);
										
										c = Cipher.getInstance("AES");
										c.init(Cipher.DECRYPT_MODE, sessionKey);
										
										os.writeObject(new AckMessage(0));
									}
									catch (Exception e)
									{
										os.writeObject(new AckMessage(-1));
									}
									break;
								case "STOP":
									StopMessage stopM = (StopMessage) m;
									os.writeObject(new AckMessage(-1));
									if (fos != null) 
									{
										fos.close();
										new File(fileName).delete();
									}
									break;
								case "CHUNK":
									Chunk chunk = (Chunk) m;
									if (chunk.getSeq() == count)
									{
										System.out.printf(" %1$4s", chunk.getSeq());
										byte[] data = c.doFinal(chunk.getData());
										crc.reset();
										crc.update(data);
										if (chunk.getCrc() == (int) crc.getValue())
										{
											System.out.print(" OK");
											fos.write(data);
											os.writeObject(new AckMessage((int) ++count));
										}
										else
										{
											System.out.print(" FAIL (CRC)");
											os.writeObject(new AckMessage(-1));
										}
										/*
										for (int i = 0; i < data.length; i++)
										{
											System.out.printf("%02X", data[i] & 0xFF);
											if (i % 16 == 15) System.out.println();
										}
										*/
											
									}
									else System.out.print(" FAIL (SEQ)");
									break;
								case "DISCONNECT":
									if (fos != null) fos.close();
									is.close();
									os.close();
									s.close();
									connected = false;
									break;
							}
							System.out.println();
						}
					}					
				}
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("\nKeys not generated. Please run with argument \"makekeys\".");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void client(String publicKey, String host, String port)
	{
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(publicKey))))
		{
			try (Socket socket = new Socket(host, Integer.parseInt(port)))
			{
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
				PublicKey p = (PublicKey) ois.readObject();
				
				KeyGenerator aes = KeyGenerator.getInstance("AES");
				aes.init(128);
				SecretKey key = aes.generateKey();
				Cipher c = Cipher.getInstance("RSA");
				c.init(Cipher.WRAP_MODE, p);
				
				byte[] k = c.wrap(key);
				String s = "";
				boolean cont = true;
				File f = null;
				CRC32 crc = new CRC32();
				
				while (f == null && cont) 
				{
					System.out.print("\nEnter file to transfer: out/");
					s = System.console().readLine();
					if (s.equals("quit")) cont = false;
					else
					{
						f = new File("out/" + s);
						if (!f.exists())
						{
							System.out.println("Bad file path. Try again, or type \"quit\" to exit.");
							f = null;
						}
					}
				}
				
				if (cont)
				{
					int cs;
					System.out.print("\nEnter desired chunk size (default 1024): ");
					try
					{
						cs = Integer.parseInt(System.console().readLine());
					}
					catch (NumberFormatException e)
					{
						cs = 1024;
						System.out.println("Number parse failed; defaulting to 1024");
					}
					os.writeObject(new StartMessage(s, k, cs));
					AckMessage am = (AckMessage) is.readObject();
					if (am.getSeq() == 0)
					{
						FileInputStream fis = new FileInputStream(f);
						c = Cipher.getInstance("AES");
						c.init(Cipher.ENCRYPT_MODE, key);
						
						int chunkNum = (int) (f.length() / cs);
						if (f.length() % cs != 0) chunkNum++;
						
						System.out.println("\nSending file \'" + s + "\' using " + chunkNum + " chunks...");
						
						for (int i = 0; i < chunkNum; i++)
						{
							byte[] b = 	new byte[cs];
							for (int j = 0; j < cs; j++)
							{								
								b[j] = (byte) fis.read();
								//System.out.printf("%02X", b[j] & 0xFF);
								//if (j % 16 == 15) System.out.println();
							}
							crc.reset();
							crc.update(b);
							long cVal = crc.getValue();
							b = c.doFinal(b);

							Chunk chunk = new Chunk(i, b, (int) cVal);
							os.writeObject(chunk);
							am = (AckMessage) is.readObject();
							
							if (am.getSeq() != i + 1) System.out.println("ACK DOES NOT MATCH");
							else System.out.printf("CHUNK %1$4s OK\n", i);
						}
					}
				}
				os.writeObject(new DisconnectMessage());
				System.out.println("\nFile saved: in/" + s);
				os.close();
				is.close();
				socket.close();
			}
			
			ois.close();
			
		}
		catch (FileNotFoundException e)
		{
			System.out.println("\nKeys not generated. Please run with argument \"makekeys\".");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length != 0)
			switch (args[0])
			{
				case "makekeys":
					makeKeys();
					break;
				case "server":
					if (args.length == 3) server(args[1], args[2]);
					break;
				case "client":
					if (args.length == 4) client(args[1], args[2], args[3]);
					break;
			}
		System.out.println("\nExiting program.");
	}
}
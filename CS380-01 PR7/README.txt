This is an FTP client/server that uses RSA and
AES encryption.

Run this program with:

	java FileTransfer makekeys
	java FileTransfer server private.bin <port>
	java FileTransfer client public.bin <host> <port>

After running the program with makekeys, two files will
generate in the directory (private.bin and public.bin).

Running the server with access to the private key will
allow it to decrypt anything the client sends out with
the public key.

Put a file in /out and run the client to move the file
to /in using encryption.

AckMessage, Chunk, DisconnectMessage, Message,
MessageType, StartMessage, StopMessage classes
authored by Nicholas Pantic
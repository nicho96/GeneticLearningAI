package ca.nicho.smb3net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Start {
	
	public static void main(String[] s) throws IOException{

		ServerSocket server = new ServerSocket(1024);
		Socket socket = server.accept();
		
		System.out.println("Emulator Connected");
		
		DataInputStream stream = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		new SMBEvolver(stream, out);
		
		server.close();
	
	}

}

package me.hedgehog.bukkitsocketserver;

import java.io.*;
import java.net.*;

public class HTTPServer implements Runnable {
	BukkitSocketServer plugin;
	ServerSocket socket;
	public Boolean tRunning = true;
	int port, maxConnections=0;
	
	public HTTPServer(BukkitSocketServer parent){
		plugin = parent;
		port = plugin.port;
		plugin.socket = socket;
	}
	
	public void kill(){
		if(socket != null){
			try {socket.close();
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public void run(){
		try{
			socket = new ServerSocket(port);
			Socket server;
			int i = 0;

			while(tRunning &&((maxConnections == 0) ||(i++ < maxConnections))){
				ClientHandler connection;

				server = socket.accept();
				connection = new ClientHandler(server, plugin);
				Thread t = new Thread(connection);
				
				t.start();
			}
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}finally{
			if(socket != null){
				try {socket.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
}

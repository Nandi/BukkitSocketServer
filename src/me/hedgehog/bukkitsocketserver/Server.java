package me.hedgehog.bukkitsocketserver;

import java.io.*;
import java.net.*;

public class Server implements Runnable {
	BukkitSocketServer plugin;
	int port, maxConnections=0;
	
	public Server(BukkitSocketServer parent){
		plugin = parent;
		port = plugin.port;
	}
	
	public void run(){
		try{
			ServerSocket listener = new ServerSocket(port);
			Socket server;
			int i = 0;

			while((maxConnections == 0) ||(i++ < maxConnections)){
				ClientHandler connection;

				server = listener.accept();
				connection = new ClientHandler(server, plugin);
				Thread t = new Thread(connection);
				
				t.start();
			}
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}
}

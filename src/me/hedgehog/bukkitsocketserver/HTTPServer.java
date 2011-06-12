package me.hedgehog.bukkitsocketserver;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPServer extends Thread {
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	BukkitSocketServer plugin;
	ServerSocket sock;
	
	private Thread listeningThread;
	int port;
	
	public HTTPServer(BukkitSocketServer parent, int port){
		plugin = parent;
		this.port = port;
	}
	
	public void startServer() throws IOException{
		sock = new ServerSocket(port);
		listeningThread = this;
		start();
		log.info("[BukkitSocketServer] Webserver started on port: "+port);
	}
	
	public void run(){
		
		while(listeningThread == Thread.currentThread()){
			try{
				ClientHandler connection;
				Socket socket = sock.accept();
				connection = new ClientHandler(socket, plugin);
				Thread t = new Thread(connection);
				
				t.start();
				
			} catch (IOException ioe) {
				log.log(Level.SEVERE, "[BukkitSocketServer] Exception on WebServer-thread", ioe);
			}
		}
	}
	
	public void shutdown(){
		log.info("[BukkitSocketServer] Shutting down webserver...");
		try{
			if(sock != null)
				sock.close();
		}catch (IOException ioe) {
			log.log(Level.INFO, "[BukkitSocketServer] Exception while closing socket for webserver shutdown", ioe);			
		}
		listeningThread = null;
	}
}

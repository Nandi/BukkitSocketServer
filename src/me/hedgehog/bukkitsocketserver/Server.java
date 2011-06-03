package me.hedgehog.bukkitsocketserver;

import java.io.*;
import java.net.*;
import java.lang.reflect.Field;

import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;


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
	
	// Read the console
	public String readConsole(){
		String console = "";
		try{
			FileInputStream fstream = new FileInputStream("server.log");
			
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine()) != null){
				console += strLine;
			}
			in.close();
		}catch(Exception e){}
		return console;
	}
	
	// Send commands to the console
	public void consoleCommand(String cmd){
		CraftServer craftserver = (CraftServer)plugin.getServer();
		Field field;
		try { 
			field = CraftServer.class.getDeclaredField("console");
		}
		catch (NoSuchFieldException ex) {return; }
		catch (SecurityException ex) {return; }
		
		MinecraftServer mcs;
		
		try { 
			field.setAccessible(true);
			mcs = (MinecraftServer) field.get(craftserver);
		}
		catch (IllegalArgumentException ex) {return; }
		catch (IllegalAccessException ex) {return; }
		
		if ( (!mcs.isStopped) && (MinecraftServer.isRunning(mcs)) )
			mcs.issueCommand(cmd, mcs);
	}
}

package me.hedgehog.bukkitsocketserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;

public class CommandHandler {
	
	BukkitSocketServer plugin;
	
	//Constructor
	public CommandHandler(BukkitSocketServer parent){
		plugin = parent;
	}
	
	// Kicks a list of players
	public void kickPlayer(String[] args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				plugin.getServer().getPlayer(arg).kickPlayer("You have been kicked by the webadmin.");
			}
		}
	}
	
	// Bans a list of players
	public void banPlayer(String[] args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				consoleCommand("ban " + arg);
			}
		}
	}
	
	// Gets plugin list
	public void pluginList(){
	}
	
	// Gets a list of all players, takes world name as arguments
	public void playerList(String[] args){
	}
	
	// Gets players inventory takes player name as argument
	public void playerInventory(String[] args){
	}
	
	// Gets the console output since last start
	public void getConsole(){
	}

	// Gets a standard responce to a unrecognized command
	public void errorString(String err){
	}
	
	// Read the server log
	private List<String> readConsole(){
		List<String> console = new LinkedList<String>();
		try{
			FileInputStream fstream = new FileInputStream("server.log");
			
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine()) != null){
				console.add(strLine);
			}
			in.close();
		}catch(Exception e){}
		return console;
	}
	
	// Send commands to the console
	
	private void consoleCommand(String cmd){
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

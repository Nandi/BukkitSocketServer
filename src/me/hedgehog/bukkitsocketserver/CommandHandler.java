package me.hedgehog.bukkitsocketserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.MinecraftServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import org.jdom.*;

public class CommandHandler {
	
	BukkitSocketServer plugin;
	
	//Constructor
	public CommandHandler(BukkitSocketServer parent){
		plugin = parent;
	}
	
	// Kicks a list of players
	public void kickPlayer(List<String> args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				plugin.getServer().getPlayer(arg).kickPlayer("You have been kicked by the webadmin.");
			}
		}
	}
	
	// Bans a list of players
	public void banPlayer(List<String> args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				consoleCommand("ban " + arg);
			}
		}
	}
	
	// Gets plugin list
	public Element pluginList(){
		Element rootElement = new Element("pugins");
		
		Plugin[] pluginList = plugin.getServer().getPluginManager().getPlugins();
		
		for(Plugin p : pluginList){
			Element plug = new Element("plugin");
			plug.setAttribute("name",p.getDescription().getName());
			plug.setAttribute("version", p.getDescription().getVersion());
		}
		
		return rootElement;
	}
	
	// Gets a list of all players, takes world name as arguments
	public Element playerList(List<String> args){
		Element rootElement = new Element("worlds");
		
		List<World> worlds = new LinkedList<World>();
		if(args.size() != 0){
			for(int i = 0; i < args.size(); i++){
				worlds.add(plugin.getServer().getWorld(args.get(i)));
	    	}
			
		}
		else{
    		worlds = plugin.getServer().getWorlds();
		}
		
		for(int i=0;i<worlds.size();i++){
			Element world = new Element("world");
			rootElement.addContent(world);
			world.setAttribute("name", worlds.get(i).getName());
			
			Element players = new Element("players");
			world.addContent(players);
			
			List<Player> playerList = worlds.get(i).getPlayers();
			if(playerList.size() == 0){
				players.setAttribute("empty", "true");
			}
			else{
				players.setAttribute("empty", "false");
			}
			for(int j=0;j<playerList.size();j++){
				Element player = new Element("player");
				player.setAttribute("name", playerList.get(j).getName());
				player.setAttribute("dispName", playerList.get(j).getDisplayName());
				player.setAttribute("dead", Boolean.toString(playerList.get(j).isDead()));
				players.addContent(player);
			}
		}
		
		return rootElement;
	}
	
	// Gets players inventory takes player name as argument
	public Element playerInventory(List<String> args){
		Element rootElement = new Element("players");
		
		List<Player> players = new LinkedList<Player>();
		for(String arg : args){
			if (plugin.getServer().getPlayer(arg) == null)
				return errorString("One or all of the players listed is not online");
			else
				players.add(plugin.getServer().getPlayer(arg));
		}
		
		
		for(Player p : players){
			//format xml with inventory
			Element player = new Element("player");
			player.setAttribute("name", p.getName());
			rootElement.addContent(player);
			
			PlayerInventory inv = p.getInventory();
			ItemStack[] invCont = inv.getContents();
			
			Element inventory = new Element("inventory");
			player.addContent(inventory);
			
			for(int i = 0; i< invCont.length; i++){
				Element item = new Element("item");
				
				
				
				//Check if empty slot!
				if (invCont[i] == null){
					item.setAttribute("empty", Boolean.toString(true));
				}
				else {
					item.setAttribute("empty", Boolean.toString(false));
					item.setAttribute("id", Integer.toString(invCont[i].getTypeId()));
					item.setAttribute("name", invCont[i].getType().name());
					item.setAttribute("stackSize", Integer.toString(invCont[i].getAmount()));
				}
				inventory.addContent(item);
			}
		}

		return rootElement;
	}
	
	// Gets the console output since last start
	public Element getConsole(){
		List<String> lines = readConsole();
		
		Element rootElement = new Element("console");
		
		int start = 0;
		for(int i=0;i<lines.size();i++){
			if(lines.get(i).indexOf("[INFO] Starting minecraft server") != -1)
				start = i;
		}
		for(int i=start;i<lines.size();i++){
			Element line = new Element("line");
			line.setText(lines.get(i));
			rootElement.addContent(line);
		}
		return rootElement;
	}

	// Gets a standard responce to a unrecognized command
	public Element errorString(String err){
		Element rootElement = new Element("error");
		rootElement.setText(err);
		
		return rootElement;
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

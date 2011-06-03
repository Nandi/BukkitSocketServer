package me.hedgehog.bukkitsocketserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.minecraft.server.MinecraftServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CommandHandler {
	
	BukkitSocketServer plugin;
	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	Document doc;
	
	//Constructor
	public CommandHandler(BukkitSocketServer parent){
		plugin = parent;
		try{
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
		}catch (Exception e) {
			e.printStackTrace();
		}
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
	
	// Gets a list of all players, takes world name as arguments
	public Element playerList(List<String> args){
		doc  = docBuilder.newDocument();
		Element rootElement = doc.createElement("worlds");
		doc.appendChild(rootElement);
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
			Element world = doc.createElement("world");
			rootElement.appendChild(world);
			world.setAttribute("name", worlds.get(i).getName());
			
			Element players = doc.createElement("players");
			world.appendChild(players);
			
			List<Player> playerList = worlds.get(i).getPlayers();
			if(playerList.size() == 0){
				players.setAttribute("empty", "true");
			}
			else{
				players.setAttribute("empty", "false");
			}
			for(int j=0;j<playerList.size();j++){
				Element player = doc.createElement("player");
				player.setAttribute("name", playerList.get(j).getName());
				player.setAttribute("dispName", playerList.get(j).getDisplayName());
				player.setAttribute("dead", Boolean.toString(playerList.get(j).isDead()));
				players.appendChild(player);
			}
		}
		
		return rootElement;
	}
	
	// Gets players inventory takes player name as argument
	public Element playerInventory(List<String> args){
		doc  = docBuilder.newDocument();
		Element rootElement = doc.createElement("players");
		doc.appendChild(rootElement);
		
		List<Player> players = new LinkedList<Player>();
		for(String arg : args){
			if (plugin.getServer().getPlayer(arg) == null)
				return errorString("One or all of the players listed is not online");
			else
				players.add(plugin.getServer().getPlayer(arg));
		}
		
		
		for(Player p : players){
			//format xml with inventory
			Element player = doc.createElement("player");
			player.setAttribute("name", p.getName());
			rootElement.appendChild(player);
			
			PlayerInventory inv = p.getInventory();
			ItemStack[] invCont = inv.getContents();
			
			Element inventory = doc.createElement("inventory");
			player.appendChild(inventory);
			
			for(int i = 0; i< invCont.length; i++){
				Element item = doc.createElement("item");
				
				
				
				//Check if empty slot!
				if (invCont[i] == null){
					System.out.println("Empty slot");
					item.setAttribute("empty", Boolean.toString(true));
				}
				else {
					System.out.println(invCont[i].toString());
					item.setAttribute("empty", Boolean.toString(false));
					item.setAttribute("id", Integer.toString(invCont[i].getTypeId()));
					item.setAttribute("name", invCont[i].getType().name());
					item.setAttribute("stackSize", Integer.toString(invCont[i].getAmount()));
				}
				inventory.appendChild(item);
			}
		}

		return rootElement;
	}
	
	// Gets a standard responce to a unrecognized command
	public Element errorString(String err){
		doc  = docBuilder.newDocument();
		
		Element rootElement = doc.createElement("error");
		rootElement.appendChild(doc.createTextNode(err));
		doc.appendChild(rootElement);
		
		return rootElement;
	}
	
	// Gets the console output since last start
	public Element getConsole(){
		List<String> lines = readConsole();
		doc  = docBuilder.newDocument();
		
		Element rootElement = doc.createElement("console");
		doc.appendChild(rootElement);
		int start = 0;
		for(int i=0;i<lines.size();i++){
			if(lines.get(i).indexOf("[INFO] Starting minecraft server") != -1)
				start = i;
		}
		for(int i=start;i<lines.size();i++){
			Element line = doc.createElement("line");
			line.appendChild(doc.createTextNode(lines.get(i)));
			rootElement.appendChild(line);
		}
		
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

package me.hedgehog.bukkitsocketserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.hedgehog.bukkitsocketserver.tools.*;
import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;

public class CommandHandler {
	
	BukkitSocketServer plugin;
	HttpContext context;
	
	//Constructor
	public CommandHandler(BukkitSocketServer plugin, HttpContext context){
		this.plugin = plugin;
		this.context = context;
		
	}
	
	public void compileResponse() throws IOException{
		Map<String, String[]> args = new HashMap<String, String[]>();
		byte[] bytes = null;
		HttpResponse response = context.response;
		String q = context.request.query;
		
		String[] parts = q.split("&");
		for(String s : parts){
			String[] temp = s.split("=");
			args.put(temp[0], temp[1].split(","));
		}
		String get = args.get("get")[0];
		
		if(get.equalsIgnoreCase("kickPlayer")){
			kickPlayer(args.get("name"));
		}
		else if(get.equalsIgnoreCase("banPlayer")){
			banPlayer(args.get("name"));
		}
		else if(get.equalsIgnoreCase("pluginList")){
			bytes = pluginList().getBytes("UTF-8");
		}
		else if(get.equalsIgnoreCase("playerList")){
			bytes = playerList(args.get("world")).getBytes("UTF-8");
		}
		else if(get.equalsIgnoreCase("playerInventory")){
			bytes = playerInventory(args.get("name")).getBytes("UTF-8");
		}
		else if(get.equalsIgnoreCase("getConsole")){
			bytes = getConsole().getBytes("UTF-8");
		}
		else{
			bytes = errorString("Command not recognized.").getBytes("UTF-8");
		}
		String dateStr = new Date().toString();
		response.fields.put(HttpField.Date, dateStr);
		response.fields.put(HttpField.ContentType, "text/plain; charset=utf-8");
		response.fields.put(HttpField.ContentLength, Integer.toString(bytes.length));
		context.response.status = HttpStatus.OK;
		
		BufferedOutputStream out = null;
		out = new BufferedOutputStream(response.getBody());
		out.write(bytes);
		out.flush();
	}
	
	// Kicks a list of players
	private void kickPlayer(String[] args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				plugin.getServer().getPlayer(arg).kickPlayer("You have been kicked by the webadmin.");
			}
		}
	}
	
	// Bans a list of players
	private void banPlayer(String[] args){
		for(String arg : args){
			if(plugin.getServer().getPlayer(arg) != null){
				consoleCommand("ban " + arg);
			}
		}
	}
	
	// Gets plugin list
	private String pluginList(){
		String output = "{\"plugins\":"+ Json.stringifyJson(plugin.getServer().getPluginManager().getPlugins());
		return output;
	}
	
	// Gets a list of all players, takes world name as arguments
	private String playerList(String[] args){
		return errorString("Not yet implemented");
	}
	
	// Gets players inventory takes player name as argument
	private String playerInventory(String[] args){
		return errorString("Not yet implemented");
	}
	
	// Gets the console output since last start
	private String getConsole(){
		String output = "{ \"lines\":"+Json.stringifyJson(readConsole())+"}";
		return output;
	}

	// Gets a standard responce to a unrecognized command
	private String errorString(String err){
		String output = "{\"error\":"+Json.stringifyJson(err)+"}";
		return output;
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

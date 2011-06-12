package me.hedgehog.bukkitsocketserver;

import java.util.HashMap;
import java.util.logging.Logger;
import java.io.*;
import java.net.ServerSocket;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class BukkitSocketServer extends JavaPlugin{
	Logger log = Logger.getLogger("MineCraft");
	//private final TCPComsListener playerListener = new TCPComsListener(this);
	public static HashMap<Player, Boolean> playerList = new HashMap<Player, Boolean>();
	static String mainDirectory = "plugins/BukkitSocketServer";
	File file = new File(mainDirectory + File.separator + "config.yml");
	Thread server;
	public ServerSocket socket = null;
	int port;
	
	
	// Hello, world!
	public void onEnable()
	{
		// Print some basic info about the command.  This won't appear in the server's log file, as that would be unnecessary.
		System.out.println("[" + this.getDescription().getName() + "] version " + this.getDescription().getVersion() + " loaded");
		
		//Load options
		new File(mainDirectory).mkdir();

        if(!file.exists()){
            try {
                file.createNewFile();
                write("port", "1234");
                port = 1234;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //load stuff on statup here
        	port = Integer.parseInt(read("port"));
        }
        newServer();
	}

	// Goodbye, world.
	public void onDisable()
	{
		// If we had any persistence, we might do a final save here.
		try{
			if(socket != null)
				socket.close();
		}catch (Exception e) {}
	}

	public Configuration load(){

        try {
            Configuration config = new Configuration(file);
            config.load();
            return config;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public void newServer(){
		server = null;
		server = new Thread(new HTTPServer(this));
		server.start();
	}
	
	public void write(String root, Object x){ //just so you know, you may want to write a boolean, integer or double to the file as well, therefore u wouldnt write it to the file as "String" you would change it to something else
    	Configuration config = load();
        config.setProperty(root, x);
        config.save();
    }

    public  String read(String root){
    	Configuration config = load();
        return config.getString(root);
    }
	
	/*public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		 
		if(cmd.getName().equalsIgnoreCase("tcp")){ // If the player typed /basic then do the following...
			if(args[0].equalsIgnoreCase("dirt")){
					String com = "give Hedgehog_ 3 64";
					System.out.println(com);
					consoleCommand(com);
			}
			return true;
		} //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
		return false;
	}*/
}

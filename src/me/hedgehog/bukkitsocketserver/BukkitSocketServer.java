package me.hedgehog.bukkitsocketserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class BukkitSocketServer extends JavaPlugin{
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	static String mainDirectory = "plugins/BukkitSocketServer";
	File file = new File(mainDirectory + File.separator + "config.yml");
	HttpServer httpServer;
	int port;
	
	
	// Hello, world!
	public void onEnable(){
		log.info("[" + this.getDescription().getName() + "] version " + this.getDescription().getVersion() + " loaded");
				
		//Load options
		new File(mainDirectory).mkdir();

        if(!file.exists()){
            try {
                file.createNewFile();
                write("port", "1234");
                port = 1234;
            } catch (IOException ioe) {
            	log.log(Level.SEVERE, "[BukkitSocketServer] Could not create config file at " + file.getPath(), ioe);
            }
        } else {
        	port = Integer.parseInt(read("port"));
        }
        
        httpServer = new HttpServer(this, port);
        try{
        	httpServer.startServer();
        }catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Goodbye, world.
	public void onDisable(){
		if(httpServer != null){
			httpServer.shutdown();
			httpServer = null;
		}
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

	public void write(String root, Object x){
    	Configuration config = load();
        config.setProperty(root, x);
        config.save();
    }

    public  String read(String root){
    	Configuration config = load();
        return config.getString(root);
    }
}

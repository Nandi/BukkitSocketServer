package me.hedgehog.bukkitsocketserver;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.InetAddress;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class BukkitSocketServer extends JavaPlugin{
	protected static final Logger log = Logger.getLogger("Minecraft");
	protected static Map<InetAddress, String> tokens = new HashMap<InetAddress, String>();
	
	private static String mainDirectory = "plugins/BukkitSocketServer";
	private File file = new File(mainDirectory + File.separator + "config.yml");
	private HttpServer httpServer;
	protected int port;
	protected String user;
	protected String pass;
	
	
	// Hello, world!
	public void onEnable(){
		log.info("[" + this.getDescription().getName() + "] version " + this.getDescription().getVersion() + " loaded");
				
		//Load options
		new File(mainDirectory).mkdir();

        if(!file.exists()){
            try {
                file.createNewFile();
                write("port", "1234");
                write("user", "admin");
                write("pass", "admin");
                port = 1234;
                user = "admin";
                pass = getSHA("admin");
                
            } catch (IOException ioe) {
            	log.log(Level.SEVERE, "[BukkitSocketServer] Could not create config file at " + file.getPath(), ioe);
            }
        } else {
        	port = Integer.parseInt(read("port"));
        	user = read("user");
        	pass = getSHA(read("pass"));
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
	
	public String getSHA(String s){
		String sha1 = "";
		String text = "admin";
		byte[] sha1hash = new byte[40];
		try{
			MessageDigest md;
		    md = MessageDigest.getInstance("SHA-512");
		    
		    md.update(text.getBytes("UTF-8"), 0, text.length());
		    sha1hash = md.digest();
		    sha1 = bytes2String(sha1hash);
		}catch (Exception e) {}
		return sha1;
	}
	
	public static String bytes2String(byte[] bytes) {
		StringBuilder string = new StringBuilder();
		for (byte b: bytes) {
			String hexString = Integer.toHexString(0x00FF & b);
			string.append(hexString.length() == 1 ? "0" + hexString : hexString);
		}
		return string.toString();
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

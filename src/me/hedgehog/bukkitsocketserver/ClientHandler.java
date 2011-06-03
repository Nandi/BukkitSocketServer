package me.hedgehog.bukkitsocketserver;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.server.MinecraftServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SuppressWarnings("unused")
public class ClientHandler implements Runnable {
    private Socket server;
    private BukkitSocketServer plugin;
    private String line;
    private CommandHandler c;

    public ClientHandler(Socket server, BukkitSocketServer plugin) {
    	this.server = server;
    	this.plugin = plugin;
    	c = new CommandHandler(plugin);
    	
    }

    public void run () {
    	try {
			// Get input from the client
			DataInputStream in = new DataInputStream (server.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			PrintStream out = new PrintStream(server.getOutputStream());
			
			String input = "";
			while((line = br.readLine()) != null && !line.equals("END")) {
				try {
					input += line;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			decodeXML(input, out);

			server.close();
    	} catch (IOException ioe) {
    		System.out.println("IOException on socket listen: " + ioe);
    		ioe.printStackTrace();
    	}
    }


    private void decodeXML(String input, PrintStream out) {
    	try{
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input));
	        Document doc = dBuilder.parse(is);
	        
	        NodeList nList = doc.getElementsByTagName("command");
	        
	        String[] output = {};
	        for(int i = 0; i < nList.getLength(); i++){
	        	Node nNode = nList.item(i);	    
	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	            	Element eElement = (Element) nNode;
	            	String command = nNode.getChildNodes().item(0).getNodeValue();
	            	
	            	NamedNodeMap atr = nNode.getAttributes();
	            	List<String> attributes = new LinkedList<String>();
	            	for(int j = 0; j < atr.getLength(); j++){
	            		attributes.add(atr.item(j).getNodeValue());
	            	}
	            	
	            	output = processInput(command, attributes);
	            }
	            for(String item : output){
					out.println(item);
				}
	        }
	        
    	}catch (Exception e) {
    		e.printStackTrace();
		}
	}
	
    private String[] processInput(String input, List<String> args) throws Exception{
        String[] output = null;
       
        if(input.equalsIgnoreCase("playerlist"))
        	output = c.playerList(args);
        else if(input.equalsIgnoreCase("console"))
        	output = c.getConsole();
        else if(input.equalsIgnoreCase("playerinventory"))
        	output = c.playerInventory(args);
        else if(input.equalsIgnoreCase("kickPlayer"))
        	c.kickPlayer(args);
        else
        	output = c.errorString("Server does not recognize the command."); 
        
        return output;
       }

}
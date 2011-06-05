package me.hedgehog.bukkitsocketserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

@SuppressWarnings("unused")
public class ClientHandler implements Runnable {
    private Socket server;
    private BukkitSocketServer plugin;
    private CommandHandler c;

    public ClientHandler(Socket server, BukkitSocketServer plugin) {
    	this.server = server;
    	this.plugin = plugin;
    	c = new CommandHandler(this.plugin);
    	
    }

    public void run () {
    	String line;
    	try {
			// Get input from the client
			DataInputStream in = new DataInputStream (server.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			PrintStream out = new PrintStream(server.getOutputStream());
			
			String input = "";
			while((line = br.readLine()) != null && !line.equals("")) {
				input += line;
			}
			System.out.println("[" + plugin.getDescription().getName() + "]: Message recived");
			decodeXML(input, out);

			server.close();
    	} catch (IOException ioe) {
    		System.out.println("IOException on socket listen: " + ioe);
    		ioe.printStackTrace();
    	}
    }


    
	@SuppressWarnings("unchecked")
	private void decodeXML(String input, PrintStream out) {
    	try{
    		
    		SAXBuilder builder = new SAXBuilder();
    		Reader in = new StringReader(input);
    		Document docIn = null;
    		Element output = null;
    		
    		docIn = builder.build(in);
    		
	        List<Element> nList = docIn.getRootElement().getChildren("command");
	        
	        Element rootElement = new Element("responseList");
	        
	        for(int i = 0; i < nList.size(); i++){
	        	
	        	Element eElement = nList.get(i);
            	Element response = new Element("response");
            	
            	String command = eElement.getText();
            	
            	response.setAttribute("command", command);
            	
            	List<Attribute> attr = eElement.getAttributes();
            	List<String> attributes = new LinkedList<String>();
            	for(int j = 0; j < attr.size(); j++){
            		attributes.add(attr.get(j).getValue());
            	}
            	
            	output = processInput(command, attributes);
            	
            	response.addContent(output);
            	rootElement.addContent(response);
	        }
	        
	        Document docOut = new Document(rootElement);
	        
	        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
	        //XMLOutputter outputter = new XMLOutputter();
	        StringBuffer process = new StringBuffer();
	        
	        String temp = outputter.outputString(docOut).trim();
	        
	        String[] arr = temp.split("\n\r");
	        
	        System.out.println(arr.length);
	        
	        for(String s : arr){
	        	out.println(s);
	        }
	        
	        //out.println(temp);
	        out.println("\n\r");
	        out.flush();
	        
	        System.out.println("[" + plugin.getDescription().getName() + "]: Message sent");

    	}catch (Exception e) {
    		e.printStackTrace();
		}
	}
	
    private Element processInput(String input, List<String> args) throws Exception{
        Element output = null;
       
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
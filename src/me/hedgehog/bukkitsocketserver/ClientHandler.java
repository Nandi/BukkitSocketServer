package me.hedgehog.bukkitsocketserver;

import java.io.*;
import java.net.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.hedgehog.bukkitsocketserver.tools.*;

public class ClientHandler implements Runnable {
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	private static Pattern requestHeaderLine = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+HTTP/(.+)$");
	private static Pattern requestHeaderField = Pattern.compile("^([^:]+):\\s*(.+)$");
	private Socket socket;
	private BukkitSocketServer plugin;
	private CommandHandler commands;
	
	private PrintStream printOut;
	private StringWriter sw = new StringWriter();
	private Matcher requestHeaderLineMatcher;
	private Matcher requestHeaderFieldMatcher;

	public ClientHandler(Socket socket, BukkitSocketServer plugin) {
		this.socket = socket;
		this.plugin = plugin;
	}
	
	private final static void readLine(InputStream in, StringWriter sw) throws IOException {
		int readc;
		while((readc = in.read()) > 0) {
			char c = (char)readc;
			if (c == '\n')
				break;
			else if (c != '\r')
				sw.append(c);
		}
	}
    
	private final String readLine(InputStream in) throws IOException {
		readLine(in, sw);
		String r = sw.toString();
		sw.getBuffer().setLength(0);
		return r;
	}
	
	private final boolean readRequestHeader(InputStream in, HttpRequest request) throws IOException{
		String statusLine = readLine(in);
		
		if(statusLine == null)
			return false;
			
		if(requestHeaderLineMatcher == null)
			requestHeaderLineMatcher = requestHeaderLine.matcher(statusLine);
		else
			requestHeaderLineMatcher.reset(statusLine);
		
		Matcher m = requestHeaderLineMatcher;
		if(!m.matches())
			return false;
		request.method = m.group(1);
		
		//Find a regex that includes the possebility of queries
		{
			String[] temp;
			if((temp = m.group(2).split("\\?")).length > 1){
				request.query = temp[1];
			}
			request.path = temp[0];
				
		}
		request.version = m.group(3);
		
		String line;
		while(!(line = readLine(in)).equalsIgnoreCase("")){
			if(requestHeaderFieldMatcher == null)
				requestHeaderFieldMatcher = requestHeaderField.matcher(line);
			else
				requestHeaderFieldMatcher.reset(line);
			
			m = requestHeaderFieldMatcher;
			
			if(m.matches()){
				String fieldName = m.group(1);
				String fieldValue = m.group(2);
				request.fields.put(fieldName, fieldValue);
			}
		}
		return true;
	}
	
	public static final void writeResponseHeader(PrintStream out, HttpResponse response) throws IOException {
		out.append("HTTP/");
		out.append(response.version);
		out.append(" ");
		out.append(String.valueOf(response.status.getCode()));
		out.append(" ");
		out.append(response.status.getText());
		out.append("\r\n");
		for(Entry<String, String> field : response.fields.entrySet()){
			out.append(field.getKey());
			out.append(": ");
			out.append(field.getValue());
			out.append("\r\n");
		}
		out.append("\r\n");
		out.flush();
	}
	
	public void writeResponseHeader(HttpResponse response) throws IOException {
		writeResponseHeader(printOut, response);
	}

	public void run() {
		try{
			if(socket == null)
				return;
			socket.setSoTimeout(5000);
			
			//BukkitSocketServer.tokens.put(, null);
			
			InputStream in = socket.getInputStream();
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), 40960);
			
			printOut = new PrintStream(out, false);
			while(true){
				HttpRequest request = new HttpRequest();
				
				request.remoteAddr = ((InetSocketAddress)socket.getRemoteSocketAddress()).getAddress();
				
				if(!BukkitSocketServer.tokens.containsKey(request.remoteAddr))
					BukkitSocketServer.tokens.put(request.remoteAddr, null);
				
				if(!readRequestHeader(in, request)){
					if(BukkitSocketServer.tokens.containsKey(request.remoteAddr))
						BukkitSocketServer.tokens.remove(request.remoteAddr);
					
					socket.close();
					return;
				}

				long bound = -1;
				BoundInputStream boundBody = null;
				{
					String contentLengthStr = request.fields.get(HttpField.ContentLength);
					if(contentLengthStr != null){
						try{
							bound = Long.parseLong(contentLengthStr);
						}catch (NumberFormatException e) {}
						
						if(bound >= 0)
							request.body = boundBody = new BoundInputStream(in, bound);
						else
							request.body = in;
					}
				}

				NonClosableOutputStream  nonClosableResponseBody = new NonClosableOutputStream(out);
				final HttpResponse response = new HttpResponse(this, nonClosableResponseBody);
				
				HttpContext context = new HttpContext(request, response, this);
				
				commands = new CommandHandler(plugin, context);
				
				//Write response here!
				commands.compileResponse();
				
				nonClosableResponseBody.close();
				
				if(bound > 0 && boundBody.skip(bound)< bound){
					//socket.close();
					//return;
				}
				
				String connection = response.fields.get("Connection");
				String contentLength = response.fields.get("Content-Length");
				if(contentLength == null && connection == null){
					response.fields.put("Content-Length", "0");
					OutputStream responseBody = response.getBody();
					
					if(responseBody == null){
						out.flush();
						
						if(BukkitSocketServer.tokens.containsKey(context.request.remoteAddr))
							BukkitSocketServer.tokens.remove(context.request.remoteAddr);
						
						socket.close();
						return;
					}
				}
				
				if(connection != null && connection.equals("close")){
					out.flush();
					
					if(BukkitSocketServer.tokens.containsKey(context.request.remoteAddr))
						BukkitSocketServer.tokens.remove(context.request.remoteAddr);
					
					socket.close();
					return;
				}
				out.flush();
			}
		}catch(IOException ioe){
			if(socket != null){
				try{
					if(BukkitSocketServer.tokens.containsKey((InetSocketAddress)socket.getRemoteSocketAddress()))
						BukkitSocketServer.tokens.remove((InetSocketAddress)socket.getRemoteSocketAddress());
					
					socket.close();
				}catch (IOException e) {
				}
			}
			return;
		}catch (Exception e){
			if(socket != null){
				try{
					if(BukkitSocketServer.tokens.containsKey((InetSocketAddress)socket.getRemoteSocketAddress()))
						BukkitSocketServer.tokens.remove((InetSocketAddress)socket.getRemoteSocketAddress());
					
					socket.close();
				}catch (IOException ioe) {
				}
			}
			log.log(Level.SEVERE, "[BukkitSocketServer] Exception while handling request: ", e);
			e.printStackTrace();
			return;
		}
	}
}
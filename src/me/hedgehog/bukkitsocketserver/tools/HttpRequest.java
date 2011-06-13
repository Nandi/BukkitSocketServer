package me.hedgehog.bukkitsocketserver.tools;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	public InetAddress remoteAddr;
    public String method;
    public String path;
    public String query;
    public String version;
    public Map<String, String> fields = new HashMap<String, String>();
    public InputStream body;
}

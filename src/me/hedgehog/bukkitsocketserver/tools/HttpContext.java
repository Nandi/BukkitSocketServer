package me.hedgehog.bukkitsocketserver.tools;

import me.hedgehog.bukkitsocketserver.ClientHandler;

public class HttpContext {
    public HttpRequest request;
    public HttpResponse response;
    public ClientHandler connection;
    
    public HttpContext(HttpRequest request, HttpResponse response, ClientHandler connection) {
        this.request = request;
        this.response = response;
        this.connection = connection;
    }
}

package me.hedgehog.bukkitsocketserver.tools;


public interface HttpHandler {
    void handle(String path, HttpContext context) throws Exception;
}

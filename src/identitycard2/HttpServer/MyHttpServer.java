/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import identitycard2.Config.ConfigParser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nguyenduyy
 */
public class MyHttpServer {
    private int port = 6969;
    HttpServer httpServer = null;
    ExecutorService httpThreadPool;
    public static MyHttpServer instance= null;
    public static MyHttpServer getInstance(){
        if(instance == null) {
            instance = new MyHttpServer();
            
        }
        return instance;
    }
    public void stop(){
        
        this.httpServer.stop(1);
        this.httpThreadPool.shutdownNow();


    }
    public void start(){
        httpServer.start();
    }
    private MyHttpServer(){
        try {
            Integer configPort = ConfigParser.getInstance().getListenPort();
            if(configPort != null) port = configPort;
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
             httpThreadPool= Executors.newFixedThreadPool(1);
             httpServer.setExecutor(httpThreadPool);
            httpServer.createContext("/", new indexGetHandler());
            httpServer.createContext("/verify", new verifyServicePostHandler());
            httpServer.createContext("/new", new newSessionGetHandler());
            httpServer.setExecutor(null);
            
            
        } catch (IOException ex) {
            Logger.getLogger(MyHttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    public void setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
    }
           
    
    public static void parseQuery(String query, Map<String, 
	Object> parameters) throws UnsupportedEncodingException {

         if (query != null) {
                 String pairs[] = query.split("[&]");
                 for (String pair : pairs) {
                          String param[] = pair.split("[=]");
                          String key = null;
                          String value = null;
                          if (param.length > 0) {
                          key = URLDecoder.decode(param[0], 
                          	System.getProperty("file.encoding"));
                          }

                          if (param.length > 1) {
                                   value = URLDecoder.decode(param[1], 
                                   System.getProperty("file.encoding"));
                          }

                          if (parameters.containsKey(key)) {
                                   Object obj = parameters.get(key);
                                   if (obj instanceof List<?>) {
                                            List<String> values = (List<String>) obj;
                                            values.add(value);

                                   } else if (obj instanceof String) {
                                            List<String> values = new ArrayList<String>();
                                            values.add((String) obj);
                                            values.add(value);
                                            parameters.put(key, values);
                                   }
                          } else {
                                   parameters.put(key, value);
                          }
                 }
         }
}
}


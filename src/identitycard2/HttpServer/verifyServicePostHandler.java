/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class verifyServicePostHandler implements HttpHandler {
    SessionHandler sessionHandler ;
    @Override
    public void handle(HttpExchange he) throws IOException {
          // parse request
          sessionHandler = SessionHandler.getInstance();
                 Map<String, Object> parameters = new HashMap<String, Object>();
                 InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                 BufferedReader br = new BufferedReader(isr);
                 String query = br.readLine();
                 String response = null;
                 PackageHandler pk = null;
                 try{
                     JSONObject json = new JSONObject(query);
                     pk = new PackageHandler(json);
                     
                     pk.sessionData = sessionHandler.getSessionByOfPartner(he.getRemoteAddress().getHostName());
                     pk.localsessionId = pk.sessionData.getSessionId();
                     response = pk.handle();
                     
                 }catch(Exception e){
                      response = null;
                 }
                 
                 // send response
                 if(response == null){
                     
                     response = PackageHandler.getErrorReponse("Invalid input, miss status field");
                 }
                 
                  
                  he.getResponseHeaders().set("Access-Control-Allow-Origin","*");
                   he.sendResponseHeaders(200, response.length());
                 OutputStream os = he.getResponseBody();
                 os.write(response.toString().getBytes());
                 os.close();
                 
                
    }
    
}

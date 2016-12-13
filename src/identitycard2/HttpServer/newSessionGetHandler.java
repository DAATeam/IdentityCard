/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import identitycard2.crypto.MD5Helper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 *
 * @author nguyenduyy
 */
public class newSessionGetHandler implements HttpHandler{
    SessionHandler sessionHandler;
    @Override
    public void handle(HttpExchange he) throws IOException {
        sessionHandler = SessionHandler.getInstance();
        //get other parameter such as basename, service name, link ,...
        SessionData sd = createNewSessionData();
        sessionHandler.createSession(sd);
        //response
        String response = sd.getSessionId();
        he.sendResponseHeaders(200, response.length());
        he.getResponseHeaders().set("Access-Control-Allow-Origin","*");
        OutputStream os = he.getResponseBody();
         sd.setStatus(SessionHandler.SessionStatusEnum.WAIT);
        sessionHandler.updateSession(sd);
                 
        os.write(response.toString().getBytes());
        os.close();
        
    }
    
    private SessionData createNewSessionData(){
        SessionData sd = new SessionData();
        Date now = new Date();
        String s = now.toString();
        sd.setTimestamp(now);
        sd.setStatus(SessionHandler.SessionStatusEnum.INIT);
        sd.setSessionId(MD5Helper.hashString(s));
        return sd;
    }
    
}

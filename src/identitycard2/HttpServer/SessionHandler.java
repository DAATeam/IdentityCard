/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 *
 * @author nguyenduyy
 */
public class SessionHandler {
    public static enum SessionStatusEnum {INIT,WAIT,VERIFY,VERIFYOK,VERIFYFAIL};
    public static String SESSION_BASENAME = "session";
    ArrayList<SessionData> sessionList = null;

    public static SessionHandler instance = null;
    public static SessionHandler getInstance(){
        if(instance == null){
            instance = new SessionHandler();
        }
        return instance;
    }
    private SessionHandler() {
        sessionList = new ArrayList<>();
    }
    public void createSession(SessionData data){
        if(isFreshSessionId(data.getSessionId())){
        sessionList.add(data);
        }
    }
    public SessionData getSession(String sid){
        for(SessionData sd : sessionList){
            if(sd.getSessionId().equals(sid)){
                
                return sd;
            }
        }
        return null;
    }
    
    public boolean isFreshSessionId(String sid){
        for(SessionData s : sessionList){
            if(s.getSessionId().equals(sid)){
                return false;
            }
            
        }
        return true;
    }
    public boolean updateSession(SessionData s){
        String id = s.getSessionId();
        for(int i=0; i< sessionList.size(); i++){
            if(sessionList.get(i).getSessionId().equals(s.getSessionId())){
                sessionList.set(i, s);
                return true;
            }
        }
        return false;
        
    }
        
}

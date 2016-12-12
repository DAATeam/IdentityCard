/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.VerifyApi;

import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class getVerifierApi extends ApiRequester{
    private String sessionId;
    public getVerifierApi(){
        super();
        this.method = "GET";
        
        
    }
    private Object onData(byte[] data){
        try{
            String s = new String(data);
            JSONObject json = new JSONObject(s);
            return json;
        }catch(Exception e){
        
            return null;
        }
        
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        this.getData.put("session", sessionId);
    }
    
}

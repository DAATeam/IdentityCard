/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.HttpServer;

import identitycard2.DirtyWork;
import identitycard2.JoinApi.ApiFormat;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Issuer;
import identitycard2.Models.Verifier;
import identitycard2.Tools.Data;
import identitycard2.crypto.BNCurve;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class PackageHandler {
    public static String PERMISSION = "permission";
    public static String CREDENTIAL = "credential";
    JSONObject json = null;
    String localsessionId = null;
    Verifier verifier = null;
    BNCurve curve = null;
    Issuer.IssuerPublicKey ipk = null;
    SessionData sessionData =null;
    public PackageHandler(JSONObject json){
        this.json = json;
        
        curve = Data.getInstance().getBNCurve();
        if(curve != null){
        verifier =new Verifier(curve);
        }
        ipk = Data.getInstance().getIssuerPubicKey();
        
    }
    public String handle(){
        if(checkStatusInJSON(json)){
            Map<String , JSONObject> map = validateServiceDataFormat(json);
            if(map != null){
                boolean valid = verifyService(map);
                if(valid){
                    //String sn = extractServiceName(map);
                    //FIXME : open request user acception
                    
                    String d = generateResponseForService(map);
                    sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYOK);
                    
                    SessionHandler.getInstance().updateSession(sessionData);
                    synchronized (this){
                    SessionHandler.getInstance().removeSessionBySessionID(sessionData.getSessionId());
                    }
                    return d;
                }
                else {
                    sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYFAIL);
            SessionHandler.getInstance().updateSession(sessionData);
                    return getErrorReponse("verifyService fail");}
                        }
        
            else{
                sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYFAIL);
            SessionHandler.getInstance().updateSession(sessionData);
                return getErrorReponse("Wrong format") ;
            }
        }
        else{
            return null;
        }
    }
    public Map<String, JSONObject> validateServiceDataFormat(JSONObject json){
        String p_label = "permission";
        String s_label = "sessionId";
        if(json.has(ApiFormat.PERMISSION) && json.has(ApiFormat.SESSIONID) && json.has(ApiFormat.SIG)){
            try {
                JSONObject j1 = new JSONObject();
                JSONObject j2 = new JSONObject();
                JSONObject j3 = new JSONObject();
                j1.put(p_label, json.getString(p_label));
                j2.put(s_label, json.getString(s_label));
                j3.put(ApiFormat.SIG, json.getString(ApiFormat.SIG));
                Map<String, JSONObject> map = new HashMap<String, JSONObject>();
                map.put(p_label, j1);
                map.put(s_label, j2);
                map.put(ApiFormat.SIG, j3);
                
                return map;
            } catch (JSONException ex) {
                Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
        }
        else{
            return null;
        }
    }
    public boolean verifyService(Map<String, JSONObject> map){
        boolean valid = true;
        
        JSONObject p_json = map.get("permission");
        try {
            byte[] sig_data =DirtyWork.hexStringToByteArray(map.get(ApiFormat.SIG).getString(ApiFormat.SIG));
            //Parse to signature on localSessinId wrt basename = "permission"
            Authenticator.EcDaaSignature sig = new Authenticator.EcDaaSignature(
                sig_data,localsessionId.getBytes(),curve
                );
            //Verify and Examine permission
            String per = p_json.getString(PERMISSION);
            boolean b =verifier.verifyWrt(per.getBytes() , localsessionId.getBytes(), sig, PERMISSION, ipk, null);
            return b;
            
        } catch (Exception ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
       
    }
    private String extractServiceName(Map<String, JSONObject> map){
        JSONObject p_json = map.get(PERMISSION);
        
            try {
                 return p_json.getString("service_name");
            } catch (JSONException ex) {
                Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        
    }
    
            
    public static boolean checkStatusInJSON(JSONObject json){
        if(json.has(ApiFormat.STATUS)){
            try {
                if(json.getString(ApiFormat.STATUS).equals(ApiFormat.OK)){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException ex) {
                Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        else{
            return false;
        }
    }
    private boolean checkSessionId(String sid){
        if(sid.equals(localsessionId)){
            
            sessionData  = SessionHandler.getInstance().getSession(sid);
            if(sessionData.getStatus()!= SessionHandler.SessionStatusEnum.WAIT){
                return false;
            }
            sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFY);
            SessionHandler.getInstance().updateSession(sessionData);
            return true;
        }
        else return false;
    }
    private String generateResponseForService(Map<String, JSONObject> map){
           JSONObject p_json = map.get(PERMISSION);
        try {
            //get Ano-id member type id 
            JSONObject json = new JSONObject(p_json.getString(PERMISSION));
            String mitd = Integer.toString(Data.getInstance().getMember_type());
            String level = json.getString(mitd);
            //get level data 
            String res = collectDataInLevel(level, map);
            return res;
            
        } catch (JSONException ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    private String collectDataInLevel(String level,Map<String, JSONObject> map){
        String info = Data.getInstance().getValueOfField(level);
        String cre = Data.getInstance().getCredentialOfField(level);
        String gsk = Data.getInstance().getGskOfField(level);
        try {
            JSONObject s_json = map.get("sessionId");
            String sessionId = s_json.getString("sessionId");
            //create sign on value of JSON
            Authenticator au = new Authenticator(curve, ipk, new BigInteger(gsk));
            Issuer.JoinMessage2 jm2 = new Issuer.JoinMessage2(curve, cre);
            au.EcDaaJoin2Wrt(jm2,info);
            //FIXME : Now, user fix basename for easy test
            Authenticator.EcDaaSignature sig = au.EcDaaSignWrt(info.getBytes(), "verification", sessionId);
            JSONObject res = new JSONObject();
            res.put("information",info);
            res.put(ApiFormat.SIG, DirtyWork.bytesToHex(sig.encode(curve)));
            res.put(ApiFormat.STATUS, ApiFormat.OK);
            return res.toString();
        } catch (Exception ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
            
            
        

    
    
    public static String getErrorReponse(String msg){
        try {
            JSONObject j = new JSONObject();
            j.put(ApiFormat.STATUS, ApiFormat.ERROR);
            j.put(ApiFormat.MESSAGE, msg);
            return j.toString();
        } catch (JSONException ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

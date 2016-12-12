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
                    String d = generateResponseForService(map);
                    sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYOK);
                    SessionHandler.getInstance().updateSession(sessionData);
                    return d;
                }
                else {
                    sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYFAIL);
            SessionHandler.getInstance().updateSession(sessionData);
                    return null;}
                        }
        
            else{
                sessionData.setStatus(SessionHandler.SessionStatusEnum.VERIFYFAIL);
            SessionHandler.getInstance().updateSession(sessionData);
                return null ;
            }
        }
        else{
            return null;
        }
    }
    public Map<String, JSONObject> validateServiceDataFormat(JSONObject json){
        if(json.has(ApiFormat.CL_SERNAME) && json.has(ApiFormat.CL_PERMISSION)){
            try {
                JSONObject j1 = new JSONObject(json.getString(ApiFormat.CL_SERNAME));
                JSONObject j2 = new JSONObject(json.get(ApiFormat.CL_PERMISSION));
                Map<String, JSONObject> map = new HashMap<String, JSONObject>();
                map.put(ApiFormat.CL_SERNAME, j1);
                map.put(ApiFormat.CL_PERMISSION, j2);
                
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
        for(Map.Entry<String, JSONObject> e : map.entrySet()){
            valid&=verifyItem(e.getValue(), e.getKey());
            if(valid == false) return valid;
        }
        if(valid){
            
            return true;
        }
        else return false;
    }
    public boolean verifyItem(JSONObject json, String basename){
        try {
            String message = json.getString(ApiFormat.VALUE);
            String Ssig  = json.getString(ApiFormat.SIG);
            String Scert = json.getString(ApiFormat.CERT);
            String Ssid= json.getString(ApiFormat.SESSIONID);
            
            if(localsessionId == null) localsessionId = Ssid;
            if(!checkSessionId(Ssid)) return false;
            
            String Ssesig = json.getString(ApiFormat.SESSION_SIG);
            
            //parse
            if(verifier != null && ipk != null){
          
            Authenticator.EcDaaSignature sig = new Authenticator.EcDaaSignature(
                    DirtyWork.hexStringToByteArray(Ssig),
                    message.getBytes(),
                    curve
                    
            );
            Authenticator.EcDaaSignature cert = new Authenticator.EcDaaSignature(
                    DirtyWork.hexStringToByteArray(Scert),
                    sig.encode(curve), // encode without nym 
                    curve
                    
            );
            Authenticator.EcDaaSignature sessionSig = new Authenticator.EcDaaSignature(
                    DirtyWork.hexStringToByteArray(Ssesig),
                    Ssid.getBytes(),
                    curve
                    
            );
            boolean valid = true;
            valid &= verifier.verify(sig, basename, ipk, null);
            valid &= verifier.verify(cert, ApiFormat.CERT_BASENAME, ipk, null);
            valid &= verifier.verify(sessionSig, basename, ipk, null);
            // FIX : add link fuction
            valid &= verifier.link(sig, sessionSig);
            return valid;
            }
            else return false;
        } catch (Exception ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
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
              
            
            String s = collectDataByFields(map);
            return s;
        

    }
    private String collectDataByFields(Map<String, JSONObject> map){
        JSONObject j = map.get(ApiFormat.CL_PERMISSION);
        String[] fs;
        String sessionId;
        try {
            fs = j.getString(ApiFormat.VALUE).split(",");
            sessionId = json.getString(ApiFormat.SESSIONID);
        } catch (JSONException ex) {
            Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if(fs!= null && fs.length >0){
        JSONObject res =new JSONObject();
        for(int i=0; i< fs.length; i++){
            JSONObject d = new JSONObject();
            try {
                d.put(ApiFormat.VALUE,Data.getInstance().getValueOfField(fs[i]));
                String message = d.toString();
                d.put(ApiFormat.SESSIONID, sessionId);
                Authenticator tmp_aut = Data.getInstance().getAuthenticator(fs[i]);
                Authenticator.EcDaaSignature sesssig = tmp_aut.EcDaaSignWithNym(fs[i], sessionId,sessionId);
                Authenticator.EcDaaSignature sig = tmp_aut.EcDaaSignWithNym(fs[i],message,sessionId);
                d.put(ApiFormat.SESSION_SIG, DirtyWork.bytesToHex(sesssig.encodeWithNym(curve)));
                d.put(ApiFormat.SIG, DirtyWork.bytesToHex(sig.encodeWithNym(curve)));
                d.put(ApiFormat.CERT, Data.getInstance().getCertOfField(fs[i]));
                res.put(fs[i], d.toString());
            } catch (Exception ex) {
                Logger.getLogger(PackageHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
            
        }
        return res.toString();
        }
    
    else{
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

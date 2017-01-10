/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.RequestTask;

import identitycard2.DirtyWork;
import identitycard2.HttpServer.SessionData;
import identitycard2.HttpServer.SessionHandler;
import identitycard2.JoinApi.ApiFormat;
import identitycard2.JoinApi.JoinApiHandler;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Authenticator.EcDaaSignature;
import identitycard2.Models.Issuer;
import identitycard2.Models.Issuer.JoinMessage2;
import identitycard2.Tools.Data;
import identitycard2.VerifyOld.ApiRequester;
import identitycard2.crypto.BNCurve;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import org.json.JSONObject;
import sun.misc.IOUtils;

/**
 *
 * @author nguyenduyy
 */
public class RequestTask extends Task<JSONObject>{
        BNCurve curve;
    Issuer.IssuerPublicKey ipk;
    String host;
    public RequestTask(){
        
    }

    public Issuer.IssuerPublicKey getIpk() {
        return ipk;
    }

    public void setIpk(Issuer.IssuerPublicKey ipk) {
        this.ipk = ipk;
    }
    

    public BNCurve getCurve() {
        return curve;
    }

    public void setCurve(BNCurve curve) {
        this.curve = curve;
    }

    
    

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    


    
    @Override
    protected JSONObject call() throws Exception {
        String link_new = "";
        if(host.endsWith("/")){
            link_new = host + "new";
        }
        else link_new =  host + "/new";
        
        URL url = new URL(link_new);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        
        String sid = handleResponse(con);
        if(sid ==null) {

            return null;
        }

            Date d = new Date();
            SessionData sd = new SessionData();
            sd.setBasename(ApiFormat.PERMISSION);
            sd.setPartnerName(host);
            sd.setTimestamp(d);
            sd.setSessionId(d.toString());
            String per = Data.getInstance().getValueOfField(ApiFormat.PERMISSION);
            String cre = Data.getInstance().getCredentialOfField(ApiFormat.PERMISSION);
            String gsk = Data.getInstance().getGskOfField(ApiFormat.PERMISSION);
            SessionHandler.getInstance().createSession(sd);

            Authenticator.EcDaaSignature sig =createSig(per,cre,gsk,sid,ApiFormat.PERMISSION);
        if(sig == null){ 

            return null;}
        //send /verify request

        JSONObject resJson = new JSONObject();
        resJson.put(ApiFormat.STATUS, ApiFormat.OK);
        resJson.put(ApiFormat.PERMISSION, per);
        resJson.put(ApiFormat.SIG, DirtyWork.bytesToHex(sig.encode(curve)));
        resJson.put(ApiFormat.SESSIONID, sd.getSessionId());
        String postData = resJson.toString();
        String link_verify = "";

        if(host.endsWith("/")){
            link_verify = host + "verify";
        }
        else link_verify =  host + "/verify";
        
        url = new URL(link_verify);
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setFixedLengthStreamingMode(postData.length());
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();
        
        String info = handleResponse(con);
        if(info == null){
            SessionHandler.getInstance().removeSessionBySessionID(sd.getSessionId());
            return null;
        }
        ReceivedInfoOberservable rio = new ReceivedInfoOberservable();
        rio.response = new JSONObject(info);
        //notify UI thread
        Data.getInstance().setReceiveInfo(rio);
        
        return new JSONObject(info);
            
        
        
    }
    private Authenticator.EcDaaSignature createSig(String info, String cre, String gsk, String sid, String basename){
        try {
            Authenticator au = new Authenticator(curve, ipk, new BigInteger(gsk));
            Issuer.JoinMessage2 jm2 = new Issuer.JoinMessage2(curve, cre);
            au.EcDaaJoin2Wrt(jm2,info);
            Authenticator.EcDaaSignature sig = au.EcDaaSignWrt(info.getBytes(),basename, sid);
            
            return sig;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RequestTask.class.getName()).log(Level.SEVERE, null, ex);
            
            return null;

        }
    }
    private String handleResponse(HttpURLConnection con){
        try {
            int code = con.getResponseCode();
            //Logger.getLogger("Task","code "+ String.valueOf(code));
            if(code ==200){
                
            }
            else{
                
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(ApiRequester.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int bytesRead =0;
            InputStream in = con.getInputStream();
            while ((bytesRead = in.read(buff)) != -1)
            {
                bo.write(buff, 0, bytesRead);
            }
                
            
		in.close();
            
            return new String(bo.toByteArray());
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
		
    }
    
}

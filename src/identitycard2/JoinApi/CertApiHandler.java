/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.JoinApi;

import identitycard2.DirtyWork;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Issuer;
import identitycard2.Tools.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class CertApiHandler {
    private GetCertTask remoteIssuer = null;
    private Issuer.JoinMessage2 jm2=null;
    private String basename=null;
    private Issuer.JoinMessage1 jm1=null;
    
    private String message = null;
    private BigInteger gsk = null;
    private Authenticator.EcDaaSignature signature = null;
    
    public final static String TAG_NONCE = "nonce=";
    public final static String TAG_SIG = "sig=";
    public final static String TAG_BASENAME = "basename=";
    //output
    private String certJSON = null;
    

    public Authenticator.EcDaaSignature getSignature() {
        return signature;
    }

    public void setSignature(Authenticator.EcDaaSignature signature) {
        this.signature = signature;
    }

       
    public CertApiHandler(){
        
    }
    public String getPOSTData(){
        StringBuilder builder = new StringBuilder();
        builder.append(TAG_NONCE+jm1.nonce.toString());
        builder.append("&");
        builder.append(TAG_BASENAME+basename);
        builder.append("&");
        try {
            signature = remoteIssuer.getAuthenticator().EcDaaSign(basename, message);
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CertApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            signature = null;
        }
        builder.append(TAG_SIG+DirtyWork.bytesToHex(signature.encode(remoteIssuer.getCurve())));
        return builder.toString();
        
    }
    public void processApi(){
        try {
            URL url=new URL(remoteIssuer.getAddress().toString()+"/cert");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(setupConnection(connection)){
                handleResponse(connection);
            }
        } catch (IOException ex) {
            Logger.getLogger(Jm1ApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
     private boolean setupConnection(HttpURLConnection c){
          try {
            c.setRequestMethod("POST");
            String data = getPOSTData();
            c.setFixedLengthStreamingMode(data.length());
            c.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(c.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    private boolean handleResponse(HttpURLConnection con){
         BufferedReader in;
        try {
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
                
		in.close();
                
                JSONObject json = new JSONObject(response.toString());
                String status = json.getString(ApiFormat.STATUS);
                if(status.equals(ApiFormat.OK)){
                    onSuccess(json);
                }
                else{
                    onError(json);
                    return false;
                }
                
                
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    private void onSuccess(JSONObject json){
        if(json != null){
            try {
                certJSON = json.getString(ApiFormat.CERT);
                
            } catch (Exception ex) {
                Logger.getLogger(Jm1ApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void onError(JSONObject json){
        certJSON = null;
    }

    public GetCertTask getRemoteIssuer() {
        return remoteIssuer;
    }

    public void setRemoteIssuer(GetCertTask remoteIssuer) {
        this.remoteIssuer = remoteIssuer;
    }

    public Issuer.JoinMessage2 getJm2() {
        return jm2;
    }

    public void setJm2(Issuer.JoinMessage2 jm2) {
        this.jm2 = jm2;
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public Issuer.JoinMessage1 getJm1() {
        return jm1;
    }

    public void setJm1(Issuer.JoinMessage1 jm1) {
        this.jm1 = jm1;
    }
   

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigInteger getGsk() {
        return gsk;
    }

    public void setGsk(BigInteger gsk) {
        this.gsk = gsk;
    }

    public String getCertJSON() {
        return certJSON;
    }

    public void setCertJSON(String certJSON) {
        this.certJSON = certJSON;
    }
    
    
    
    
}

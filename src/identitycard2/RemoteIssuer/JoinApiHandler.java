/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.RemoteIssuer;

import identitycard2.DirtyWork;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Issuer;
import identitycard2.Tools.Data;
import identitycard2.crypto.BNCurve;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
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
public class JoinApiHandler {
    private String M; //PIN or finger printing
    public final String TAG_M = "M=";
    private String appId ; //provide by issuer
    public final String TAG_appId = "appId=";
    private GetCertTask remoteIssuer;
    //output
    
    private String curveName= null;
    private String ipkJSON = null;
    private String nonceString = null;
    
    public JoinApiHandler(GetCertTask remoteIssuer){
        this.remoteIssuer = remoteIssuer;
    }
    public String getPOSTData(){
        StringBuilder data = new StringBuilder();
        data.append(TAG_M + M);
        data.append("&");
        data.append(TAG_appId + appId);
        return data.toString();
    }
    public void processApi(){
        try {
            URL url=new URL(remoteIssuer.getAddress().toString()+"/join");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if(setupConnection(connection)){
                handleResponse(connection);
            }
        } catch (IOException ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        try {
           
            if( json!= null){
                curveName = json.getString(ApiFormat.CURVE);
                ipkJSON = json.getString(ApiFormat.CL_IPK);
                nonceString = json.getString(ApiFormat.CL_NONCE);
            }

        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            curveName = null;
            ipkJSON = null;
            
        }
    }
    
    
    private void onError(JSONObject json){
        curveName = null;
        ipkJSON = null;
    }

    public String getM() {
        return M;
    }

    public void setM(String M) {
        this.M = M;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public GetCertTask getRemoteIssuer() {
        return remoteIssuer;
    }

    public void setRemoteIssuer(GetCertTask remoteIssuer) {
        this.remoteIssuer = remoteIssuer;
    }

    public String getCurveName() {
        return curveName;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public String getIpkJSON() {
        return ipkJSON;
    }

    public void setIpkJSON(String ipkJSON) {
        this.ipkJSON = ipkJSON;
    }

    public String getNonceString() {
        return nonceString;
    }

    public void setNonceString(String nonceString) {
        this.nonceString = nonceString;
    }
    
 
    
}

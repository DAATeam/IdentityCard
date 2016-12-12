/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.JoinApi;

import identitycard2.Models.Issuer;
import identitycard2.Tools.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class Jm1ApiHandler {
    private GetCertTask remoteIssuer;
    private Issuer.JoinMessage1 jm1;
    private String jm1JSON;
    private String field;
    //output
    private String Jm2JSON ;
    
    
    public static final String TAG_JM1 = "jm1=";
    public static final String TAG_field = "field=";
    public Jm1ApiHandler(){
        
    }
    
    public String getPOSTData(){
        StringBuilder builder = new StringBuilder();
        builder.append(TAG_JM1+jm1JSON);
        builder.append("&");
        builder.append(TAG_field+field);
        return builder.toString();
        
    }
    public void processApi(){
        try {
            URL url=new URL(remoteIssuer.getAddress().toString()+"/jm1");
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
                Jm2JSON = json.getString(ApiFormat.JM2);

            } catch (Exception ex) {
                Logger.getLogger(Jm1ApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    public String getJm2JSON() {
        return Jm2JSON;
    }

    public void setJm2JSON(String Jm2JSON) {
        this.Jm2JSON = Jm2JSON;
    }
    private void onError(JSONObject json){
        
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
    
    
    public GetCertTask getRemoteIssuer() {
        return remoteIssuer;
    }

    public void setRemoteIssuer(GetCertTask remoteIssuer) {
        this.remoteIssuer = remoteIssuer;
    }

    public Issuer.JoinMessage1 getJm1() {
        return jm1;
    }

    public void setJm1(Issuer.JoinMessage1 jm1) {
        this.jm1 = jm1;
        this.jm1JSON = jm1.toJson(remoteIssuer.getCurve());
    }

    public String getJm1JSON() {
        return jm1JSON;
    }

    public void setJm1JSON(String jm1JSON) {
        this.jm1JSON = jm1JSON;
    }
    
}

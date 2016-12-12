/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.VerifyApi;



import identitycard2.JoinApi.ApiFormat;
import identitycard2.JoinApi.JoinApiHandler;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

import org.json.JSONObject;
import sun.misc.IOUtils;

/**
 *
 * @author nguyenduyy
 */
public class ApiRequester extends Task<Object> {
    URL URL = null;
    String method = null;
    Map<String, String> postData;
    Map<String,String> getData;
    // data listener
    public ObservableData observableData = null;
    public Observer observer = null;
    public ApiRequester(){
        postData = new HashMap<String, String>();
        getData = new HashMap<>();
        observableData = new ObservableData();
        
    }
    private String getPOSTData(){
        if(postData != null){
            ArrayList<String> res = new ArrayList<>();
            for(Map.Entry<String, String> e : postData.entrySet()){
                String key = e.getKey();
                String value = e.getValue();
                res.add(key+"="+value);
            }
            return String.join("&", res);
        }
        else return null;
    }
    private String getGETData(){
        if(getData != null){
            if(getData.size() == 0) return "";
            else{
                ArrayList<String> res = new ArrayList<>();
                for(Map.Entry<String, String> e : getData.entrySet()){
                    res.add(e.getKey()+"="+e.getValue());
                }
                return "?"+String.join("&", res);
            }
        }
        else return null;
    }
    private HttpURLConnection setupConnection(){
        if(method.equals("GET")){
            return setupGETConnection();
        }
        else if(method.equals("POST")){
            return setupPOSTConnection();
        }
        else return null;
    }
    private HttpURLConnection setupGETConnection(){
        try {
            HttpURLConnection c = null;       
            String data = getGETData();
            if(data != null){
                String link = URL.toString()+data;
                URL u = new URL(link);
                c = (HttpURLConnection) u.openConnection();
            }
            if(c != null){
                c.setRequestMethod("GET");
                
            }
            
            return c; 
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    private HttpURLConnection setupPOSTConnection(){
        
        try {
            HttpURLConnection c = (HttpURLConnection) URL.openConnection();
            c.setRequestMethod("POST");
            
            String data = getPOSTData();
            c.setFixedLengthStreamingMode(data.length());
            c.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(c.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
            return c; 
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    private Object handleResponse(HttpURLConnection con){
        try {
            int code = con.getResponseCode();
            if(code ==200){
                on200(con);
            }
            else{
                onError(con);
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(ApiRequester.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        
        try {
            
            InputStream in = con.getInputStream();
           
            byte[] response = IOUtils.readFully(in, 0, true);
		in.close();
            onData(response);
            return response;                
        } catch (Exception ex) {
            Logger.getLogger(JoinApiHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
		
    }
    /**Override
     * Process when response code is 200 OK
     * @param con 
     */
    private void on200(HttpURLConnection con){
        
    }
    /**Override
     * Process when request error
     * @param con 
     */
    private void onError(HttpURLConnection con){
        
    }
    /** Override this
     * Process byte array of data;
     * @param data
     * @return Object 
     */
    private Object onData(byte[] data){
        
        return data;
    }
    
    @Override
    protected Object call(){
        if(URL == null || method == null){
            return null;
        }
        else{
            HttpURLConnection c = setupConnection();
            if(c!= null){
                Object o = handleResponse(c);
                observableData.setObject(o);
                return o;
                
            }
            else return null;
        }
    }

    public URL getURL() {
        return URL;
    }

    public void setURL(URL URL) {
        this.URL = URL;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getPostData() {
        return postData;
    }

    public void setPostData(Map<String, String> postData) {
        this.postData = postData;
    }
    
}

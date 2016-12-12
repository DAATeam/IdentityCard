/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.VerifyApi;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class verifyApiHandler {
    getVerifierApi gApi = null;
    postVerifierApi pApi = null;
    URL url;
    Observer observer;
    public verifyApiHandler(){
        observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                try{
                ObservableData od = (ObservableData) o;
                JSONObject json= (JSONObject) od.getObject();
                //FIX : verify step
                //....
                //get value From Data
                pApi.setData(json.getString("message"));
                pApi.setSig(json.getString("sig"));
                pApi.setCert(json.getString("cert"));
                pApi.setSessionSig(json.getString("session_sig"));
                Thread t = new Thread(pApi);
                t.setDaemon(true);
                t.start();
                }catch(Exception e){
                    
                    return;
                }
                
            }
        };
        gApi = new getVerifierApi();
        //FIX : make random session
        gApi.setSessionId("randomSession");
        gApi.observableData.addObserver(observer);
        pApi = new postVerifierApi();
        
    }
    public void execute(){
        Thread t  =new Thread(gApi);
        t.setDaemon(true);
        t.start();
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
        gApi.setURL(url);
        pApi.setURL(url);
    }
    
    
}

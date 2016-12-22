/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.VerifyOld;

import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author nguyenduyy
 */
public class postVerifierApi extends ApiRequester {
    String data = null;
    String cert = null;
    String sig = null;
    String sessionSig = null;
    
    public postVerifierApi(){
        super();
        this.method = "POST";
        
    }
    private Object onData(byte[] data){
        return null;
    }

    public String getSessionSig() {
        return sessionSig;
    }

    public void setSessionSig(String sessionSig) {
        this.sessionSig = sessionSig;
        postData.put("session_sig", sessionSig);
    }

    public String getData() {
        return data;
    }

    public void setData(String message) {
        this.data = message;
        this.postData.put("message", data);
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
        postData.put("cert", cert);
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
        postData.put("sig", sig);
    }
    
            
}

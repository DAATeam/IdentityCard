/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.JoinApi;

import identitycard2.DirtyWork;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Issuer;
import identitycard2.Models.Verifier;
import identitycard2.Tools.Data;
import identitycard2.crypto.BNCurve;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.util.Callback;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class GetCertTask extends Task<Map<String, String>>{
    public String apiJoin = "join";
    public String apiJm1 = "jm1";
    public String apiCert = "cert";
    
    private URL address;
    private BNCurve curve = null; 
    private Authenticator authenticator = null;
    private Verifier verifier = null;
    
    private JoinApiHandler joinApiHandler = null;
    private Jm1ApiHandler jm1ApiHandler = null;
    private CertApiHandler certApiHandler = null;
    
    public static final int JOIN_FAIL = 10;
    public static final int JM1_FAIL = 20;
    public static final int CERT_FAIL = 30;
    public static final int OK = 40;

    //input 
    //join api
    private String appId = null;
    private String M = null;
    //jm1 api
    private String fieldName = null;
    private String basename = null;
    private String data = null;
    //output
    private BigInteger gsk = null;
    private BigInteger nonce = null;
    private Issuer.IssuerPublicKey ipk = null;
    private Issuer.JoinMessage1 jm1 = null;
    private Issuer.JoinMessage2 jm2 = null;
    private Authenticator.EcDaaSignature cert = null;
    public int status = 0;
    
    
    
    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getM() {
        return M;
    }

    public void setM(String M) {
        this.M = M;
    }
    
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public BNCurve getCurve() {
        return curve;
    }

    public void setCurve(BNCurve curve) {
        this.curve = curve;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }
    public GetCertTask(){
        
        
    }
    public boolean isOnline(){
        try {
            HttpURLConnection connection = (HttpURLConnection) address.openConnection();
            connection.disconnect();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(GetCertTask.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }

    public URL getAddress() {
        return address;
    }

    public void setAddress(URL address) {
        this.address = address;
    }
    @Override
    public Map<String, String> call(){
        boolean success = true;
        updateMessage("Starting to join to issuer");
        joinApiHandler = new JoinApiHandler(this);
        joinApiHandler.setAppId(appId);
        joinApiHandler.setM(M);
        joinApiHandler.processApi();
        success &= (joinApiHandler.getIpkJSON() != null);
        success &= (joinApiHandler.getCurveName()!= null);
        if(success){
            try{
            curve = BNCurve.createBNCurveFromName(joinApiHandler.getCurveName());
            Issuer.IssuerPublicKey ipk = new Issuer.IssuerPublicKey(curve,joinApiHandler.getIpkJSON());
            authenticator = new Authenticator(curve, ipk );
            nonce = new BigInteger(joinApiHandler.getNonceString());
            gsk = authenticator.getSk();
                    }catch(Exception e){
                        success = false;
                        updateMessage("Invalid data in join step");
                    }
            
                            
        }
        if(success){
        updateMessage("Starting to request credential for "+ basename);
        jm1ApiHandler = new Jm1ApiHandler();
        jm1ApiHandler.setRemoteIssuer(this);
        jm1ApiHandler.setField(fieldName);
        try{
        jm1 = authenticator.EcDaaJoin1(nonce);
        jm1ApiHandler.setJm1(jm1);
        
        }catch(Exception e){
            updateMessage("Invalid credential");
            success = false;
        }
        if(success){
            jm1ApiHandler.processApi();
        }else{
            updateMessage("Recieve invalid nonce");
            status = JOIN_FAIL;
            return null;
        }
        
        }
        else{
            updateMessage("Fail to join");
            status = JOIN_FAIL;
            return null;
        }
        
        success &= (jm1ApiHandler.getJm2JSON() != null);        
        
        if(success){
            updateMessage("Starting to request certificate for "+basename);
            try {
                jm2 = new Issuer.JoinMessage2(curve, jm1ApiHandler.getJm2JSON());
                authenticator.EcDaaJoin2(jm2);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(GetCertTask.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
                status = JM1_FAIL;
                updateMessage("Invalid credential");
                return null;
            }
            certApiHandler = new CertApiHandler();
            certApiHandler.setRemoteIssuer(this);
            certApiHandler.setJm1(jm1);
            certApiHandler.setBasename(basename);
            certApiHandler.setJm2(jm2);
            certApiHandler.setGsk(gsk);
            certApiHandler.setMessage(data);
            certApiHandler.processApi();
        }
        else{
            status = JM1_FAIL;
            updateMessage("Fail to get credential");
            return null;
        }
        success &= (certApiHandler.getCertJSON() != null);
        try{
        cert = new Authenticator.EcDaaSignature(DirtyWork.hexStringToByteArray(certApiHandler.getCertJSON()),
                data.getBytes(), curve);
        }catch(Exception e){
            status = JM1_FAIL;
            updateMessage("Invalid credential");
        }
        success &= (cert != null);
        if(!success) {
            status = CERT_FAIL;
            updateMessage("Invalid credential");
            return null;
        }
        else{
            updateMessage("Success : "+ basename );
            Map<String, String> map = new HashMap<String, String>();
            map.put(ApiFormat.CERT,certApiHandler.getCertJSON() );
            map.put(ApiFormat.GSK, gsk.toString());
            map.put(ApiFormat.CREDENTIAL,jm1ApiHandler.getJm2JSON());
            map.put(ApiFormat.FIELD, basename);
            
            return map;
        }
    }


}

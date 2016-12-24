/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Tools;

import identitycard2.Config.ConfigParser;
import identitycard2.DirtyWork;
import identitycard2.HttpServer.SessionHandler;
import identitycard2.IdentityCard2;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Issuer;
import identitycard2.JoinApi.ApiFormat;
import identitycard2.Models.Verifier;
import identitycard2.RequestTask.ReceivedInfoOberservable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import org.json.JSONObject;
import identitycard2.crypto.AESEncryptor;
import identitycard2.crypto.BNCurve;
import identitycard2.crypto.BitKeySelector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.control.TableRow;
import org.json.JSONException;
/**
 *
 * @author nguyenduyy
 */
public class Data extends Observable{
      private String IV = "0123456789abcdef";

    
    private ArrayList<Field> fields;
    private ArrayList<String> TAGS ;
    private String esk;
    private String ipk;
    
    
    private String appId , curve;
    private BNCurve BNCurve = null;
    
    private File file = null;
    private Application application = null;
    private byte[] decryptKey = null;
    
    public final String MESSAGE = "message";
    public final String SIG = "sig";
    public final String CERT = "cert";
    
    //verify data
    ReceivedInfoOberservable receiveInfo = null;

    @Override
    protected synchronized void setChanged() {
        super.setChanged(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o); //To change body of generated methods, choose Tools | Templates.
    }
  

    public String getCurve() {
        return curve;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }
    
    
    public static Data mInstance = null;

    public byte[] getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(byte[] decryptKey) {
        this.decryptKey = decryptKey;
    }
    public static Data getInstance(Application application){
        if(mInstance == null){
            mInstance = new Data(application);
        }
        return mInstance;
    }
    public static Data getInstance(){
        if(mInstance ==null){
            return null;
        }
        return mInstance;
    }
    public static void reset(){
        mInstance = null;
    }
    private Data(Application application){
        TAGS = new ArrayList<>();
        fields = new ArrayList<>();
        
        this.application = application;
        try {
            
            File mainfile = new File(IdentityCard2.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(mainfile.getParent()+"/data/data");
        } catch (URISyntaxException ex) {
            Logger.getLogger(Shadow.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                Files.createFile(file.toPath());
            } catch (IOException ex) {
                Logger.getLogger(Shadow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            
        }
    }
    public String toJSON(){
        JSONObject json = new JSONObject();
        
        try {
            
            json.put("appId", appId);
            json.put("curve", curve);
            //json.put("esk", esk);
            json.put("ipk",ipk);
            for(Field f : fields){
                json = f.putToJSONObject(json);
            }
            return json.toString();
        } catch (JSONException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    public boolean parseFromJSON(JSONObject json){
        try {
            
            appId =json.getString("appId");
            curve = json.getString("curve");
            
            ipk = json.getString("ipk");
            //esk = json.getString("esk");
            Iterator is = json.keys();
            do{
                String k = (String) is.next();
                if(k.indexOf("level") == 0){
                    TAGS.add(k);
                    Field f=  new Field(k);
                    f.setEpk(json.getString(f.getTAG_epk()));
                    f.setEsk(json.getString(f.getTAG_esk()));
                    f.setValue(json.getString(k));
                    f.setCredential(json.getString(f.getTAG_credential()));
                    f.setGsk(json.getString(f.getTAG_gsk()));
                    fields.add(f);
                }
            }while(is.hasNext());
            //field permission
            String k = "permission";
            TAGS.add("permission");
            Field f=  new Field(k);
                    f.setEpk(json.getString(f.getTAG_epk()));
                    f.setEsk(json.getString(f.getTAG_esk()));
                    f.setValue(json.getString(k));
                    f.setCredential(json.getString(f.getTAG_credential()));
                    f.setGsk(json.getString(f.getTAG_gsk()));
                    fields.add(f);
            
            
        } catch (JSONException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    public void addField(String TAG){
        if(!TAGS.contains(TAG)){
        TAGS.add(TAG);
        fields.add(new Field(TAG));
        }
    }
    public void addGsk(String TAG, String gsk){
        int id = findFieldId(TAG);
        if(id >=0){
            Field f = fields.get(id);
            f.setGsk(gsk);
        }
    }
    
    public void addValue(String TAG, String value){
        int id = findFieldId(TAG);
        if(id >=0){
            Field f = fields.get(id);
            f.setValue(value);
        }
    }
    
    public void addCredential(String TAG, String cr){
        int id = findFieldId(TAG);
        if(id >=0){
            Field f = fields.get(id);
            f.setCredential(cr);
        }
    }
    public int findFieldId(String TAG){
        for(int i = 0; i< TAGS.size(); i++){
            String s = TAGS.get(i);
            if(s.equals(TAG)){
                return i;
            }
        }
        return -1;
    }
    public void readFromFile(){
        try {
            if(file != null && decryptKey != null){
            byte[] b = Files.readAllBytes(file.toPath());
            //String j = AESEncryptor.decrypt(BitKeySelector.getAES128Key(decryptKey), IV, b);
            parseFromJSON(new JSONObject(new String(b)));
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String getValueOfField(String field){
        Field f = fields.get(findFieldId(field));
        if(f!= null){
            return f.getValue();
        }
        else return null;
    }
    
    public String getCredentialOfField(String field){
        Field f = fields.get(findFieldId(field));
        if(f!= null){
            return f.getCredential();
        }
        else return null;
    }
    public String getGskOfField(String field){
        Field f = fields.get(findFieldId(field));
        if(f!= null){
            return f.getGsk();
        }
        else return null;
    }
    
    public boolean save(){
        String d = toJSON();
        byte[] b = AESEncryptor.encrypt(new String(decryptKey), IV, d);
        try {
            Files.write(file.toPath(),b );
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    public void update(Map<String,String> map){
        if(map != null){
        String field = map.get(ApiFormat.FIELD);
    
        addGsk(field, map.get(ApiFormat.GSK));
        addCredential(field, map.get(ApiFormat.CREDENTIAL));
        //notify data has changed
        setChanged();
        notifyObservers();
        }
        
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }

    public byte[] getEsk() {
        return DirtyWork.hexStringToByteArray(esk);
    }

    public void setEsk(byte[] esk) {
        this.esk = DirtyWork.bytesToHex(esk);
    }

    public ArrayList<String> getTAGS() {
        return TAGS;
    }

    public void setTAGS(ArrayList<String> TAGS) {
        this.TAGS = TAGS;
    }

    public String getIpk() {
        return ipk;
    }

    public void setIpk(String ipk) {
        this.ipk = ipk;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    public BNCurve getBNCurve(){
        if(BNCurve == null){
            BNCurve = BNCurve.createBNCurveFromName(curve);
            return BNCurve;
        }
        else{
            return BNCurve;
        }
    }
    public Issuer.IssuerPublicKey getIssuerPubicKey(){
        if(ipk != null){
            Issuer.IssuerPublicKey pk = new Issuer.IssuerPublicKey(getBNCurve(),ipk);
            return pk;
        }
        else return null;
        
    }
    public Authenticator getAuthenticator(String field){
        try{
            Issuer.IssuerPublicKey ipk = getIssuerPubicKey();
            BNCurve curve = getBNCurve();
            BigInteger sk = new BigInteger(getGskOfField(field));
            Authenticator a = new Authenticator(curve, ipk, sk);
            Issuer.JoinMessage2 jm2 = new Issuer.JoinMessage2(curve,getCredentialOfField(field) );
            a.EcDaaJoin2(jm2);
            return a;
        }catch(Exception e){
            return null;
        }
    }
    public JSONObject getJSONByField(String f){
        JSONObject json = new JSONObject();
          try {
              json.put(ApiFormat.VALUE,getValueOfField(f));
              //json.put(ApiFormat.SIG, getSigOfField(f)); //nym problem
    
              return json;
          } catch (JSONException ex) {
              Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
              return null;
          }
    
    }          



    public ReceivedInfoOberservable getReceiveInfo() {
        return receiveInfo;
    }

    public void setReceiveInfo(ReceivedInfoOberservable receiveInfo) {
        this.receiveInfo = receiveInfo;
        
    }
    public ArrayList<Info> collectionInfoFromData(){
        ArrayList<Info> ai = new ArrayList<Info>();
        for(Field f : fields){
            if(!f.getTAG().equals(ApiFormat.PERMISSION)){
                try {
                    JSONObject d_json = new JSONObject(f.getValue());
                    Iterator ite =d_json.keys();
                    while(ite.hasNext()){
                        String k =  (String) ite.next();
                        if(!k.equals("expire_date") && !hasKey(ai, k)){
                            
                            Info info = new Info();
                            info.setField(k);
                            info.setValue(d_json.getString(k));
                            ai.add(info);
                            
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        }
        return ai;
    }
    private boolean hasKey(ArrayList<Info> al, String key){
       for(Info i : al){
           if(i.getField().equals(key)) return true;
       }
       return false;
    }
    
}

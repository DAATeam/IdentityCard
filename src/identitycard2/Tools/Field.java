/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Tools;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenduyy
 */
public class Field {
    private String TAG ;
    private String value;
    private String gsk;
    private String TAG_gsk;

    private String credential;
    private String TAG_credential;

    private String esk , epk;
    private String TAG_esk, TAG_epk;
    public Field(String TAG){
        this.TAG = TAG;
        this.TAG_gsk = "gsk_"+TAG;

        this.TAG_credential = "credential_"+TAG;
        this.gsk="";

        this.credential="";
        this.value="";

        esk = "";TAG_esk = "esk_"+TAG;
        epk = "";TAG_epk = "epk_"+TAG;
        
        
    }

    public String getEsk() {
        return esk;
    }

    public void setEsk(String esk) {
        this.esk = esk;
    }

    public String getEpk() {
        return epk;
    }

    public void setEpk(String epk) {
        this.epk = epk;
    }

    public String getTAG_esk() {
        return TAG_esk;
    }

    public void setTAG_esk(String TAG_esk) {
        this.TAG_esk = TAG_esk;
    }

    public String getTAG_epk() {
        return TAG_epk;
    }

    public void setTAG_epk(String TAG_epk) {
        this.TAG_epk = TAG_epk;
    }


    public JSONObject putToJSONObject(JSONObject json){
        if(json != null){
            try {
                json.put(TAG,value);
                json.put(TAG_gsk, gsk);
                //json.put(TAG_cert,cert);
                json.put(TAG_credential, credential);
                json.put(TAG_epk, epk);
                json.put(TAG_esk, esk);
            } catch (JSONException ex) {
                Logger.getLogger(Field.class.getName()).log(Level.SEVERE, null, ex);
                return json;
            }
        }
        return json;
    }
    public static Field constructForTAG(String t, JSONObject json){
        if(json == null) return null;
        Field f = new Field(t);
        try {
            f.setValue(json.getString(f.getTAG()));
            f.setGsk(json.getString(f.getTAG_gsk()));
            //f.setCert(json.getString(f.getTAG_cert()));
            f.setCredential(json.getString(f.getTAG_credential()));
            f.setEpk(f.getTAG_epk());
            f.setEsk(f.getTAG_esk());
            return f;
        } catch (JSONException ex) {
            Logger.getLogger(Field.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    public void constructFromTAG(JSONObject json){
        try {
            setValue(json.getString(getTAG()));
            setGsk(json.getString(getTAG_gsk()));
            //setCert(json.getString(getTAG_cert()));
            setCredential(json.getString(getTAG_credential()));
            setEpk(json.getString(getTAG_epk()));
            setEsk(json.getString(getTAG_esk()));
            
        } catch (JSONException ex) {
            Logger.getLogger(Field.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }

    public String getTAG_gsk() {
        return TAG_gsk;
    }

    public void setTAG_gsk(String TAG_gsk) {
        this.TAG_gsk = TAG_gsk;
    }



    public String getTAG_credential() {
        return TAG_credential;
    }

    public void setTAG_credential(String TAG_credential) {
        this.TAG_credential = TAG_credential;
    }
    
    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGsk() {
        return gsk;
    }

    public void setGsk(String gsk) {
        this.gsk = gsk;
    }



    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
    
}


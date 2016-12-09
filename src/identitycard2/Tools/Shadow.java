/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2.Tools;

import identitycard2.IdentityCard2;
import identitycard2.crypto.MD5Helper;
import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;

import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;

/**
 *
 * @author nguyenduyy
 */
public class Shadow {
    private final String TAG = "Shadow : ";
    
    private Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    private byte[] value;
    private File file;
    public Shadow(Application application){
        this.application = application;
        try {
            
            File mainfile = new File(IdentityCard2.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(mainfile.getParent()+"/data/shadow");
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
               
    
    
    public boolean createShadow(String pin){
        if(pin != null){
        byte[] h = MD5Helper.hashStringToByte(pin);
            try {
                Files.write(file.toPath(), h);
            } catch (IOException ex) {
                Logger.getLogger(Shadow.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
        
        
    }
    public byte[] getShadow(){
        if(file != null && file.exists()){
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException ex) {
                Logger.getLogger(Shadow.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        else return null;
    }
    
    
}
    
    
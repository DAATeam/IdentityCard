/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2;

import identitycard2.Config.ConfigParser;
import identitycard2.HttpServer.MyHttpServer;
import identitycard2.JoinApi.GetCertTask;
import identitycard2.Tools.Data;
import identitycard2.VerifyApi.verifyApiHandler;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author nguyenduyy
 */
public class MainController implements Initializable, Observer {
    private final String UNTRUSTED = "Not available";
    private final String TRUSTED=  "Available";
    @FXML
    private  Label txt_user_name;
    @FXML 
    private  Label txt_user_job;
    @FXML
    public  Label txt_user_name_trust;
    @FXML
    public  Label txt_user_job_trust;
    @FXML
    public volatile Label txt_status;
    @FXML 
    private Label txt_remote_issuer;
    @FXML
    private Label label_service_name;
    @FXML
    private Label label_service_permission;
    @FXML
    private Label label_service_name_trust;
    @FXML
    private Label label_service_per_trust;
    @FXML 
    private TextField txt_verify_url ;
    @FXML 
    private Button btn_verify;
    @FXML private Button btn_online;
    
    private Application application = null;
     private Data data = null;
     
     private GetCertTask remoteIssuer = null;
     private ConfigParser configParser = null;
     
     private MyHttpServer myHttpServer = null;
     private boolean isOnline = false;

     @Override
    public void initialize(URL url, ResourceBundle rb) {
        configParser = ConfigParser.getInstance();
        remoteIssuer = configParser.getRemoteIssuer();
        myHttpServer = MyHttpServer.getInstance();
        
        if(remoteIssuer != null){
            try {
                txt_remote_issuer.setText(remoteIssuer.getAddress().toURI().toString());
            } catch (URISyntaxException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                txt_remote_issuer.setText("Parse error");
           }
        }
        btn_verify.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                processVerify();            
            }
        });
        btn_online.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event){
                handleBtnOnline();
            }
        });
    }
    private void processVerify(){
        try{
            URL u = new URL(txt_verify_url.getText());
            verifyApiHandler v = new verifyApiHandler();
           v.setUrl(u);
           v.execute();
        }catch(Exception e){
            return;
        }
        
    }
    public void updateView(){
        
        try {
            JSONObject jj =new JSONObject(data.getValueOfField("user_job"));
            JSONObject jn =new JSONObject(data.getValueOfField("user_name"));
            txt_user_name.setText(jn.getString("user_name"));
        txt_user_job.setText(jj.getString("user_job"));
        } catch (JSONException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //nees to  move to a thread
                    
        
        if(data.getEsk() == null){
            
        }
        else{
            
        }
        if(isNoData(data.getCertOfField("user_name"))){
            txt_user_name_trust.setText(UNTRUSTED);
            JSONObject json = new JSONObject();
            try {
                json.put("user_name", data.getValueOfField("user_name"));
            } catch (JSONException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                json = null;
            }
            if(json != null){
                 requestCert(new String(data.getDecryptKey()),
                    data.getAppId(), "user_name", "user_name",json.toString() );
            }
           
            
        }else{
            txt_user_name_trust.setText(TRUSTED);
        }
        if(isNoData(data.getCertOfField("user_job"))){
            txt_user_job_trust.setText(UNTRUSTED);
             JSONObject json = new JSONObject();
            try {
                json.put("user_job", data.getValueOfField("user_job"));
            } catch (JSONException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                json = null;
            }
            if(json != null){
                 requestCert(new String(data.getDecryptKey()),
                    data.getAppId(), "user_job", "user_job",json.toString() );
            }
        }
        else{
            txt_user_job_trust.setText(TRUSTED);
        }
        
    }
    private boolean isNoData(String s){
        if(s == null || s.equals("")){
            return true;
        }
        else return false;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
        data.addObserver(this);
        updateView();
        
    }
    private void requestCert(String M, String appId, String basename, String field, String message){
        GetCertTask task = configParser.getRemoteIssuer();
        task.setAppId(appId);
        task.setBasename(basename);
        task.setFieldName(field);
        task.setM(M);
        
        txt_status.textProperty().bind(task.messageProperty());
        task.setData(message);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle(WorkerStateEvent t){
                Data.getInstance().update(task.getValue());
            }
        });
        Thread th = new Thread(task);
         th.setDaemon(true);
         th.start();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        updateView();
    }
    public void handleBtnOnline(){
        if(isOnline){
            
            try{
            myHttpServer.stop();
            btn_online.setText("Go Online");
            }catch(Exception e){
                
            }
        }
        else{
            
            myHttpServer.start();
            btn_online.setText("Go Offline");
        }
    }
   
    


    
    
}

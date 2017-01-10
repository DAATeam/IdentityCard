/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2;

import identitycard2.Config.ConfigParser;
import identitycard2.HttpServer.MyHttpServer;
import identitycard2.HttpServer.SessionData;
import identitycard2.HttpServer.SessionHandler;
import identitycard2.JoinApi.ApiFormat;
import identitycard2.JoinApi.GetCertTask;
import identitycard2.Models.Authenticator;
import identitycard2.Models.Verifier;
import identitycard2.RequestTask.RequestNonTask;
import identitycard2.RequestTask.RequestTask;
import identitycard2.Tools.Data;
import identitycard2.Tools.Field;
import identitycard2.Tools.Info;
import identitycard2.Tools.Permission;
import identitycard2.VerifyOld.ApiRequester;
import identitycard2.VerifyOld.verifyApiHandler;
import java.awt.GridLayout;

import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
    public TableView table_info;
    @FXML
    public TableView table_receive;
    @FXML
    public TableView table_permission;
    @FXML
    public TableView table_level;
    @FXML
    public Label txt_isTrust;
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
        initTableView();
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
            
            String h = txt_verify_url.getText();
            RequestTask rt= new RequestTask();
            //txt_status.textProperty().bind(rt.messageProperty());
            rt.setHost(h);
            rt.setCurve(Data.getInstance().getBNCurve());
            rt.setIpk(Data.getInstance().getIssuerPubicKey());
            rt.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    updateTabVerify();
                }
            });
            rt.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                   
                }
            });
            Thread t = new Thread(rt);
            
            t.start();
            //rt.call();
            
           
        }catch(Exception e){
            Logger.getLogger(ApiRequester.class.getName()).log(Level.SEVERE, null, e);
            return;
        }
        
    }
    public void updateView(){
        
        //show info 
        ArrayList<Info> ai = Data.getInstance().collectionInfoFromData();
            ObservableList<Info> oli = FXCollections.observableArrayList(ai);
           table_info.setItems(oli);
        
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
        updateTablePermission();
        updateTableLevel();
    }
  
    @Override
    public void update(Observable o, Object arg) {
        updateView();
        
    }
    public void handleBtnOnline(){
        if(isOnline){
            isOnline = false;
            try{
            myHttpServer.stop();
            btn_online.setText("Go Online");
            
            }catch(Exception e){
                
            }
        }
        else{
            
            myHttpServer.start();
            btn_online.setText("Go Offline");
            isOnline = true;
        }
    }
    private void updateTabVerify(){
        //verify tab 
        
        if(data.getReceiveInfo() != null){
            JSONObject j = data.getReceiveInfo().response;
            try {
                if(j.getString(ApiFormat.STATUS).equals(ApiFormat.OK)){
                    String m = j.getString("information");
                addInfoToTableView(table_receive, fromStringJSONToArrayList(m));
                if(isReceiveInfoTrusted(j)){
                    txt_isTrust.setText("Can Trust");
                }else txt_isTrust.setText("Not Trust");
                }
                else{
                    txt_isTrust.setText(j.getString(ApiFormat.MESSAGE));
                }
                
                
            } catch (JSONException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                
            }
            
    }
    }
    private ArrayList<Info> fromStringJSONToArrayList(String m){
        ArrayList<Info> ai = new ArrayList<Info>();
        try {
            JSONObject json = new JSONObject(m);
            Iterator ite = json.keys();
            while(ite.hasNext()){
                String k = (String) ite.next();
                Info info =new Info();
                info.setField(k);
                info.setValue(json.getString(k));
                ai.add(info);
            }
            return ai;
        } catch (JSONException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    private void addARowOfInfoToView(Field f){
        
    }
       private void addInfoToTableView(TableView tv ,ArrayList<Info> infos){
           ObservableList<Info> oli = FXCollections.observableArrayList(infos);
           tv.setItems(oli);
    }
       private void initTableView(){
        //Table info
           table_info.setEditable(true);
       
        TableColumn fieldColumn = new TableColumn("Field");
        fieldColumn.setCellValueFactory(new PropertyValueFactory<Info,String>("field_text"));

        TableColumn infoColumn = new TableColumn("Information");
        infoColumn.setCellValueFactory(
            new PropertyValueFactory<Info,String>("value")
        );

        TableColumn statusCOlumn = new TableColumn("Status");
        
        statusCOlumn.setCellValueFactory(
            new PropertyValueFactory<Info,String>("status")
        );
        table_info.getColumns().addAll(fieldColumn, infoColumn, statusCOlumn);
        
        //table received info
        TableColumn fieldColumn1 = new TableColumn("Field");
        fieldColumn1.setCellValueFactory(new PropertyValueFactory<Info,String>("field_text"));

        TableColumn infoColumn1 = new TableColumn("Information");
        infoColumn1.setCellValueFactory(
            new PropertyValueFactory<Info,String>("value")
        );

        TableColumn statusCOlumn1 = new TableColumn("Status");
        
        statusCOlumn1.setCellValueFactory(
            new PropertyValueFactory<Info,String>("status")
        );
        table_receive.setEditable(true);
       
        
        table_receive.getColumns().addAll(fieldColumn1, infoColumn1, statusCOlumn1);
        //table Permission
        TableColumn to_col =  new TableColumn("Request To");
        to_col.setCellValueFactory(new PropertyValueFactory<Permission,String>("member_type_text"));
        TableColumn level_col = new TableColumn("Request Level");
        level_col.setCellValueFactory(new PropertyValueFactory<Permission,String>("level"));
        table_permission.setEditable(true);
        table_permission.getColumns().addAll(to_col,level_col);
        //table Level
        TableColumn from_col = new TableColumn("Level name");
        from_col.setCellValueFactory(new PropertyValueFactory<Level,String>("level_name"));
        TableColumn f_col = new TableColumn("W.r.t  Fields");
        f_col.setCellValueFactory(new PropertyValueFactory<Level,String>("fields"));
        table_level.setEditable(true);
        table_level.getColumns().addAll(from_col,f_col);
       }
       private boolean isReceiveInfoTrusted(JSONObject response){
        Verifier v = new Verifier(Data.getInstance().getBNCurve());
        boolean b = false;
          try {
              String info = response.getString("information");
              byte[] sig_b = DirtyWork.hexStringToByteArray(response.getString(ApiFormat.SIG));
              
              SessionData sd = SessionHandler.getInstance().getSessionByOfPartner(txt_verify_url.getText());
              String lsid = sd.getSessionId();
              Authenticator.EcDaaSignature sig = new Authenticator.EcDaaSignature(sig_b, lsid.getBytes(), Data.getInstance().getBNCurve());
              String basename = "verification";
                         
              b = v.verifyWrt(info.getBytes(),lsid.getBytes(),sig,basename,Data.getInstance().getIssuerPubicKey(), null);
              synchronized(this){
              SessionHandler.getInstance().removeSessionBySessionID(sd.getSessionId());
              }
             return b;
              
              
          } catch (Exception ex) {
              
              Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
              return b;
          }
        
        
        
    }
       public void updateTablePermission(){
            ArrayList<Permission> ap = Data.getInstance().getAllPermission();
            ObservableList<Permission> op = FXCollections.observableArrayList(ap);
            table_permission.setItems(op);
        }
       public void updateTableLevel(){
           ArrayList<identitycard2.Tools.Level> ap = Data.getInstance().getAllLevelInfo();
            ObservableList<identitycard2.Tools.Level> op = FXCollections.observableArrayList(ap);
            table_level.setItems(op);
       }
}   
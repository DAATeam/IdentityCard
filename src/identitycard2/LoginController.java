package identitycard2;

import identitycard2.Tools.Data;
import javafx.application.Application;
import identitycard2.Tools.Shadow;
import identitycard2.crypto.MD5Helper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController implements Initializable {
    private final String TAG = "LoginController : ";
    @FXML
    private PasswordField tf_pin;
    private enum EnumLoginMode  {USE_PIN, USE_FINGER, USE_FACE};
    @FXML
    private CheckBox cb_pin, cb_finger, cb_face;
    @FXML
    private Button btn_login;
    
    private Application application;
    
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        btn_login.setOnMouseClicked(new EventHandler<MouseEvent> (){
            @Override
            public void handle(MouseEvent event) {
                processLogin();            
            }
            
        });
        
    }
    private void processLogin() {
        Shadow shadow = new Shadow(application);
        byte[] h = shadow.getShadow();
        if(h != null){
            
            byte[] input = MD5Helper.hashStringToByte(tf_pin.getText());
            if(Arrays.equals(h, input)){
                openMainWindow();                
                
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login error");
                alert.setContentText("Wrong identity : \n"
                        );
                alert.showAndWait();
                               

            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login error");
                alert.setContentText("Null file Exception");
                alert.showAndWait();
        }
    }
    public void setApplication(Application a){
        this.application = a;
    }
    private void openMainWindow(){
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader(application.getClass().getResource("MainFXMLDocument.fxml"));
        Parent root;
        try {
            root = loader.load();
            MainController mainController = (MainController) loader.getController();
            mainController.setApplication(application);
            Data data = Data.getInstance(application);
            data.setDecryptKey( tf_pin.getText().getBytes());
            data.readFromFile();
            mainController.setData(data);
            
            Scene scene = new Scene(root);
        
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.setScene(scene);
        newStage.show();

        } catch (IOException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

}
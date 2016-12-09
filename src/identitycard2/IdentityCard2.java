/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package identitycard2;

import identitycard2.Tools.Data;
import identitycard2.Tools.Shadow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author nguyenduyy
 */
public class IdentityCard2 extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginFXMLDocument.fxml"));
        Parent root = loader.load();
        LoginController loginController = (LoginController) loader.getController();
        loginController.setApplication(this);
        Shadow shadow = new Shadow(this);
        shadow.createShadow("daa");
        Data data = Data.getInstance(this);
        addDemoData(data);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    private void addDemoData(Data data){
        data.setAppId("1");
        data.setCurve("");
        data.setDecryptKey("daa".getBytes());
        data.setEsk(null);
        data.setIpk("");
        data.addField("user_name");
        data.addValue("user_name", "Duyy");
        data.addField("user_job");
        data.addValue("user_job", "Boss");
        data.save();
        Data.reset();
        
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chatclient;

import java.awt.Rectangle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author DENGUN-WATIB
 */
public class ChatClient extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
      
        root.setStyle("-fx-border-insets: 23;\n" +
"\n" +
"  -fx-background-insets: 23;\n" +
"\n" +
"  -fx-background-radius: 6;\n" +
"\n" +
"  -fx-border-radius: 6;\n" +
"\n" +
"  -fx-border-color: gray;\n" +
"\n" +
"  -fx-border-style: solid;\n" +
"\n" +
"  -fx-border-width: 1;\n" +
"\n" +
"  -fx-effect: dropshadow(three-pass-box, #5E62D6;"); 
        root.setStyle("");  

       
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
         //stage.initStyle(StageStyle.TRANSPARENT);
        
        Image icon = new Image(getClass().getResourceAsStream("comment.jpg"));
        stage.getIcons().add(icon);
        
        
        //stage.getIcons().add(getClass().getResource("comment.jpg"));
        stage.setTitle("Distributed Client Program");
        stage.setResizable(false);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        launch(args);
    }
    
}

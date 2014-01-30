
package mailprogram;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MailProgram extends Application {
    public static void validatePassword(String password) {
        if (password.equals("password")) {
            FXMLFramework.setFXML("EmailScreen");
            FXMLFramework.setResizable(true);
        }
    }
    public void start(Stage stage) {
        FXMLFramework.setMainStage(stage);
        FXMLFramework.setTitle("Email Program");
        //FXMLFramework.setResizable(false);
        FXMLFramework.loadFXML("PasswordScreen");
        FXMLFramework.loadFXML("EmailScreen");
        FXMLFramework.loadFXML("AddUser");
        FXMLFramework.loadFXML("MessageScreen");
        //FXMLFramework.setFXML("PasswordScreen");
        FXMLFramework.setFXML("EmailScreen");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                FXMLFramework.getMainStage().hide();
                Mail.closeConnections();
            }
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
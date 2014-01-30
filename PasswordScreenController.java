
package mailprogram;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class PasswordScreenController implements Initializable {
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    
    public void initialize(URL url, ResourceBundle rb) {
        EventHandler<ActionEvent> submit = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                MailProgram.validatePassword(passwordField.getText());
            }
        };
        loginButton.setOnAction(submit);
        passwordField.setOnAction(submit);
    }
}

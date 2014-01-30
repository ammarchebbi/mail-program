
package mailprogram;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;


public class AddUserController implements Initializable {
    
    @FXML TextField usernameField;
    @FXML PasswordField passwordField;
    @FXML TextField incomingField;
    @FXML TextField outgoingField;
    @FXML ChoiceBox incomingProtocol;
    @FXML ChoiceBox outgoingProtocol;
    @FXML Button addButton;
    @FXML Button closeButton;
    
    public void clearForm() {        
        usernameField.clear();
        passwordField.clear();
        incomingField.clear();
        outgoingField.clear();
        incomingProtocol.setValue("pop3");
        outgoingProtocol.setValue("smtp");
    }
    
    public void registerUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (!Mail.openMailIncoming(incomingProtocol.getValue().toString(), incomingField.getText(), username, password)) return;
        if (!Mail.openMailOutgoing(outgoingProtocol.getValue().toString(), outgoingField.getText(), username, password)) return;
        FXMLFramework.closeWindow("Add User");
        clearForm();
        EmailScreenController.addUserToTree(username);
        EmailScreenController.saveUser(username);
    }
    
    public void initialize(URL url, ResourceBundle rb) {
        incomingProtocol.getItems().setAll("pop3", "pop3s", "imap", "imaps");        
        incomingProtocol.setValue("pop3");
        outgoingProtocol.getItems().setAll("smtp");        
        outgoingProtocol.setValue("smtp");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                registerUser();
            }
        });
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FXMLFramework.closeWindow("Add User");
            }
        });
    }    
}


package mailprogram;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;

public class MessageScreenController implements Initializable {

    @FXML private static TextField toField;
    @FXML private static TextField subjectField;
    @FXML private static HTMLEditor msgContent;
    @FXML private static Button sendButton;
    private static Message message = null;
    
    private void setupButtons() {
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                String htmlText = msgContent.getHtmlText().replace("<body contenteditable=\"true\">", "<body>");
                if (message != null) {
                    Mail.replyTo(message, htmlText);
                    FXMLFramework.closeWindow("Compose Message");
                    return;
                }

                String[] to = toField.getText().split(";");
                InternetAddress[] recipients = new InternetAddress[to.length];
                int index = 0;
                for (String i : to) {
                    try {
                        recipients[index] = new InternetAddress(i.trim());
                    } catch (Exception e) {
                        
                    }
                    index++;
                }                
                Mail.sendTo(recipients, subjectField.getText(), htmlText);
                FXMLFramework.closeWindow("Compose Message");
            }
        });
    }
    public static void setReply(Message msg) {
        if (msg == null) return;
        try {
            toField.setText(msg.getReplyTo()[0].toString());
            toField.setDisable(true);
            subjectField.setText("Re: " + msg.getSubject());
            subjectField.setDisable(true);
            message = msg;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error setting reply!\n" + e);
        }
    }
    public static void clearForm() {
        toField.setText("");
        toField.setDisable(false);
        subjectField.setText("");
        subjectField.setDisable(false);
        msgContent.setHtmlText("");
        message = null;
    } 
    
    public void initialize(URL url, ResourceBundle rb) {
        setupButtons();
    }
}

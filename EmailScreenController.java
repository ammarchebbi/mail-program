
package mailprogram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.swing.JOptionPane;

public class EmailScreenController implements Initializable {
    @FXML private static ListView<Message> mailList;
    @FXML TreeView<String> folderTree;
    @FXML WebView msgView;
    @FXML Button addUser, composeMsg, replyMsg, deleteMsg, flagMsg, downloadMsg;
    @FXML SplitPane mailTablePane;
    private static TreeItem<String> rootItem = new TreeItem<>("Profiles");

    public static String getPath() {
        String path = MailProgram.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = path;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String absolutePath = decodedPath.substring(0, decodedPath.lastIndexOf("/"))+"\\";
        return absolutePath;
    }
    private static File path = new File(getPath());

    private void setButtons() {
        addUser.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                openAddUserPrompt();
            }            
        });
        composeMsg.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                openMessagePrompt(null);
            }
        });
        replyMsg.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                Message msg = mailList.getSelectionModel().getSelectedItems().get(0);
                if (msg == null) return;
                openMessagePrompt(msg);
            }
        });
        deleteMsg.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                MultipleSelectionModel<Message> mailListSM = mailList.getSelectionModel();
                ObservableList<Message> selectedItems = mailListSM.getSelectedItems();
                ObservableList<Message> items = mailList.getItems();
                for (Message i : selectedItems) {
                    try {
                        i.setFlag(Flags.Flag.DELETED, true);
                        items.remove(i);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Failed to delete messages!");
                    }
                }
                mailListSM.clearAndSelect(0);
                updateMailList();
            }
        });
        flagMsg.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                MultipleSelectionModel<Message> mailListSM = mailList.getSelectionModel();
                ObservableList<Message> selectedItems = mailListSM.getSelectedItems();
                for (Message i : selectedItems) {
                    try {
                        i.setFlag(Flags.Flag.FLAGGED, !i.isSet(Flags.Flag.FLAGGED));
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Failed to flag messages!");
                    }
                }
                updateMailList();
            }
        });
        /*downloadMsg.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                MultipleSelectionModel<Message> mailListSM = mailList.getSelectionModel();
                ObservableList<Message> selectedItems = mailListSM.getSelectedItems();
                for (Message i : selectedItems) {
                    try {
                        downloadMessage(i);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Failed to download messages!\n" + e);
                        break;
                    }
                }
            }
        });*/
    }
    
    private class Cell extends ListCell<Message> {
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        Label sender = new Label("");
        Label subject = new Label("");
        Label date = new Label("");
        Label flagged = new Label("");
        public Cell() {
            super();
            subject.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);   
            subject.setFont(Font.font(null, 18.0)); 
            sender.setTextOverrun(OverrunStyle.WORD_ELLIPSIS);      
            flagged.setTextFill(Paint.valueOf("#F00"));
            hbox.getChildren().addAll(sender, flagged);   
            vbox.getChildren().addAll(subject, hbox);         
        }
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty) {
                setGraphic(null);
            } else {
                //subject.setPrefWidth(mailList.getWidth() - 18);
                vbox.setPrefWidth(mailList.getWidth() - 24);
                try {
                    String subjectString = item.getSubject();                    
                    subject.setText(subjectString == null || subjectString.isEmpty() ? "No Subject" : subjectString);
                    sender.setText(item.getFrom()[0].toString());
                    if (item.isSet(Flags.Flag.FLAGGED)) {
                        flagged.setText("Flagged");
                        sender.setPrefWidth(mailList.getWidth() - 64);
                                        
                    } else {
                        flagged.setText("");
                        sender.setPrefWidth(mailList.getWidth());
                    }
                } catch (Exception e) {
                    sender.setText("Error");
                    subject.setText("Error");
                }
                setGraphic(vbox);
            }
        }        
    }
    private void updateMailList() { // Cheeky.
        ObservableList<Message> cells = mailList.getItems();
        mailList.setItems(null);
        mailList.setItems(cells);
    }    
    private void setMailList() {
        mailList.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>() {
           public ListCell<Message> call(ListView<Message> param) {
                return new Cell();
            }
        });
        mailTablePane.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                updateMailList();
            }
        });
        mailList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mailList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Message>() {
            public void changed(ObservableValue<? extends Message> ov, Message t, Message t1) {
                if (t1 == null || mailList.getSelectionModel().getSelectedItems().size() > 1) return;
                msgView.getEngine().loadContent(Mail.getMsgText(t1));
            }
        });
    }
    private static void downloadMessage(Message message) throws Exception {
        System.out.println(message.getReceivedDate().toString());
        File file = new File(path, Mail.active_username + "/" + message.getFolder().getFullName() + "/" + message.getReceivedDate().toString().replace(":", "-") + ".mail");
        file.createNewFile();
        String encrypted = AESencrp.encrypt(message.getSubject());
        FileOutputStream output = new FileOutputStream(file);
        output.write(encrypted.getBytes());
        output.close();
    }
    private static void generateMailList(String folderName) {
        ObservableList<Message> items = mailList.getItems();
        Message[] messages = Mail.getMessages(folderName);
        if (messages == null) return;
        items.clear();
        for (int i = messages.length - 1; i >= 0; --i) {
            try {
                items.add(messages[i]);
            } catch (Exception e) {
                System.out.println("Error getting message.");
            }            
        }
        /*for (Message i : messages) {
            try {
                items.add(i);
            } catch (Exception e) {
                System.out.println("Error getting message.");
            }
        }*/
    }
    private static void addFolderToBranch(TreeItem<String> branch, Folder i) {
        try {
            if (!Mail.holdsMessages(i)) { // If folder holds folders ...
                TreeItem<String> folderBranch = new TreeItem<>(i.getName());
                for (Folder j : i.list()) {
                    addFolderToBranch(folderBranch, j);
                }
                branch.getChildren().add(folderBranch);
            }
            else {
                branch.getChildren().add(new TreeItem<>(i.getName()));
            }
            File dir = new File(path, Mail.active_username + "/" + i.getFullName());
            dir.mkdirs();            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error Branch!");
        }
    }
    public static void addUserToTree(String username) {
        Mail.setActiveUser(username);
        TreeItem<String> userBranch = new TreeItem<>(username);
        try {
            for (Folder i : Mail.active_inbox.list()) {
                addFolderToBranch(userBranch, i);
                System.out.println(username + "/" + i.getName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error adding new account to tree!");
        }
        rootItem.getChildren().add(userBranch);        
    }
    private void setFolderTree() {
        rootItem.setExpanded(true);
        folderTree.setRoot(rootItem);
        folderTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            private String getUser(TreeItem<String> item) {
                if (item == rootItem) return null;
                while (item.getParent() != rootItem) {
                    item = item.getParent();
                }
                return item.getValue();
            }
            private String getPath(TreeItem<String> item) {
                String string = new String();
                while (item.getParent() != rootItem || Mail.userExists(item.getParent().getValue())) {
                    string+= item.getValue() + "/";
                    item = item.getParent();
                }
                return (string.length() > 0 ? string.substring(0, string.length() - 1) : null);
            }
            public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue, TreeItem<String> newValue) {
                TreeItem<String> selectedItem = newValue;
                String user = getUser(selectedItem);
                if (user == null) return;
                Mail.setActiveUser(user);
                String path = getPath(selectedItem);
                if (path == null) return;
                generateMailList(path);
                mailList.getSelectionModel().clearAndSelect(0);
            }
        });
    }
    
    private void openAddUserPrompt() {
        if (FXMLFramework.hasWindow("Add User")) return;
        FXMLFramework.addWindow("Add User", "AddUser", true, true);
    }
    private void openMessagePrompt(Message message) {
        if (FXMLFramework.hasWindow("Compose Message")) return;
        FXMLFramework.addWindow("Compose Message", "MessageScreen", true, true).setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                MessageScreenController.clearForm();
            }
        });        
        MessageScreenController.setReply(message);
    }
    
    public void initialize(URL url, ResourceBundle rb) {
        setFolderTree();
        setMailList();
        setButtons();
        
    }
    public static void saveUser(String username) {
        //System.out.println(Mail.mail_incoming.get(username).getProperties());
        //System.out.println(Mail.mail_outgoing.get(username).getProperties());
    }
    public void loadUsers() {
        
    }
}

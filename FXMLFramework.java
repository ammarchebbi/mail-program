
package mailprogram;

import java.util.HashMap;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXMLFramework {
    private static HashMap<String, FXMLLoader> fxmlFiles = new HashMap<>();
    private static HashMap<String, Scene> fxmlScenes = new HashMap<>();
    private static HashMap<String, Stage> windows = new HashMap<>();
    private static Stage mainStage;
    private static Scene mainScene;
    
    public static Stage getMainStage() { 
        return mainStage;
    }
    public static Scene getMainScene() {
        return mainScene;
    }
    public static Stage addWindow(String title, String fxml, boolean show, boolean resizable) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setResizable(resizable);
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                String title = ((Stage)t.getSource()).getTitle();
                windows.remove(title);
            }
        });
        setFXML(stage, fxml);
        windows.put(title, stage);
        if (show) stage.show();
        return stage;
    }
    public static void closeWindow(String title) {
        Stage stage = windows.get(title);
        if (stage == null) return;
        stage.close();
        windows.remove(title);
    }
    public static void closeWindows() {
        for(Stage i : windows.values()) {
            i.close();
        }
    }
    public static boolean hasWindow(String title) {
        return windows.containsKey(title);
    }
    public static Stage getWindow(String title) {
        return windows.get(title);
    }

    public static void setMainStage(Stage stage) {
        mainStage = stage;
        mainStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                closeWindows();
            }
        });
    } 
    public static void setResizable(boolean resizable) {
        mainStage.setResizable(resizable);
    }
    public static void setTitle(String title) {
        mainStage.setTitle(title);
    }
    
    private static Scene setFXML(Stage stage, String fxml) {
        Scene scene = null;
        try {
            scene = fxmlScenes.get(fxml);
            if (scene == null) {                                         // Check if we've already loaded the FXML layout before.
                scene = new Scene((Parent)fxmlFiles.get(fxml).load());   // If we haven't loaded the FXML layout before, load it. 
                fxmlScenes.put(fxml, scene);                             // Then place the result into fxmlScenes. 
            }
            stage.setScene(scene);                                      // Set scene to desired FXML layout.
        }
        catch(Exception e) { // Catching an IOException causes the program to stop.
            System.out.println("Cannot load FXML \"" + fxml + "\" since it doesn't exist!");
        }
        return scene;
    }
    public static void setFXML(String fxml) {
        mainScene = setFXML(mainStage, fxml);
        mainStage.show();
    }
    public static FXMLLoader getFXML(String fxml) {
        return fxmlFiles.get(fxml);
    }
    public static void loadFXML(String fxml) {
        if (fxmlFiles.get(fxml) != null) { // Check if we've already loaded the FXML file before.
            System.out.println("Already loaded FXML \"" + fxml + "\"!");
            return;
        }
        fxmlFiles.put(fxml, new FXMLLoader(FXMLFramework.class.getResource(fxml + ".fxml")));
    }
}

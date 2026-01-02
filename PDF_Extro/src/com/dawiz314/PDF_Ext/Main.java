package PDF_Ext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application{

    @Override public void start(Stage primaryStage) throws Exception {
        FXMLLoader root = new FXMLLoader(Main.class.getResource("views/index.fxml"));
        AnchorPane ap = root.load(); // We are using an anchor pane just to force the borderpane to use all space

        Scene scene = new Scene(ap, 800, 600); // Default sizing for now
        scene.getStylesheets().add(getClass().getResource("css/index.css").toExternalForm());
        primaryStage.setTitle("PDF Grabber!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");

        System.out.println("Starting application!");
        launch(args);
    }
}


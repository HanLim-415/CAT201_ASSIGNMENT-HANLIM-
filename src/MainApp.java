import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Load the root node from FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        // Set the window title
        primaryStage.setTitle("Smart To-Do List");

        // Create a scene with the loaded layout
        Scene scene = new Scene(root, 1200, 808);

        // Attach the scene to the stage (window)
        primaryStage.setScene(scene);

        // Show the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}

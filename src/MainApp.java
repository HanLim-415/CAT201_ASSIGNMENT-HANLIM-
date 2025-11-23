import controller.MainController;
import javafx.application.Application;
import javafx.application.Platform; // <-- Make sure this import is here
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 1. Create a loader instance
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));

        // 2. Load the root node
        Parent root = loader.load();

        // 3. GET THE CONTROLLER
        MainController controller = loader.getController();

        // 4. Set the window title
        primaryStage.setTitle("Smart To-Do List");

        // 5. Create a scene
        Scene scene = new Scene(root, 1400, 700);

        // 6. Attach the scene
        primaryStage.setScene(scene);

        // 7. --- THIS IS THE CORRECTED EXIT LOGIC ---
        // This will run, save the tasks, and then
        // force the application to shut down.
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window is closing. Saving tasks...");
            controller.saveTasksOnExit(); // Call the save method

            // --- ADD THESE LINES BACK IN ---
            Platform.exit(); // Tells JavaFX to shut down
            System.exit(0);  // Tells the Java Virtual Machine to shut down
        });

        primaryStage.setResizable(true);

        // 8. Show the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}
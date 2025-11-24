import controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    // Define these at the CLASS level
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        primaryStage.setTitle("Smart To-Do List");

        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/app-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("App icon not found");
        }

        // 1. Transparent Window
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // 2. Larger Scene
        Scene scene = new Scene(root, 1440, 800);

        // 3. Transparent Fill
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);

        // 4. Dragging Logic
        // We use 'this' to be explicit, though it should work without it if scope is correct.
        root.setOnMousePressed(event -> {
            this.xOffset = event.getSceneX();
            this.yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - this.xOffset);
            primaryStage.setY(event.getScreenY() - this.yOffset);
        });

        // 5. Exit Logic
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window is closing. Saving tasks...");
            controller.saveTasksOnExit();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
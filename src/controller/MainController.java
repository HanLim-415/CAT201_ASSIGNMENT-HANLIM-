package controller;

// --- Imports ---
import javafx.application.Platform; // <-- 1. ADD THIS IMPORT
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Task;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;
import java.io.IOException;
import java.time.LocalDate;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.collections.transformation.FilteredList;
import util.DataManager;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for the main application view (MainView.fxml).
 * Handles the main TableView and button actions.
 */
public class MainController {

    private ObservableList<Task> tasks = DataManager.loadTasks();
    private FilteredList<Task> filteredTasks;
    private double xOffset = 0;
    private double yOffset = 0;

    // --- FXML Fields (unchanged) ---
    @FXML private Button addTaskButton;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Boolean> colStatus;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colPriority;
    @FXML private TableColumn<Task, String> colCategory;
    @FXML private TableColumn<Task, LocalDate> colDueDate;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ComboBox<String> filterPriorityCombo;
    @FXML private DatePicker filterDate;
    @FXML private Label totalTasksLabel;
    @FXML private Label dueTodayLabel;
    @FXML private Label overdueLabel;
    @FXML private Label upcomingLabel;
    @FXML private Label completedTasksLabel;

    @FXML
    private void initialize() {
        // --- Table Column Setup (unchanged) ---
        taskTable.setEditable(true);
        colStatus.setEditable(true);
        colStatus.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));
        colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        colPriority.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colDueDate.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());

        // --- Filter Setup (unchanged) ---
        filteredTasks = new FilteredList<>(tasks, p -> true);
        taskTable.setItems(filteredTasks);
        filterCategoryCombo.getItems().addAll("All Categories", "Work", "Personal", "School", "Home", "Other");
        filterCategoryCombo.getSelectionModel().select("All Categories");
        filterStatusCombo.getItems().addAll("All Status", "Completed", "Pending");
        filterStatusCombo.getSelectionModel().select("All Status");
        filterPriorityCombo.getItems().addAll("All Priority", "Low", "Medium", "High");
        filterPriorityCombo.getSelectionModel().select("All Priority");

        // --- Instant Filter Listeners (unchanged) ---
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        filterCategoryCombo.valueProperty().addListener((obs, old, val) -> applyFilters());
        filterStatusCombo.valueProperty().addListener((obs, old, val) -> applyFilters());
        filterDate.valueProperty().addListener((obs, old, val) -> applyFilters());
        filterPriorityCombo.valueProperty().addListener((obs, old, val) -> applyFilters());

        // Double-click listener (unchanged)
        taskTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && taskTable.getSelectionModel().getSelectedItem() != null) {
                handleViewDetails();
            }
        });

        tasks.addListener((ListChangeListener.Change<? extends Task> c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {

                    // Update the UI counters
                    Platform.runLater(() -> updateSummaryLabels());

                    // Save the changes to the file immediately!
                    DataManager.saveTasks(tasks);

                    break; // Stop checking this specific change event
                }
            }
        });

        // Call it once at the start to set the initial values
        updateSummaryLabels();
    }

    // --- Helper Method to Make Window Draggable ---
    private void makeDraggable(Parent root, Stage stage) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
    /**
     * Calculates and updates the summary labels in the sidebar.
     * (This method is unchanged, the logic here is correct)
     */
    private void updateSummaryLabels() {
        LocalDate today = LocalDate.now();
        long total = 0;
        long completed = 0;
        long dueToday = 0;
        long overdue = 0;
        long upcoming = 0;

        for (Task task : tasks) {
            total++;

            if (task.isCompleted()) {
                completed++;
            } else {
                if (task.getDueDate().isEqual(today)) {
                    dueToday++;
                } else if (task.getDueDate().isBefore(today)) {
                    overdue++;
                } else {
                    upcoming++;
                }
            }
        }

        totalTasksLabel.setText(String.valueOf(total));
        completedTasksLabel.setText(String.valueOf(completed));
        dueTodayLabel.setText(String.valueOf(dueToday));
        overdueLabel.setText(String.valueOf(overdue));
        upcomingLabel.setText(String.valueOf(upcoming));
    }

    // --- All other methods (handleAddTaskClick, handleViewDetails, etc.) ---
    // --- are unchanged and correct. ---

    @FXML
    private void handleAddTaskClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent taskFormRoot = loader.load();
            TaskFormController taskFormController = loader.getController();

            // 1. Create Stage with TRANSPARENT Style (Removes White Bar)
            Stage dialogStage = new Stage();
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) addTaskButton.getScene().getWindow());

            // 2. Create Scene ONCE and set Fill to TRANSPARENT
            Scene scene = new Scene(taskFormRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Required for rounded corners
            dialogStage.setScene(scene);

            // 3. Pass Data to Controller
            taskFormController.setDialogStage(dialogStage);

            // 4. Enable Dragging (Since we removed the title bar)
            makeDraggable(taskFormRoot, dialogStage);

            dialogStage.showAndWait();

            if (taskFormController.isSaveClicked()) {
                String title = taskFormController.getTaskTitle();
                String desc = taskFormController.getTaskDescription();
                LocalDate dueDate = taskFormController.getTaskDueDate();
                String category = taskFormController.getTaskCategory();
                String priority = taskFormController.getTaskPriority();

                Task newTask = new Task(title, desc, dueDate, category, priority);
                tasks.add(newTask);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMinimize() {
        // Get the current stage (window) from any component in the scene, e.g., addTaskButton
        Stage stage = (Stage) addTaskButton.getScene().getWindow();

        // Minimize the window
        stage.setIconified(true);
    }

    @FXML
    private void handleExit() {
        // 1. Create the confirmation alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null); // No header, just the message
        alert.setContentText("Are you sure you want to exit?");
        alert.initStyle(javafx.stage.StageStyle.UNDECORATED);

        Image image = new Image(getClass().getResourceAsStream("/images/exit.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(48);
        imageView.setFitWidth(48);
        alert.setGraphic(imageView);

        // --- 2. LINK CSS ---
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-page");

        // --- 3. CUSTOMIZE BUTTONS ---
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Lookup the buttons so we can style them individually
        Button yesButton = (Button) dialogPane.lookupButton(buttonTypeYes);
        Button noButton = (Button) dialogPane.lookupButton(buttonTypeNo);

        // Add specific CSS classes
        yesButton.getStyleClass().add("yes-button");
        noButton.getStyleClass().add("no-button");

        // Show the alert and wait for the user's choice
        Optional<ButtonType> result = alert.showAndWait();
        // If they clicked "Yes", save and close
        if (result.isPresent() && result.get() == buttonTypeYes) {
            DataManager.saveTasks(tasks);
            Stage stage = (Stage) addTaskButton.getScene().getWindow();
            stage.close();
        }
    }

    // --- UPDATED HELP METHOD ---
    @FXML
    private void handleHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("User Guidelines");
        alert.setHeaderText("How to use Smart ToDo List");

        // 1. Set Alert Icon
        ImageView helpIcon = loadImageViewSafely("/images/help.png", 48);
        if (helpIcon != null) {
            alert.setGraphic(helpIcon);
        }

        // 2. Load Content Images using the helper method
        ImageView addTaskImageNode = loadImageViewSafely("/images/add-task.png", 150);
        ImageView deleteTaskNode = loadImageViewSafely("/images/delete.png", 900);
        ImageView filterTaskNode = loadImageViewSafely("/images/filter.png", 900);
        ImageView exitTaskNode = loadImageViewSafely("/images/exit-app.png", 250);

        // 2. Create Main Container
        VBox contentContainer = new VBox(20);
        contentContainer.setPrefWidth(500);

        // 3. Add Help Items (Using the helper method for cleaner code)
        contentContainer.getChildren().addAll(
                createHelpItem("1. Add Task", "Click the (+) button on the left sidebar to create a new task."), addTaskImageNode,
                createHelpItem("2. Edit Task", "Double-click any row in the table to view or edit details."),
                createHelpItem("3. Delete Task", "Select a task row and click the 'Delete' button to remove it."), deleteTaskNode,
                createHelpItem("4. Search & Filter", "Use the top bar to search by keyword or filter by Category/Status."), filterTaskNode,
                createHelpItem("5. Save & Exit", "Your data saves automatically when you modify tasks or exit the app."), exitTaskNode
        );

        // 5. Set Content & Style
        alert.getDialogPane().setContent(contentContainer);
        alert.initStyle(javafx.stage.StageStyle.UNDECORATED);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-page");

        alert.showAndWait();
    }

    /**
     * Helper method to load an image safely.
     * Returns an ImageView if successful, or null if the image is missing.
     */
    private ImageView loadImageViewSafely(String path, double width) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            ImageView view = new ImageView(img);
            view.setFitWidth(width);
            view.setPreserveRatio(true);
            return view;
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
            return null; // Return null so nothing gets added to the VBox
        }
    }

    /**
     * Helper method to create a nicely formatted help item.
     * It puts the Title in Gold/Beige (Bold) and the Description in Light Beige (Normal).
     */
    private VBox createHelpItem(String title, String description, Node graphic) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #948979; -fx-font-size: 15px; -fx-font-family: 'Segoe UI Black';");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #DFD0B8; -fx-font-size: 13px; -fx-font-family: 'Segoe UI Black';");
        descLabel.setWrapText(true);

        VBox container = new VBox(5, titleLabel, descLabel);

        // If an image exists, wrap it in an HBox to CENTER it
        if (graphic != null) {
            HBox imageWrapper = new HBox(graphic);
            imageWrapper.setAlignment(Pos.CENTER); // This centers the image horizontally
            container.getChildren().add(imageWrapper);
        }

        return container;
    }

    // Overloaded helper for text-only items
    private VBox createHelpItem(String title, String description) {
        return createHelpItem(title, description, null);
    }

    @FXML
    private void handleViewDetails() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to view/edit.");
            alert.initStyle(javafx.stage.StageStyle.UNDECORATED); // Removes white title bar

            // --- ADD CUSTOM ERROR ICON ---
            try {
                Image image = new Image(getClass().getResourceAsStream("/images/error.png"));
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(48);
                imageView.setFitWidth(48);
                alert.setGraphic(imageView);
            } catch (Exception e) {
                System.out.println("Could not load error.png");
            }

            // 1. Link CSS
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
            dialogPane.getStyleClass().add("alert-page");

            // 2. Style the OK Button (So it isn't invisible)
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            // We reuse "yes-button" (Green) or "no-button" (Red) to ensure text is visible.
            // Green ("yes-button") is usually best for a simple "OK".
            okButton.getStyleClass().add("yes-button");

            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent taskFormRoot = loader.load();
            TaskFormController taskFormController = loader.getController();

            // 1. Create Stage with TRANSPARENT Style (Removes White Bar)
            Stage dialogStage = new Stage();
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) addTaskButton.getScene().getWindow());

            // 2. Create Scene ONCE and set Fill to TRANSPARENT
            Scene scene = new Scene(taskFormRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Required for rounded corners
            dialogStage.setScene(scene);

            // 3. Pass Data to Controller
            taskFormController.setTask(selectedTask);
            taskFormController.setDialogStage(dialogStage);

            // 4. Enable Dragging (Since we removed the title bar)
            makeDraggable(taskFormRoot, dialogStage);

            // 5. Show
            dialogStage.showAndWait();

            if (taskFormController.isSaveClicked()) {
                taskTable.refresh();
                int index = tasks.indexOf(selectedTask);
                if (index != -1) {
                    tasks.set(index, selectedTask);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

        // --- 1. "NO SELECTION" POPUP (Yellow Warning Triangle) ---
        if (selectedTask == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            // BIG HEADER TEXT
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to delete.");
            alert.initStyle(javafx.stage.StageStyle.UNDECORATED);

            // --- ADD CUSTOM ERROR ICON ---
            try {
                Image image = new Image(getClass().getResourceAsStream("/images/error.png"));
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(48);
                imageView.setFitWidth(48);
                alert.setGraphic(imageView);
            } catch (Exception e) {
                System.out.println("Could not load error.png");
            }

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
            dialogPane.getStyleClass().add("alert-page");

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.getStyleClass().add("yes-button");

            alert.showAndWait();
            return;
        }

        // --- 2. DELETE CONFIRMATION POPUP (Custom Folder Icon) ---
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");

        // --- THIS MAKES IT LOOK LIKE THE OTHER POPUP ---
        alert.setHeaderText("Confirm Deletion");

        alert.setContentText("Are you sure you want to delete: " + selectedTask.getTitle() + "?");
        alert.initStyle(javafx.stage.StageStyle.UNDECORATED);

        // Add your Custom Icon
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/delete.png"));
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(48);
            imageView.setFitWidth(48);
            alert.setGraphic(imageView);
        } catch (Exception e) {
            // Ignore if image missing
        }

        // Link CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-page");

        // Customize Buttons
        ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Button yesButton = (Button) dialogPane.lookupButton(buttonTypeYes);
        Button noButton = (Button) dialogPane.lookupButton(buttonTypeNo);

        yesButton.getStyleClass().add("no-button"); // Red
        noButton.getStyleClass().add("yes-button"); // Green

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            tasks.remove(selectedTask);
        }
    }

    @FXML
    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase();
        String category = filterCategoryCombo.getValue();
        String status = filterStatusCombo.getValue();
        String priority = filterPriorityCombo.getValue();
        LocalDate date = filterDate.getValue();

        filteredTasks.setPredicate(t -> {
            boolean keywordMatch = keyword.isEmpty() ||
                    t.getTitle().toLowerCase().contains(keyword);
            boolean categoryMatch = category.equals("All Categories") ||
                    t.getCategory().equals(category);
            boolean statusMatch;
            if (status.equals("All Status")) {
                statusMatch = true;
            } else if (status.equals("Completed")) {
                statusMatch = t.isCompleted();
            } else {
                statusMatch = !t.isCompleted();
            }
            boolean priorityMatch = priority == null ||
                                    priority.equals("All Priority") ||
                                    t.getPriority().equalsIgnoreCase(priority);
            boolean dateMatch = (date == null) ||
                    t.getDueDate().isEqual(date);
            return keywordMatch && categoryMatch && statusMatch && priorityMatch && dateMatch;
        });
    }

    @FXML
    private void clearFilters() {
        searchField.setText("");
        filterCategoryCombo.getSelectionModel().select("All Categories");
        filterStatusCombo.getSelectionModel().select("All Status");
        filterPriorityCombo.getSelectionModel().select("All Priority");
        filterDate.setValue(null);
    }

    public void saveTasksOnExit() {
        DataManager.saveTasks(tasks);
    }

    @FXML
    private void handleFilterAll() {
        clearFilters();
    }

    @FXML
    private void handleFilterToday() {
        filterDate.setValue(LocalDate.now());
        searchField.setText("");
        filterCategoryCombo.getSelectionModel().select("All Categories");
        filterStatusCombo.getSelectionModel().select("All Status");
    }

    @FXML
    private void handleFilterUpcoming() {
        clearFilters();
        filterStatusCombo.getSelectionModel().select("Pending");
    }

    @FXML
    private void handleFilterSchool() {
        clearFilters();
        filterCategoryCombo.getSelectionModel().select("School");
    }

    @FXML
    private void handleFilterWork() {
        clearFilters();
        filterCategoryCombo.getSelectionModel().select("Work");
    }

    @FXML
    private void handleFilterPersonal() {
        clearFilters();
        filterCategoryCombo.getSelectionModel().select("Personal");
    }

    @FXML
    private void handleFilterHome() {
        clearFilters();
        filterCategoryCombo.getSelectionModel().select("Home");
    }
}
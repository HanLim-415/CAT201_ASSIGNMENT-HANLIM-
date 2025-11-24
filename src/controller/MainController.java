package controller;

// --- Imports ---
import javafx.application.Platform; // <-- 1. ADD THIS IMPORT
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Task");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) addTaskButton.getScene().getWindow());
            dialogStage.setScene(new Scene(taskFormRoot));
            taskFormController.setDialogStage(dialogStage);

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

    @FXML
    private void handleHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("User Guidelines");
        alert.setHeaderText("How to use Smart ToDo List");

        // --- 1. SET THE ICON (Top Right) ---
        try {
            Image helpIcon = new Image(getClass().getResourceAsStream("/images/help.png"));
            ImageView iconView = new ImageView(helpIcon);
            iconView.setFitHeight(48);
            iconView.setFitWidth(48);
            alert.setGraphic(iconView);
        } catch (Exception e) {
            System.out.println("Help icon not found.");
        }

        // --- 2. CREATE A CONTAINER FOR TEXT + IMAGE ---
        // A VBox (Vertical Box) to stack text on top of the image
        VBox contentContainer = new VBox(15); // 15px spacing between items
        contentContainer.setPrefWidth(500);   // Force it to be wider so text fits!

        // --- 3. THE TEXT PART ---
        String guidelinesText =
                        "1. Add Task:\n" +
                        "   Click the (+) button on the left sidebar to create a new task.\n\n" +
                        "2. Edit Task:\n" +
                        "   Double-click any row in the table to edit details.\n\n" +
                        "3. Delete Task:\n" +
                        "   Select a task and click 'Delete' to remove it.\n\n" +
                        "4. Search & Filter:\n" +
                        "   Use the top bar to search by keyword or filter by Category.\n\n" +
                        "5. Save & Exit:\n" +
                        "   Your data saves automatically when you modify tasks or exit.";

        Label textLabel = new Label(guidelinesText);
        textLabel.setWrapText(true); // <--- THIS FIXES THE CUT-OFF TEXT
        textLabel.setStyle("-fx-text-fill: #DFD0B8; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Black';");

        // --- 4. THE IMAGE SNIPPET PART ---
        // Uncomment and use your image file when you have it!
    /*
    try {
        Image snippet = new Image(getClass().getResourceAsStream("/images/snippet.png"));
        ImageView snippetView = new ImageView(snippet);
        snippetView.setFitWidth(480); // Make it fit inside the box
        snippetView.setPreserveRatio(true);
        contentContainer.getChildren().add(snippetView);
    } catch (Exception e) {
        // Image not found
    }
    */

        // Add text (and later the image) to the box
        contentContainer.getChildren().add(0, textLabel); // Add text at index 0

        // --- 5. SET THIS BOX AS THE DIALOG CONTENT ---
        alert.getDialogPane().setContent(contentContainer);

        // --- 6. APPLY STYLING ---
        alert.initStyle(javafx.stage.StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/alertpage.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-page");

        alert.showAndWait();
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

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Task Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner((Stage) addTaskButton.getScene().getWindow());
            dialogStage.setScene(new Scene(taskFormRoot));

            taskFormController.setTask(selectedTask);
            taskFormController.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (taskFormController.isSaveClicked()) {
                taskTable.refresh();
                // We must manually tell the ListChangeListener that the
                // task has been updated, so the summary labels refresh.
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
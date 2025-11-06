package controller;

// --- Imports ---
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.io.IOException;
import java.time.LocalDate;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import util.DataManager;

/**
 * Controller for the main application view (MainView.fxml).
 * Handles the main TableView and button actions.
 */
public class MainController {

    // --- 1. MODIFIED THIS LINE ---
    // This now loads tasks from your tasks.json file at startup
    private ObservableList<Task> tasks = DataManager.loadTasks();
    private FilteredList<Task> filteredTasks;

    // --- FXML Fields from MainView.fxml ---
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
    @FXML private DatePicker filterDate;


    /**
     * This method is called by the FXML loader when initialization is complete.
     */
    @FXML
    private void initialize() {
        // --- Table Column Setup ---
        taskTable.setEditable(true);
        colStatus.setEditable(true);
        colStatus.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));
        colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        // --- 2. REMOVED DUPLICATE colTitle LINE ---
        colPriority.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colDueDate.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());

        // --- Filter Setup ---
        filteredTasks = new FilteredList<>(tasks, p -> true); // 'p -> true' means "show all" by default
        taskTable.setItems(filteredTasks);

        // Populate filter ComboBoxes
        filterCategoryCombo.getItems().addAll("All Categories", "Work", "Personal", "School", "Home", "Other");
        filterCategoryCombo.getSelectionModel().select("All Categories");

        filterStatusCombo.getItems().addAll("All Status", "Completed", "Pending");
        filterStatusCombo.getSelectionModel().select("All Status");

        // --- 3. DELETED TEST DATA ---
        // (The tasks.add(...) lines are now gone)
    }

    /**
     * This method is called when the "Add New Task" button is clicked.
     */
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
                tasks.add(newTask); // Add to the main list (FilteredList updates automatically)
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Add Task form!");
        }
    }

    // --- Other Methods from your FXML ---

    @FXML
    private void handleExit() {
        // This just closes the window.
        // The *real* save logic is in MainApp.java's setOnCloseRequest
        Stage stage = (Stage) addTaskButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelp() {
        System.out.println("Help menu clicked!");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Smart ToDo List");
        alert.setHeaderText(null);
        alert.setContentText("This is an assignment for CAT201.");
        alert.showAndWait();
    }

    @FXML
    private void handleViewDetails() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to view/edit.");
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
                taskTable.refresh(); // Refresh table to show changes
                System.out.println("Task updated: " + selectedTask.getTitle());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Add Task form!");
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to delete.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Task: " + selectedTask.getTitle());
            alert.setContentText("Are you sure you want to delete this task?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                tasks.remove(selectedTask); // Remove from main list
            }
        }
    }

    @FXML
    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase();
        String category = filterCategoryCombo.getValue();
        String status = filterStatusCombo.getValue();
        LocalDate date = filterDate.getValue();

        filteredTasks.setPredicate(t -> {
            boolean keywordMatch = keyword.isEmpty() ||
                    t.getTitle().toLowerCase().contains(keyword) ||
                    t.getDescription().toLowerCase().contains(keyword);

            boolean categoryMatch = category.equals("All Categories") ||
                    t.getCategory().equals(category);

            boolean statusMatch;
            if (status.equals("All Status")) {
                statusMatch = true;
            } else if (status.equals("Completed")) {
                statusMatch = t.isCompleted();
            } else { // "Pending"
                statusMatch = !t.isCompleted();
            }

            boolean dateMatch = (date == null) ||
                    t.getDueDate().isEqual(date);

            // Task is shown only if it matches ALL filters
            return keywordMatch && categoryMatch && statusMatch && dateMatch;
        });
    }

    @FXML
    private void clearFilters() {
        searchField.setText("");
        filterCategoryCombo.getSelectionModel().select("All Categories");
        filterStatusCombo.getSelectionModel().select("All Status");
        filterDate.setValue(null);
        filteredTasks.setPredicate(p -> true); // Reset filter to show all
    }

    /**
     * This method is called by MainApp when the window is closed.
     */
    public void saveTasksOnExit() {
        DataManager.saveTasks(tasks);
    }

    // ... (Your other handleFilter... methods) ...
    @FXML private void handleFilterAll() {}
    @FXML private void handleFilterToday() {}
    @FXML private void handleFilterUpcoming() {}
    @FXML private void handleFilterSchool() {}
    @FXML private void handleFilterWork() {}
    @FXML private void handleFilterPersonal() {}
    @FXML private void handleFilterHome() {}

}
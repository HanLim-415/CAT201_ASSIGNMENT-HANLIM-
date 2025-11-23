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

        // --- 2. THIS IS THE CORRECTED LISTENER ---
        tasks.addListener((ListChangeListener.Change<? extends Task> c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                    // Use Platform.runLater() to delay the update
                    // This ensures all property changes are complete
                    // before we re-calculate the summary.
                    Platform.runLater(() -> updateSummaryLabels());
                    break;
                }
            }
        });
        // --- END CORRECTION ---

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
        Stage stage = (Stage) addTaskButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelp() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About Smart ToDo List");
        alert.setHeaderText(null);
        alert.setContentText("This is an assignment for CAT201.");
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
        if (selectedTask == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to delete.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Task: " + selectedTask.getTitle());
            alert.setContentText("Are you sure you want to delete this task?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                tasks.remove(selectedTask);
            }
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
                    t.getTitle().toLowerCase().contains(keyword) ||
                    t.getDescription().toLowerCase().contains(keyword);
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
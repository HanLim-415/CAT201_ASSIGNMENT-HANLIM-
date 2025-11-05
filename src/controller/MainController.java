package controller;

// --- Imports ---
import javafx.collections.FXCollections;
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

/**
 * Controller for the main application view (MainView.fxml).
 * Handles the main TableView and button actions.
 */
public class MainController {

    // This is the main list that holds all your task data.
    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private FilteredList<Task> filteredTasks;

    // --- FXML Fields from MainView.fxml ---
    // These variables MUST match the fx:id's from SceneBuilder

    @FXML
    private Button addTaskButton;

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, Boolean> colStatus;

    @FXML
    private TableColumn<Task, String> colTitle;

    @FXML
    private TableColumn<Task, String> colPriority;

    @FXML
    private TableColumn<Task, String> colCategory;

    @FXML
    private TableColumn<Task, LocalDate> colDueDate;

    // --- Initialization Method ---

    /**
     * This method is called by the FXML loader when initialization is complete.
     * It sets up the TableView columns and loads initial data.
     */
    @FXML
    private void initialize() {
        // --- Table Column Setup ---
        taskTable.setEditable(true);
        colStatus.setEditable(true);
        colStatus.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));
        colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        colTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        colPriority.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colDueDate.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());

        // --- NEW: Filter Setup ---
        // 1. Wrap the main 'tasks' list in a FilteredList
        filteredTasks = new FilteredList<>(tasks, p -> true); // 'p -> true' means "show all" by default

        // 2. Bind the TableView to the FilteredList (instead of the main 'tasks' list)
        taskTable.setItems(filteredTasks);

        // 3. Populate filter ComboBoxes
        filterCategoryCombo.getItems().addAll("All Categories", "Work", "Personal", "School", "Home", "Other");
        filterCategoryCombo.getSelectionModel().select("All Categories");

        filterStatusCombo.getItems().addAll("All Status", "Completed", "Pending");
        filterStatusCombo.getSelectionModel().select("All Status");
        // --- END NEW ---

        // --- Load Data ---
        // (Optional) Add dummy data for testing. We'll replace this with JSON loading later.
        tasks.add(new Task("Assignment 1", "Complete CAT201", LocalDate.now(), "School", "High"));
        tasks.add(new Task("Buy Groceries", "Milk, Eggs, Bread", LocalDate.now().plusDays(2), "Home", "Medium"));

        // Note: We add test data to the main 'tasks' list,
        // and the 'filteredTasks' list (which the table views) updates automatically.
    }

    // --- FXML Event Handlers ---

    /**
     * This method is called when the "Add New Task" button is clicked.
     * It loads and shows the TaskFormView.fxml pop-up window.
     */
    @FXML
    private void handleAddTaskClick() {
        try {
            // 1. Load the FXML file for the pop-up
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent taskFormRoot = loader.load();

            // 2. Get the controller for the pop-up window
            TaskFormController taskFormController = loader.getController();

            // 3. Create a new "Stage" (the window) for the pop-up
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Task");
            dialogStage.initModality(Modality.WINDOW_MODAL); // This blocks the main window

            // Set the owner (the main window)
            Stage mainStage = (Stage) addTaskButton.getScene().getWindow();
            dialogStage.initOwner(mainStage);

            // 4. Set the scene
            dialogStage.setScene(new Scene(taskFormRoot));

            // 5. Give the pop-up controller a reference to its own stage
            taskFormController.setDialogStage(dialogStage);

            // 6. Show the pop-up and wait for it to close
            dialogStage.showAndWait();

            // 7. After it closes, check if the save button was clicked
            if (taskFormController.isSaveClicked()) {
                // Get the data from the form's controller
                String title = taskFormController.getTaskTitle();
                String desc = taskFormController.getTaskDescription();
                LocalDate dueDate = taskFormController.getTaskDueDate();
                String category = taskFormController.getTaskCategory();
                String priority = taskFormController.getTaskPriority();

                // Create a new Task object
                Task newTask = new Task(title, desc, dueDate, category, priority);

                // Add the new task to your main ObservableList
                tasks.add(newTask);

                // The TableView will update automatically!
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Add Task form!");
        }
    }

    // --- Other Methods from your FXML ---

    @FXML
    private void handleExit() {
        // This will get the main window and close it
        Stage stage = (Stage) addTaskButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelp() {
        System.out.println("Help menu clicked!");
        // TODO: Show a real pop-up alert
    }

    @FXML
    private void handleViewDetails() {
        // 1. Get the task currently selected in the TableView
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

        // 2. Check if a task was actually selected
        if (selectedTask == null) {
            // Nothing selected, show a warning
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to view/edit.");
            alert.showAndWait();
            return; // Stop running the method
        }

        try {
            // 3. Load the FXML file for the pop-up (just like in handleAddTaskClick)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent taskFormRoot = loader.load();

            // 4. Get the controller for the pop-up window
            TaskFormController taskFormController = loader.getController();

            // 5. Create a new "Stage" (the window) for the pop-up
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Task Details"); // Set a new title
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage mainStage = (Stage) addTaskButton.getScene().getWindow();
            dialogStage.initOwner(mainStage);
            dialogStage.setScene(new Scene(taskFormRoot));

            // --- THIS IS THE NEW PART ---
            // 6. Pass the selected task to the form controller
            taskFormController.setTask(selectedTask);
            taskFormController.setDialogStage(dialogStage);
            // --- END OF NEW PART ---

            // 7. Show the pop-up and wait for it to close
            dialogStage.showAndWait();

            // 8. After it closes, check if the save button was clicked
            if (taskFormController.isSaveClicked()) {
                // If save was clicked, the 'handleSaveTask' method in
                // TaskFormController already updated the task's properties.

                // Because the Task object uses JavaFX Properties,
                // the TableView will update automatically!

                // We can refresh the table just in case (optional but good)
                taskTable.refresh();
                System.out.println("Task updated: " + selectedTask.getTitle());
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Add Task form!");
        }
    }

    @FXML
    private void handleDeleteTask() {
        System.out.println("Delete Task button clicked!");
        // 1. Get the task currently selected in the TableView
        Task selectedTask = taskTable.getSelectionModel().getSelectedItem();

        // 2. Check if a task was actually selected
        if (selectedTask == null) {
            // Nothing selected, show a warning
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task in the table to delete.");
            alert.showAndWait();
        } else {
            // A task is selected, ask for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Task: " + selectedTask.getTitle());
            alert.setContentText("Are you sure you want to delete this task?");

            // 4. Show the alert and wait for the user's response
            Optional<ButtonType> result = alert.showAndWait();

            // 5. If the user clicked "OK", remove the task
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Remove the task from the main 'tasks' list
                tasks.remove(selectedTask);
                // The TableView will automatically update!
            }
        }
    }

    @FXML
    private void applyFilters() {
        // 1. Get the current values from all filter controls
        String keyword = searchField.getText().toLowerCase();
        String category = filterCategoryCombo.getValue();
        String status = filterStatusCombo.getValue();
        LocalDate date = filterDate.getValue();

        // 2. Set the "predicate" (the filter rule) for our FilteredList
        // The task object (t) is passed in, and we return 'true' if
        // it should be included, or 'false' if it should be hidden.
        filteredTasks.setPredicate(t -> {

            // --- Check 1: Keyword Search ---
            // If keyword is empty, it's an automatic pass.
            // Otherwise, check if title or description contains the keyword.
            boolean keywordMatch = keyword.isEmpty() ||
                    t.getTitle().toLowerCase().contains(keyword) ||
                    t.getDescription().toLowerCase().contains(keyword);

            // --- Check 2: Category Filter ---
            // If "All Categories" is selected, it's a pass.
            // Otherwise, check if the task's category matches.
            boolean categoryMatch = category.equals("All Categories") ||
                    t.getCategory().equals(category);

            // --- Check 3: Status Filter ---
            // If "All Status" is selected, it's a pass.
            // Otherwise, check the task's 'completed' status.
            boolean statusMatch;
            if (status.equals("All Status")) {
                statusMatch = true;
            } else if (status.equals("Completed")) {
                statusMatch = t.isCompleted(); // 'isCompleted()' must be true
            } else { // "Pending"
                statusMatch = !t.isCompleted(); // 'isCompleted()' must be false
            }

            // --- Check 4: Date Filter ---
            // If no date is picked, it's a pass.
            // Otherwise, check if the task's due date is the same day.
            boolean dateMatch = (date == null) ||
                    t.getDueDate().isEqual(date);

            // --- Final Decision ---
            // The task is only shown if it matches ALL filters
            return keywordMatch && categoryMatch && statusMatch && dateMatch;
        });
    }

    @FXML
    private void clearFilters() {
        // 1. Reset all the filter controls
        searchField.setText("");
        filterCategoryCombo.getSelectionModel().select("All Categories");
        filterStatusCombo.getSelectionModel().select("All Status");
        filterDate.setValue(null); // Clears the date picker

        // 2. Set the predicate back to default (show all tasks)
        filteredTasks.setPredicate(p -> true);
    }

    // ... (Your other handleFilter... methods) ...
    @FXML private void handleFilterAll() {}
    @FXML private void handleFilterToday() {}
    @FXML private void handleFilterUpcoming() {}
    @FXML private void handleFilterSchool() {}
    @FXML private void handleFilterWork() {}
    @FXML private void handleFilterPersonal() {}
    @FXML private void handleFilterHome() {}

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private DatePicker filterDate;

} // <-- This is the end of the MainController class
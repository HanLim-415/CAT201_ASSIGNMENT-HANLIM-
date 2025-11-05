package controller; // This package matches your project structure: src/controller

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional; // Needed for Alert.showAndWait() result
import model.Task;

public class TaskFormController {

    // FXML elements from TaskFormView.fxml - make sure these fx:id's match exactly!
    @FXML
    private TextField taskTitleField;
    @FXML
    private TextArea taskDescriptionArea;
    @FXML
    private DatePicker taskDueDatePicker;
    @FXML
    private ChoiceBox<String> taskCategoryChoiceBox; // Will hold categories like "Work", "Personal", etc.
    @FXML
    private ChoiceBox<String> taskPriorityChoiceBox; // Will hold priorities like "High", "Medium", "Low"
    @FXML
    private Button saveTaskButton;
    @FXML
    private Button closeTaskFormButton;

    private Stage dialogStage;      // Reference to the pop-up window's stage (itself)
    private boolean saveClicked = false; // Flag to indicate if "Save" button was pressed

    // Optional: A Task object to hold data (useful for editing existing tasks)
    private Task currentTask;

    // --- Initialization Method ---
    // This method is called automatically by JavaFX after the FXML has been loaded
    @FXML
    public void initialize() {
        // Populate ChoiceBoxes with example data
        // You would typically get these categories/priorities from your data model
        taskCategoryChoiceBox.getItems().addAll("Work", "Personal", "School", "Home", "Other");
        taskPriorityChoiceBox.getItems().addAll("High", "Medium", "Low");

        // Select default values (optional, can be removed)
        taskCategoryChoiceBox.getSelectionModel().selectFirst(); // Selects the first item
        taskPriorityChoiceBox.getSelectionModel().select("Medium"); // Selects "Medium"

        // You can add listeners here if needed, e.g., to validate in real-time
    }

    // --- Event Handlers (Linked via onAction in FXML) ---

    // Handles the action when the "Save" button is clicked
    @FXML
    private void handleSaveTask() {
        // 1. Validate the user input
        if (isInputValid()) {

            // 2. Get the new values from the input fields
            String title = taskTitleField.getText();
            String description = taskDescriptionArea.getText();
            LocalDate dueDate = taskDueDatePicker.getValue();
            String category = taskCategoryChoiceBox.getValue();
            String priority = taskPriorityChoiceBox.getValue();

            // 3. Check if we are in "Edit" mode (currentTask is not null)
            if (currentTask != null) {
                // We are editing: update the existing task object
                currentTask.setTitle(title);
                currentTask.setDescription(description);
                currentTask.setDueDate(dueDate);
                currentTask.setCategory(category);
                currentTask.setPriority(priority);
            }

            // 4. Set flag and close
            saveClicked = true;
            dialogStage.close();
        }
    }

    // Handles the action when the "Close" button is clicked
    @FXML
    private void handleCloseForm() {
        saveClicked = false; // Ensure flag is false if not saving
        dialogStage.close(); // Close the pop-up
    }

    // --- Helper Methods ---

    /**
     * Sets the stage of this dialog. This is called by the MainController
     * when the dialog is opened.
     * @param dialogStage The stage of the pop-up window.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked Save, false otherwise.
     * @return true if Save was clicked.
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    // /**
    //  * Sets the task to be displayed in the dialog.
    //  * This method would be used when you want to edit an existing task.
    //  * @param task The task object to edit.
    //  */
    public void setTask(Task task) {
        this.currentTask = task;

        // Populate the form fields with data from the task object
        taskTitleField.setText(task.getTitle());
        taskDescriptionArea.setText(task.getDescription());
        taskDueDatePicker.setValue(task.getDueDate());
        taskCategoryChoiceBox.getSelectionModel().select(task.getCategory());
        taskPriorityChoiceBox.getSelectionModel().select(task.getPriority());
    }

    /**
     * Validates the user input in the form fields.
     * @return true if the input is valid, false otherwise.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (taskTitleField.getText() == null || taskTitleField.getText().trim().isEmpty()) {
            errorMessage += "Title cannot be empty!\n";
        }
        if (taskDueDatePicker.getValue() == null) {
            errorMessage += "Please select a Due Date!\n";
        }
        if (taskCategoryChoiceBox.getValue() == null || taskCategoryChoiceBox.getValue().trim().isEmpty()) {
            errorMessage += "Please select a Category!\n";
        }
        if (taskPriorityChoiceBox.getValue() == null || taskPriorityChoiceBox.getValue().trim().isEmpty()) {
            errorMessage += "Please select a Priority!\n";
        }

        if (errorMessage.isEmpty()) {
            return true; // All fields are valid
        } else {
            // Show an alert dialog with the validation errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage); // Make the alert tied to the pop-up window
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please Correct Invalid Fields");
            alert.setContentText(errorMessage);

            // Wait for the user to close the alert
            alert.showAndWait();
            return false; // Input is not valid
        }
    }

    // You can add getter methods here to retrieve the task data after saveClicked is true
    public String getTaskTitle() { return taskTitleField.getText(); }
    public String getTaskDescription() { return taskDescriptionArea.getText(); }
    public LocalDate getTaskDueDate() { return taskDueDatePicker.getValue(); }
    public String getTaskCategory() { return taskCategoryChoiceBox.getValue(); }
    public String getTaskPriority() { return taskPriorityChoiceBox.getValue(); }
}
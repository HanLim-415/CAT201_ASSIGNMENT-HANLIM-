package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // This variable MUST match the fx:id of your "Add New Task" button
    // in MainView.fxml
    @FXML
    private Button addTaskButton;

    /**
     * This method is called when the "Add New Task" button is clicked.
     * It loads and shows the TaskFormView.fxml pop-up window.
     */
    @FXML
    private void handleAddTaskClick() {
        try {
            // 1. Load the FXML file for the pop-up
            // The "/" at the beginning is important! It starts from the 'src' root.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent taskFormRoot = loader.load();

            // 2. Get the controller for the pop-up window
            // Make sure you have created TaskFormController.java
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
            // (This allows the pop-up to close itself)
            taskFormController.setDialogStage(dialogStage);

            // 6. Show the pop-up and wait for it to close
            dialogStage.showAndWait();

            // 7. After it closes, check if the save button was clicked
            if (taskFormController.isSaveClicked()) {
                // This is where you will add your code later to get the task data
                System.out.println("Save button was clicked!");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load the Add Task form!");
            // You can show an alert here if you want
        }
    }

    // You can add your other methods (initialize, handleDeleteTask, etc.) here later
    @FXML
    private void handleExit() {
        // This will get the main window and close it
        Stage stage = (Stage) addTaskButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleHelp() {
        // This will show a simple "About" pop-up
        System.out.println("Help menu clicked!");

        // TODO: Later, you can make this show a real pop-up alert:
        // Alert alert = new Alert(Alert.AlertType.INFORMATION);
        // alert.setTitle("About Smart ToDo List");
        // alert.setHeaderText(null);
        // alert.setContentText("This is an assignment for CAT201.");
        // alert.showAndWait();
    }

    @FXML
    private void handleViewDetails() {
        // This is where you will add code to show the details of a selected task
        System.out.println("View Details button clicked!");

        // TODO: Later, you will get the selected task from the TableView
        // and show a new pop-up with its description.
    }

    @FXML
    private void handleDeleteTask() {
        // This is where you will add code to delete a selected task
        System.out.println("Delete Task button clicked!");

        // TODO: Later, you will get the selected task from the TableView,
        // ask for confirmation, and then remove it.
    }

    @FXML
    private void applyFilters() {
        // This is where you will add code to apply filtering logic
        System.out.println("Apply Filters button clicked!");

        // TODO: Later, you will read the values from your search field,
        // combo boxes, and date picker, then filter your task list.
    }

    @FXML
    private void clearFilters() {
        // This is where you will add code to reset all filter controls
        System.out.println("Clear Filters button clicked!");

        // TODO: Later, you will set your search field to empty,
        // and reset your combo boxes and date picker.
    }

    @FXML
    private void handleFilterAll() {
        System.out.println("Filter 'All Tasks' button clicked!");
        // TODO: Add logic to show all tasks
    }

    @FXML
    private void handleFilterToday() {
        System.out.println("Filter 'Today' button clicked!");
        // TODO: Add logic to show tasks due today
    }

    @FXML
    private void handleFilterUpcoming() {
        System.out.println("Filter 'Upcoming' button clicked!");
        // TODO: Add logic to show upcoming tasks
    }

    @FXML
    private void handleFilterSchool() {
        System.out.println("Filter 'School' button clicked!");
        // TODO: Add logic to filter by 'School' category
    }

    @FXML
    private void handleFilterWork() {
        System.out.println("Filter 'Work' button clicked!");
        // TODO: Add logic to filter by 'Work' category
    }

    @FXML
    private void handleFilterPersonal() {
        System.out.println("Filter 'Personal' button clicked!");
        // TODO: Add logic to filter by 'Personal' category
    }

    @FXML
    private void handleFilterHome() {
        System.out.println("Filter 'Home' button clicked!");
        // TODO: Add logic to filter by 'Home' category
    }
}
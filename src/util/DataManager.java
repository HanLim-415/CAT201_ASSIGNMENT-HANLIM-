package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Task;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    // The name of the file where we'll save tasks
    private static final String SAVE_FILE = "tasks.json";

    /**
     * Creates a custom Gson instance that knows how to handle
     * LocalDate.
     */
    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    /**
     * Loads the list of tasks from the tasks.json file.
     * @return An ObservableList of Tasks.
     */
    public static ObservableList<Task> loadTasks() {
        Gson gson = createGson();

        try (FileReader reader = new FileReader(SAVE_FILE)) {
            // Define the type of list we are expecting
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();

            // Read the JSON and convert it into a List<Task>
            List<Task> tasks = gson.fromJson(reader, taskListType);

            if (tasks != null) {
                // Return the loaded tasks as an ObservableList
                return FXCollections.observableArrayList(tasks);
            }

        } catch (IOException e) {
            System.out.println("No save file found or file is empty. Starting with a new list.");
        }

        // If anything fails (e.g., file not found), return an empty list
        return FXCollections.observableArrayList();
    }

    /**
     * Saves the current list of tasks to the tasks.json file.
     * @param tasks The list of tasks to save.
     */
    public static void saveTasks(ObservableList<Task> tasks) {
        Gson gson = createGson();

        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            // Convert the list of tasks to JSON and write it to the file
            gson.toJson(tasks, writer);

            System.out.println("Tasks saved successfully to " + SAVE_FILE);

        } catch (IOException e) {
            System.err.println("Error saving tasks to file!");
            e.printStackTrace();
        }
    }
}
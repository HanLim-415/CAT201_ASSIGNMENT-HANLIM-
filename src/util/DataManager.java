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

import javafx.beans.Observable;
import javafx.util.Callback;

public class DataManager {

    private static final String SAVE_FILE = "tasks.json";

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static ObservableList<Task> loadTasks() {
        Gson gson = createGson();

        // 1. Define the "extractor" that will watch for changes
        Callback<Task, Observable[]> extractor = task -> new Observable[]{
                task.completedProperty(), // Will fire an update when 'completed' changes
                task.dueDateProperty()    // Will fire an update when 'dueDate' changes
        };

        // 2. Create a new, empty "smart" list that USES this extractor
        //    (This is a valid and simple constructor)
        ObservableList<Task> observableTasks = FXCollections.observableArrayList(extractor);

        try (FileReader reader = new FileReader(SAVE_FILE)) {
            Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();

            // 3. Load the tasks from the JSON file into a temporary, simple list
            List<Task> loadedTasks = gson.fromJson(reader, taskListType);

            if (loadedTasks != null) {
                // 4. Add all the loaded tasks into our new smart list
                observableTasks.addAll(loadedTasks);
            }

        } catch (IOException e) {
            System.out.println("No save file found or file is empty. Starting with a new list.");
        }

        // 5. Return the smart list (it's either full of tasks or empty)
        return observableTasks;
    }

    public static void saveTasks(ObservableList<Task> tasks) {
        Gson gson = createGson();
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved successfully to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Error saving tasks to file!");
            e.printStackTrace();
        }
    }
}
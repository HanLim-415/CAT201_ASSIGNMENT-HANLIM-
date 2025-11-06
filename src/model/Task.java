package model;

import java.time.LocalDate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.BooleanProperty;

/**
 * This new Task class is designed to be compatible with both
 * JavaFX (which needs Properties) and Gson (which needs simple fields).
 */
public class Task {

    // --- 1. Plain Java Fields (for Gson to save) ---
    // Gson will save and load these fields directly.
    private String title;
    private String description;
    private LocalDate dueDate;
    private String category;
    private String priority;
    private boolean completed;

    // --- 2. JavaFX Properties (for the TableView) ---
    // These are marked 'transient' so Gson ignores them completely.
    private transient StringProperty titleProperty;
    private transient StringProperty descriptionProperty;
    private transient ObjectProperty<LocalDate> dueDateProperty;
    private transient StringProperty categoryProperty;
    private transient StringProperty priorityProperty;
    private transient BooleanProperty completedProperty;

    /**
     * Constructor for a new Task.
     */
    public Task(String title, String description, LocalDate dueDate, String category, String priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
        this.completed = false; // New tasks start as not completed
    }

    /**
     * No-argument constructor required by Gson.
     */
    public Task() {
        this.completed = false;
    }

    // --- 3. "Lazy-Loaded" Property Getters ---
    // These methods create the JavaFX property on-the-fly the first
    // time the TableView asks for it. This works for both
    // new tasks and tasks loaded from the JSON file.

    public StringProperty titleProperty() {
        if (titleProperty == null) {
            titleProperty = new SimpleStringProperty(title);
            // Link the property so it updates the base field
            titleProperty.addListener((obs, oldVal, newVal) -> title = newVal);
        }
        return titleProperty;
    }

    public StringProperty descriptionProperty() {
        if (descriptionProperty == null) {
            descriptionProperty = new SimpleStringProperty(description);
            descriptionProperty.addListener((obs, oldVal, newVal) -> description = newVal);
        }
        return descriptionProperty;
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        if (dueDateProperty == null) {
            dueDateProperty = new SimpleObjectProperty<>(dueDate);
            dueDateProperty.addListener((obs, oldVal, newVal) -> dueDate = newVal);
        }
        return dueDateProperty;
    }

    public StringProperty categoryProperty() {
        if (categoryProperty == null) {
            categoryProperty = new SimpleStringProperty(category);
            categoryProperty.addListener((obs, oldVal, newVal) -> category = newVal);
        }
        return categoryProperty;
    }

    public StringProperty priorityProperty() {
        if (priorityProperty == null) {
            priorityProperty = new SimpleStringProperty(priority);
            priorityProperty.addListener((obs, oldVal, newVal) -> priority = newVal);
        }
        return priorityProperty;
    }

    public BooleanProperty completedProperty() {
        if (completedProperty == null) {
            completedProperty = new SimpleBooleanProperty(completed);
            completedProperty.addListener((obs, oldVal, newVal) -> completed = newVal);
        }
        return completedProperty;
    }

    // --- 4. Standard Getters & Setters ---
    // These now update both the plain field and the (if it exists)
    // the JavaFX property, keeping them in sync.

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        if (titleProperty != null) titleProperty.set(title);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        if (descriptionProperty != null) descriptionProperty.set(description);
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        if (dueDateProperty != null) dueDateProperty.set(dueDate);
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
        if (categoryProperty != null) categoryProperty.set(category);
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) {
        this.priority = priority;
        if (priorityProperty != null) priorityProperty.set(priority);
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completedProperty != null) completedProperty.set(completed);
    }
}
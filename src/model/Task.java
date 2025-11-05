package model; // This matches the package you just created

import java.time.LocalDate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.BooleanProperty;

// Note: We are not in the 'controller' package, so we need to import it
// if we were. But this class is self-contained.

public class Task {

    // 1. Define the properties (these are the backend fields)
    private final StringProperty title;
    private final StringProperty description;
    private final ObjectProperty<LocalDate> dueDate;
    private final StringProperty category;
    private final StringProperty priority;
    private final BooleanProperty completed;

    /**
     * Constructor for a new Task.
     */
    public Task(String title, String description, LocalDate dueDate, String category, String priority) {
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.category = new SimpleStringProperty(category);
        this.priority = new SimpleStringProperty(priority);
        this.completed = new SimpleBooleanProperty(false); // New tasks always start as not completed
    }

    // --- Property Getter Methods ---
    // These are the "magic" methods that JavaFX (e.g., TableView columns)
    // will use to "bind" to the data.

    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty priorityProperty() { return priority; }
    public BooleanProperty completedProperty() { return completed; }

    // --- Standard Getters & Setters ---
    // These are for your own logic (and for JSON saving later).

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getDescription() { return description.get(); }
    public void setDescription(String desc) { this.description.set(desc); }

    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate date) { this.dueDate.set(date); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public String getPriority() { return priority.get(); }
    public void setPriority(String priority) { this.priority.set(priority); }

    public boolean isCompleted() { return completed.get(); }
    public void setCompleted(boolean isCompleted) { this.completed.set(isCompleted); }
}


import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author HP
 */
public class ToDo extends Application {

    //                          *************                 **********************
//                          *************    database connection      **********************
//                          *************                 **********************
    String sqliteDatabase = "E:\\ToDoList.db"; // SQLite database file
    String connectURL = "jdbc:sqlite:" + sqliteDatabase;

    //                          *************                 **********************
//                          *************   data fields      **********************
//                          *************                 **********************
    private TableView<Tasks> tableView;
    TextField tfName;
    TextField tfDelName;

    Label lbname;
    Label lbdelname;
    private Stage primaryStage;
    private boolean isSearchPerformed = false;
    Button btnBack;
    private TextField searchField;
    TextField tfEditname;
    Label lbeditname;
    Label lbHead;
    private Tasks selectedTask = null;
// Add the "Back" button as a class variable
    Button btnBackInsideTable;
    private boolean doubleClickEnabled = true;

    @Override
    public void start(Stage primaryStage) {
        String sqliteConnectURL = "jdbc:sqlite:" + sqliteDatabase;

        Pane pane = new Pane();

        HBox hb = new HBox(10);

        lbHead = new Label("ToDo List");
        lbHead.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 30));
        lbHead.setLayoutX(600);
        lbHead.setLayoutY(10);

        // Create the "Back" button for the TableView
        btnBackInsideTable = new Button("Back");
        btnBackInsideTable.setOnAction(event -> {
            isSearchPerformed = false;
            showTasks();
            btnBackInsideTable.setVisible(false);
        });

        searchField = new TextField();
        searchField.setPromptText("Search Tasks");
        searchField.setLayoutX(1200);
        searchField.setLayoutY(40);

        Button searchButton = new Button("Search");
        searchButton.setLayoutX(1130);
        searchButton.setLayoutY(40);
        searchButton.setOnAction(event -> showTasks());

        Button createtask = new Button("New Task");

        createtask.setOnAction(e -> createTask());

        Button deletetask = new Button("Delete");

        deletetask.setOnAction(e -> {

            deleteTask();
        });

        Button addDesc = new Button("Add Description");

        addDesc.setOnAction(e -> {

            if (selectedTask != null) {
                AddDesc(selectedTask);

            } else {
                showSuccessDialog("Please select a task First.");
            }
        });

        // ********************************************************     Search Button
        // ********************************************************     
        btnBack = new Button("Back");
        btnBack.setLayoutX(300); // Adjust the layout as needed
        btnBack.setLayoutY(40); // Adjust the layout as needed
        btnBack.setOnAction(event -> {
            searchField.clear();
            isSearchPerformed = false;
            showTasks();
        });
        hb.getChildren().addAll(createtask, addDesc, deletetask);

        //*******************************************************
        //******************************************************         Create TableView
        //*******************************************************
        tableView = new TableView<>();

        TableColumn<Tasks, Integer> snoColumn = new TableColumn<>("SNo");
        snoColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSno()).asObject());

        TableColumn<Tasks, String> nameColumn = new TableColumn<>("Name");

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        tableView.getColumns().addAll(snoColumn, nameColumn);

        //***********************
        //***********************
        // Scroll Pane
        //***********************
        //***********************
        ScrollPane scrollPane = new ScrollPane(tableView); // Wrap the TableView in a ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        HBox hbox = new HBox(scrollPane, btnBackInsideTable);
        hbox.setLayoutX(600);
        hbox.setLayoutY(90);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editmenuitem = new MenuItem("Edit Description ");
        MenuItem deleteMenuItem = new MenuItem("Delete ");
        MenuItem renametask = new MenuItem("Rename ");

        deleteMenuItem.setOnAction(event -> {
            Tasks selectedTask = tableView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                // Delete the selected Task
                ConfirmDelete(selectedTask.getName());

                // Refresh TableView after deleting a task
                showTasks();
            }
        });
//editdescription button
        editmenuitem.setOnAction(event -> {
            Tasks selectedTask = tableView.getSelectionModel().getSelectedItem();

            if (selectedTask != null) {
                // Delete the selected Task
                editDescription(selectedTask.getName());

                // Refresh TableView after deleting a task
                showTasks();
            }
        });
        // Rename task button
        renametask.setOnAction(event -> {
            Tasks selectedTask = tableView.getSelectionModel().getSelectedItem();

            if (selectedTask != null) {
                // Delete the selected Task
                String currentname = selectedTask.getName();
                renameTask(currentname);

                // Refresh TableView after deleting a task
                showTasks();
            }
        });

        contextMenu.getItems().addAll(deleteMenuItem, renametask, editmenuitem);

        tableView.setContextMenu(contextMenu);

        // Handle mouse click event on TableView
        tableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 1) {
                // Single-click event
                selectedTask = tableView.getSelectionModel().getSelectedItem();

                // Your code for single-click handling
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && doubleClickEnabled) {
                doubleClickEnabled = false; // Disable further double-clicks
                Tasks selectedTask = tableView.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {

                    openTableDataWindow(selectedTask.getName());
                }
            }
        });

//menu
        hb.setSpacing(10);
        hb.setLayoutX(600);
        hb.setLayoutY(50);

        btnBack.setVisible(false);
        btnBackInsideTable.setVisible(false);

        showTasks();
        pane.getChildren().addAll(btnBack, hbox, hb, lbHead, searchField, searchButton);
        pane.setOnMouseClicked(event -> {
            clearSelection();

        });

        Scene scene = new Scene(pane, 1200, 550);

        primaryStage.setTitle("ToDoList");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    //*******************************            *************************************//
    //******************************* Create Task *************************************//
    //*******************************               *************************************//

    private void createTask() {
        Button btnCreateTask = new Button("Create");
        Stage createTaskStage = new Stage();
        GridPane gp1 = new GridPane();
        Button btnCancel = new Button("Cancel"); // Add Cancel button

        gp1.setHgap(10);
        gp1.setVgap(10);
        gp1.setAlignment(Pos.CENTER);

        // Initialize labels and text fields
        Label lbname = new Label("Task Name:");
        TextField tfName = new TextField();

        gp1.add(lbname, 0, 0);
        gp1.add(tfName, 1, 0);
        gp1.add(btnCancel, 0, 1); // Add Cancel button to the grid
        gp1.add(btnCreateTask, 1, 1);

        btnCreateTask.setOnAction(e -> {
            String name = tfName.getText().replaceAll("\\s+", "_"); // Replace spaces with underscores
            if (!name.isEmpty()) {
                if (!doesTaskExist(name)) {
                    String createTableQuery = "CREATE TABLE IF NOT EXISTS " + name + " (sno INTEGER PRIMARY KEY AUTOINCREMENT, Description VARCHAR(255))";

                    TaskAdd(createTableQuery);
                    tfName.clear();

                    // Close the addtask dialog
                    createTaskStage.close();
                    // Refresh TableView after adding a task
                } else {
                    showSuccessDialog("Task with the same name already exists. Choose a different name.");
                }
            } else {
                showSuccessDialog("Task name Required");
            }
        });

        btnCancel.setOnAction(e -> {
            // Close the addTask dialog without performing any action
            createTaskStage.close();
        });

        // Make the addTask dialog modal
        createTaskStage.initModality(Modality.APPLICATION_MODAL);

        createTaskStage.initOwner(primaryStage);
        Scene addTaskscene = new Scene(gp1, 500, 400);

        createTaskStage.setTitle("Create Task");
        createTaskStage.setScene(addTaskscene);
        createTaskStage.showAndWait();  // Use showAndWait to wait for the dialog to close
    }

    //check task 
    private boolean doesTaskExist(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(connectURL);

            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                ResultSet resultSet = preparedStatement.executeQuery();

                return resultSet.next(); // If there is a result, the task exists
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error checking if task exists: " + e.getMessage());
            e.printStackTrace();
            return false; // Handle the exception appropriately in your application
        }
    }

    //*******************************            *************************************//
    //******************************* Add  Task *************************************//
    //*******************************               *************************************//
    public void TaskAdd(String createTableQuery) {
        try {
            Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
            Connection conn = DriverManager.getConnection(connectURL);

            // Perform database operations here
            try (PreparedStatement preparedStatement = conn.prepareStatement(createTableQuery)) {
                preparedStatement.executeUpdate();

                // Show a success dialog
                showSuccessDialog("Table created successfully!");
                showTasks();
            }

            conn.close(); // Close the connection when done
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to SQL Server database: " + e.getMessage());
            e.printStackTrace();
        }
    }

// **********************************                 **********************//
// ******************************   Success dialog Box add Task      *********//
//*************************************                 **********************//
    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
        showTasks();
    }

    // *************************************                 **********************
// ********************************       Deletion Dialog box?       **********************
//    ******************* *************                 **********************
    private void showDeletionSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Deletion Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Add OK button to the alert
        ButtonType okButton = new ButtonType("OK");
        alert.getButtonTypes().setAll(okButton);

        alert.showAndWait();
        showTasks();
    }

// Add a new method to open the window with table data
    private void openTableDataWindow(String tableName) {
        Stage tableDataStage = new Stage();
        VBox vbox = new VBox(10);

        // Create a new TableView for displaying table data
        TableView<String> tableDataTableView = new TableView<>();
        TableColumn<String, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

        tableDataTableView.getColumns().add(descriptionColumn);

        // Populate the tableDataTableView with data from the selected table
        try {
            Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
            Connection conn = DriverManager.getConnection(connectURL);

            String query = "SELECT Description FROM " + tableName;

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String description = resultSet.getString("Description");
                    tableDataTableView.getItems().add(description);
                }
            }

            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to SQL Server database: " + e.getMessage());
            e.printStackTrace();
        }

        vbox.getChildren().add(tableDataTableView);

        Scene scene = new Scene(vbox, 500, 400);
        tableDataStage.setTitle("Table Data - " + tableName);
        tableDataStage.setScene(scene);
        tableDataStage.show();
    }

// ***************************************                *************************************** 
// ***************************************   Show Tasks      *************************************** 
// ***************************************               *************************************** 
    private void showTasks() {
        tableView.getItems().clear(); // Clear previous data

//        tableView.setOnMouseClicked(event -> {
//            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
//                Tasks selectedTask = tableView.getSelectionModel().getSelectedItem();
//                if (selectedTask != null) {
//                    // Display task descriptions based on the selected table
//                    openTableDataWindow(selectedTask.getName());
//                }
//            }
//        });
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTask = new Tasks(newSelection.getName(), newSelection.getDescription());
            }
        });

        try {
            Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
            Connection conn = DriverManager.getConnection(connectURL);
            // Execute a custom query to retrieve user-created table names
            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                // Iterate through the result set and add tables to the TableView
                int sno = 1;
                while (resultSet.next()) {
                    String tableName = resultSet.getString("name");
                    // Use a placeholder description for now
                    tableView.getItems().add(new Tasks(sno++, tableName, "No description available"));
                }
            }

            conn.close(); // Close the connection when done
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to SQL Server database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //                          *************                 **********************
    //                          *************       Delete From Persons ?       **********************
    //                          *************                 **********************
    private void deleteTask() {
        if (selectedTask != null) {

            // Task is selected, proceed with deletion
            ConfirmDelete(selectedTask.getName());
        } else {
            // No task selected, prompt the user to enter a name
            promptForTaskName();
        }
    }

    private void promptForTaskName() {
        tfDelName = new TextField();
        lbdelname = new Label("Enter Task Name");
        Stage delTask = new Stage();
        GridPane gpDel = new GridPane();
        Button btnDelTask = new Button("Delete");
        Button btnCancel = new Button("Cancel");

        gpDel.setHgap(10);
        gpDel.setVgap(10);
        gpDel.setAlignment(Pos.CENTER);

        gpDel.add(lbdelname, 0, 0);
        gpDel.add(tfDelName, 1, 0);

        gpDel.add(btnDelTask, 1, 2);
        gpDel.add(btnCancel, 0, 2);

        btnDelTask.setOnAction(e -> {
            String name = tfDelName.getText();
            if (!name.isEmpty()) {
                ConfirmDelete(name);
                tfDelName.clear();
                delTask.close();
            } else {
                showDeletionSuccessDialog("Task name is required");
            }
        });

        btnCancel.setOnAction(e -> {
            tfDelName.clear();
            delTask.close();
        });

        // Make the delTask modal
        delTask.initModality(Modality.APPLICATION_MODAL);
        delTask.initOwner(primaryStage);

        Scene addContscene = new Scene(gpDel, 500, 400);

        delTask.setTitle("Delete Task");
        delTask.setScene(addContscene);
        delTask.showAndWait();  // Use showAndWait to wait for the dialog to close
    }

//                          *************                 **********************
//                          *************       Confirm Del ?       **********************
//                          *************                 **********************
    private void ConfirmDelete(String name) {
        if (!name.isEmpty()) {
            try {
                Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
                Connection conn = DriverManager.getConnection(connectURL);

                // Perform database operations here
                String deleteQuery = "DROP TABLE " + name;

                try (PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery)) {
                    preparedStatement.executeUpdate();

                    // Show a deletion success dialog
                    showDeletionSuccessDialog("Task deleted successfully!");
                    selectedTask = null;
                    showTasks();
                }

                conn.close(); // Close the connection when done
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error connecting to SQL Server database: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showDeletionSuccessDialog("Nothing to delete");
        }
    }

    //*******************************            *************************************//
    //******************************* Add Desc *************************************//
    //*******************************               *************************************//
    public void AddDesc(Tasks selectedTaskVar) {
        GridPane descPane = new GridPane();
        descPane.setHgap(10);
        descPane.setVgap(10);
        descPane.setAlignment(Pos.CENTER);

        Label lbAddDesc = new Label("Enter Task Description:");
        TextArea tfAddDesc = new TextArea();

        descPane.add(lbAddDesc, 0, 1);
        descPane.add(tfAddDesc, 1, 1);

        Button btnAddDesc = new Button("Add Description");
        Button btnCancel = new Button("Cancel");

        descPane.add(btnAddDesc, 1, 2);
        descPane.add(btnCancel, 0, 2);

        Stage addDescStage = new Stage();

        btnAddDesc.setOnAction(e -> {
            String description = tfAddDesc.getText().trim();

            if (selectedTask != null && !description.isEmpty()) {
                addDescriptionToTask(selectedTask, description);
                addDescStage.close();
            } else {
                showSuccessDialog("Please select a task first.");
            }
        });

        btnCancel.setOnAction(e -> addDescStage.close());

        // Make the addDescStage modal
        addDescStage.initModality(Modality.APPLICATION_MODAL);
        addDescStage.initOwner(primaryStage);

        Scene addDescScene = new Scene(descPane, 800, 500);
        addDescStage.setTitle("Add Description to " + selectedTaskVar.getName());
        addDescStage.setScene(addDescScene);
        addDescStage.showAndWait();
    }

    private void addDescriptionToTask(Tasks selectedTaskVar, String description) {
        if (selectedTaskVar != null && !description.isEmpty()) {
            try {
                Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
                Connection conn = DriverManager.getConnection(connectURL);

                // Perform database operations here
                String insertQuery = "INSERT INTO " + selectedTaskVar.getName() + " (Description) VALUES (?)";

                try (PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, description);
                    preparedStatement.executeUpdate();

                    // Show a success dialog
                    showSuccessDialog("Description added successfully!");

                    openTableDataWindow(selectedTaskVar.getName());
                    selectedTask = null;
                }

                conn.close(); // Close the connection when done
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error connecting to SQL Server database: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showSuccessDialog("Please enter both task name and description.");
        }
    }

//********************************           Edit Desctription        ***********************************
    public void editDescription(String name) {
        if (selectedTask != null) {
            TextArea tfEditDesc = new TextArea();

            GridPane editDescPane = new GridPane();
            editDescPane.setHgap(10);
            editDescPane.setVgap(10);
            editDescPane.setAlignment(Pos.CENTER);

            Label lbEditDesc = new Label("Edit Task Description:");

            editDescPane.add(lbEditDesc, 0, 1);
            editDescPane.add(tfEditDesc, 1, 1);

            Button btnEditDesc = new Button("Save Changes");
            Button btnCancel = new Button("Cancel");

            editDescPane.add(btnEditDesc, 1, 2);
            editDescPane.add(btnCancel, 0, 2);

            Stage editDescStage = new Stage();

            tfEditDesc.setText(selectedTask.getDescription());

            btnEditDesc.setOnAction(e -> {
                String editedDescription = tfEditDesc.getText().trim();

                if (!editedDescription.isEmpty()) {
                    // Edit the description of the selected task
                    editDescription(selectedTask.getName(), editedDescription);

                    // Close the editDescStage
                    editDescStage.close();
                } else {
                    showSuccessDialog("Please enter a new description.");
                }
            });

            btnCancel.setOnAction(e -> editDescStage.close());

            // Make the editDescStage modal
            editDescStage.initModality(Modality.APPLICATION_MODAL);
            editDescStage.initOwner(primaryStage);

            Scene editDescScene = new Scene(editDescPane, 800, 500);
            editDescStage.setTitle("Edit Task Description");
            editDescStage.setScene(editDescScene);
            editDescStage.showAndWait();
        } else {
            showSuccessDialog("Please select a task to edit its description.");
        }
    }
//********************************           Rename Task        ***********************************

    public void renameTask(String currentname) {
        if (selectedTask != null) {
            TextField tfNewName = new TextField();
            tfNewName.setText(currentname);
            Label lbNewName = new Label("Enter New Task Name:");

            Stage renameTaskStage = new Stage();
            GridPane gpRename = new GridPane();
            Button btnRenameTask = new Button("Rename");
            Button btnCancel = new Button("Cancel");

            gpRename.setHgap(10);
            gpRename.setVgap(10);
            gpRename.setAlignment(Pos.CENTER);

            gpRename.add(lbNewName, 0, 0);
            gpRename.add(tfNewName, 1, 0);
            gpRename.add(btnRenameTask, 1, 2);
            gpRename.add(btnCancel, 0, 2);

            btnRenameTask.setOnAction(e -> {
                String newName = tfNewName.getText().replaceAll("\\s+", "_"); // Replace spaces with underscores

                if (!doesTaskExist(newName)) {
                    // Rename the selected task
                    renameTask(selectedTask.getName(), newName);

                    // Close the renameTask dialog
                    renameTaskStage.close();
                } else {
                    showSuccessDialog("Task with the same name already exists. Choose a different name.");
                }
            });

            btnCancel.setOnAction(e -> renameTaskStage.close());

            // Make the renameTaskStage modal
            renameTaskStage.initModality(Modality.APPLICATION_MODAL);
            renameTaskStage.initOwner(primaryStage);

            Scene renameTaskScene = new Scene(gpRename, 500, 400);
            renameTaskStage.setTitle("Rename Task");
            renameTaskStage.setScene(renameTaskScene);
            renameTaskStage.showAndWait();
        } else {
            showSuccessDialog("Please select a task to rename.");
        }
    }

    public void renameTask(String oldName, String newName) {
        if (!oldName.isEmpty() && !newName.isEmpty()) {
            try {
                Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
                Connection conn = DriverManager.getConnection(connectURL);

                // Rename the table
                String renameQuery = "ALTER TABLE " + oldName + " RENAME TO " + newName;

                try (PreparedStatement preparedStatement = conn.prepareStatement(renameQuery)) {
                    preparedStatement.executeUpdate();

                    // Update the task name in the TableView
                    selectedTask.setName(newName);

                    // Show a success dialog
                    showSuccessDialog("Task renamed successfully!");
                    showTasks();
                }

                conn.close(); // Close the connection when done
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error connecting to SQLite database: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showSuccessDialog("Invalid task names for renaming.");
        }
    }

    public void editDescription(String tableName, String newDescription) {
        if (!tableName.isEmpty() && !newDescription.isEmpty()) {
            try {
                Class.forName("org.sqlite.JDBC"); // Change the JDBC driver class
                Connection conn = DriverManager.getConnection(connectURL);

                // Update the description in the table
                String updateQuery = "UPDATE " + tableName + " SET Description = ?";

                try (PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, newDescription);
                    preparedStatement.executeUpdate();

                    // Show a success dialog
                    showSuccessDialog("Description updated successfully!");
                    openTableDataWindow(tableName);
                }

                conn.close(); // Close the connection when done
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Error connecting to SQLite database: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showSuccessDialog("Invalid table name or description for editing.");
        }
    }

    private void clearSelection() {
        tableView.getSelectionModel().clearSelection();
        selectedTask = null;
    }

//                          *************       Task Class        **********************
    public static void main(String[] args) {
        launch(args);

    }

}

class Tasks {

    private String name;
    private String description;
    private final SimpleIntegerProperty sno;

    public Tasks(int sno, String name, String description) {
        this.sno = new SimpleIntegerProperty(sno);
        this.name = name;
        this.description = description;
    }

    // Existing constructor with two parameters
    public Tasks(String name, String description) {
        this.sno = new SimpleIntegerProperty(0); // Set a default value
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter for sno
    public int getSno() {
        return sno.get();
    }
}

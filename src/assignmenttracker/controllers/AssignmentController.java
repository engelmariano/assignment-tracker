package assignmenttracker.controllers;

import assignmenttracker.repository.AssignmentRepository;
import assignmenttracker.models.Assignment;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import assignmenttracker.models.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

public class AssignmentController implements Initializable {

    @FXML private TableView<Assignment> assignmentsTable;
    @FXML private TableColumn<Assignment, String> colTitle;
    @FXML private TableColumn<Assignment, String> colSubject;
    @FXML private TableColumn<Assignment, String> colDueDate;
    @FXML private TableColumn<Assignment, String> colStatus;
    @FXML private TableColumn<Assignment, Integer> colProgress;
    @FXML private TableColumn<Assignment, Void> colDelete;
    @FXML private TableColumn<Assignment, Void> colEdit;
    @FXML private Label fullNameLabel;
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    private User user;
    private AssignmentRepository repo = new AssignmentRepository();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        assignmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        
        colEdit.setCellFactory(col -> new TableCell<Assignment, Void>() {

            private final Button editBtn = new Button("Edit");
            {
                ImageView editIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/assignmenttracker/images/edit.png"))
                );
                editIcon.setFitWidth(25);
                editIcon.setFitHeight(25);

                editBtn.setGraphic(editIcon);
                editBtn.setStyle("-fx-background-color: transparent;"); // transparent button
                editBtn.setCursor(Cursor.HAND);

                editBtn.setOnAction(event -> {
                    Assignment a = getTableView().getItems().get(getIndex());
                    openEditDialog(a, event);
                    
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }

            private void openEditDialog(Assignment a, ActionEvent event) {
                 try {
                    FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/assignmenttracker/fxml/EditAssignmentFXML.fxml"));
                    Parent root = loader.load();
                    EditAssignmentController editController = loader.getController();
                    editController.setAssignment(a);

                    Stage dialogStage = new Stage();
                    dialogStage.initStyle(StageStyle.UNDECORATED); 
                    dialogStage.initStyle(StageStyle.TRANSPARENT); 
                    dialogStage.initModality(Modality.APPLICATION_MODAL);

                    Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    dialogStage.initOwner(parentStage);

                    Scene scene = new Scene(root);
                    dialogStage.setScene(scene);
                    scene.setFill(Color.TRANSPARENT);

                    dialogStage.showAndWait();
                    loadAssignments();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        colDueDate.setCellFactory(column -> new TableCell<Assignment, String>() {

        @Override
        protected void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);

            if (empty || value == null) {
                setText(null);
                setStyle("");
                return;
            }

            setText(value);

            try {
                // Parse date: yyyy-MM-dd
                LocalDate due = LocalDate.parse(value);
                LocalDate today = LocalDate.now();

                // Get the whole row's Assignment object
                Assignment assignment = getTableView()
                        .getItems()
                        .get(getIndex());

                int progress = assignment.getProgress();

                if (due.isBefore(today) && progress < 100) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            } catch (Exception e) {
                setStyle("");
            }
        }
    });

        
        colProgress.setCellFactory(column -> new TableCell<Assignment, Integer>() {
            private final ProgressBar progressBar = new ProgressBar(0);
            private final Label label = new Label();
            private final StackPane stack = new StackPane(progressBar, label);
            
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    double progress = value / 100.0;

                    // Set bar progress
                    progressBar.setProgress(progress);
                    progressBar.setPrefWidth(130);
                    progressBar.setPrefHeight(30);

                    // Show text inside bar
                    label.setText(value + "%");
                    label.setStyle("-fx-font-size: 11px; -fx-text-fill: black; -fx-font-weight: bold;");

                    // Color bar based on value
                    progressBar.setStyle(getBarColor(progress));

                    setGraphic(stack);
                    setText(null);
                }
            }
            
            
            
            
            private String getBarColor(double p) {
                if (p <= 0.25) {
                    return "-fx-accent: #e63946;";   // red
                } else if (p <= 0.75) {
                    return "-fx-accent: #ffb300;";   // orange
                } else {
                    return "-fx-accent: #2ecc71;";   // green
                }
            }
        });
    
        colDelete.setCellFactory(col -> new TableCell<Assignment, Void>() {

            private final Button deleteBtn = new Button();

            {
                // Load icon
                ImageView trashIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/assignmenttracker/images/trash1.png"))
                );
                trashIcon.setFitWidth(25);
                trashIcon.setFitHeight(25);

                // Apply icon to button
                deleteBtn.setGraphic(trashIcon);
                deleteBtn.setStyle("-fx-background-color: transparent;"); // transparent button
                deleteBtn.setCursor(Cursor.HAND);

                deleteBtn.setOnAction(event -> {
                    Assignment a = getTableView().getItems().get(getIndex());
                    deleteAssignment(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });


    }
    
    private void deleteAssignment(Assignment a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete \"" + a.getTitle() + "\"?");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);

        alert.showAndWait().ifPresent(type -> {
            if (type == yes) {
                repo.delete(a.getId());
                assignmentsTable.getItems().remove(a);
            }
        });
    }
    
    @FXML
    private void handleBackToMain(ActionEvent event) {
        try {
            // Load Main Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignmenttracker/fxml/MainFXML.fxml"));
            Parent root = loader.load();

            // Pass the user back to Main
            MainController controller = loader.getController();
            controller.setUser(user); 
            controller.setUsernameGreeting(); 

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            root.setOnMousePressed(mouseEvent -> {
                xOffset = mouseEvent.getSceneX();
                yOffset = mouseEvent.getSceneY();
            });

            root.setOnMouseDragged(mouseEvent -> {
                stage.setX(mouseEvent.getScreenX() - xOffset);
                stage.setY(mouseEvent.getScreenY() - yOffset);
            });

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            scene.getStylesheets().add(
                getClass().getResource("/assignmenttracker/css/styles.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.show();

            Stage current = (Stage) ((Node) event.getSource()).getScene().getWindow();
            current.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddAssignment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/assignmenttracker/fxml/AddAssignmentFXML.fxml"));
            Parent root = loader.load();
            AddAssignmentController addController = loader.getController();
            addController.setUser(this.user); 

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED); 
            dialogStage.initStyle(StageStyle.TRANSPARENT); 
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            dialogStage.initOwner(parentStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            scene.setFill(Color.TRANSPARENT);
            
            dialogStage.showAndWait();
            loadAssignments();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadAssignments() {
        ObservableList<Assignment> data = repo.getAssignmentsByUser(this.user.getId());
        assignmentsTable.setItems(data);
    }
    
    public void setStudentName(User user) {
        fullNameLabel.setText(this.user.getFullName());
    }


    public void setUser(User user) {
        this.user = user;
        loadAssignments();
    }
    
    @FXML
    public void handleExit(ActionEvent event) {
        Platform.exit();
    }
    
}

package assignmenttracker.controllers;

import assignmenttracker.models.Assignment;
import assignmenttracker.services.AssignmentService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;
import assignmenttracker.models.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

public class MainController implements Initializable{
    
    @FXML
    private Label usernameContainer; 
    @FXML
    private ListView<Object> importantListView;
    private double xOffset = 0;
    private double yOffset = 0;
    
    private final AssignmentService assignmentService = new AssignmentService();
    private User user;
    
    private enum AssignmentGroup {
        OVERDUE,
        DUE_SOON,
        HIGH_PRIORITY
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        importantListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
         
            }
        });

        importantListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // double-click
                Object selected = importantListView.getSelectionModel().getSelectedItem();
                if (selected instanceof DashboardItem di) {
                    openEditDialog(di.getAssignment());
                }
            }
        });
    }  
  
    
    private void openEditDialog(Assignment assignment) {

        try {
            FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/assignmenttracker/fxml/EditAssignmentFXML.fxml"));
            Parent root = loader.load();
            EditAssignmentController editController = loader.getController();
            editController.setAssignment(assignment);
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT); 
            
            Stage parentStage = (Stage) importantListView.getScene().getWindow();
            dialogStage.initOwner(parentStage);
  
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
           
            // After closing, refresh dashboard
            loadImportantAssignments();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private static class DashboardHeader {
        private final String title;
        
        public DashboardHeader(String title) { 
            this.title = title; 
        }
        
        public String getTitle() { 
            return title; 
        }
    }

    
    private static class DashboardItem {
        private final Assignment assignment;
        private final AssignmentGroup group;

        public DashboardItem(Assignment assignment, AssignmentGroup group) {
            this.assignment = assignment;
            this.group = group;
        }

        public Assignment getAssignment() { 
            return assignment; 
        }
        
        public AssignmentGroup getGroup() { 
            return group; 
        }
    }
    
    @FXML
    private void handleGoToAssignments(ActionEvent event) {
        try {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignmenttracker/fxml/AssignmentFXML.fxml"));
        Parent root = loader.load();
        
        AssignmentController controller = loader.getController();
        controller.setUser(user); 
        controller.setStudentName(user); 
        
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED); 
        newStage.initStyle(StageStyle.TRANSPARENT); 
        
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        
        root.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        root.setOnMouseDragged(e -> {
            newStage.setX(e.getScreenX() - xOffset);
            newStage.setY(e.getScreenY() - yOffset);
        });
           
        newStage.setScene(scene);
        newStage.show();

        // Close the current (Main) stage
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void loadImportantAssignments() {
        if (user == null) return;

        var important = assignmentService.getImportantAssignmentsForUser(user.getId());

        List<DashboardItem> overdue = new ArrayList<>();
        List<DashboardItem> dueSoon = new ArrayList<>();
        List<DashboardItem> highPriority = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);

        for (Assignment a : important) {
            LocalDate due = null;
            if (a.getDueDate() != null && !a.getDueDate().isBlank()) {
                try {
                    due = LocalDate.parse(a.getDueDate());
                } catch (Exception ignored) {}
            }

            boolean isOverdue = due != null && due.isBefore(today) && a.getProgress() < 100;
            boolean isDueSoon = due != null && !due.isBefore(today) && !due.isAfter(soon);
            boolean isTopPriority = a.getPriority() == 1;

            if (isOverdue) {
                overdue.add(new DashboardItem(a, AssignmentGroup.OVERDUE));
            } else if (isDueSoon) {
                dueSoon.add(new DashboardItem(a, AssignmentGroup.DUE_SOON));
            } else if (isTopPriority) {
                highPriority.add(new DashboardItem(a, AssignmentGroup.HIGH_PRIORITY));
            }
        }

        // sort each group by due date
        Comparator<DashboardItem> byDueDate = (d1, d2) -> {
            LocalDate ld1 = parseDateSafe(d1.getAssignment().getDueDate());
            LocalDate ld2 = parseDateSafe(d2.getAssignment().getDueDate());
            return ld1.compareTo(ld2);
        };

        overdue.sort(byDueDate);
        dueSoon.sort(byDueDate);
        highPriority.sort(byDueDate);

        ObservableList<Object> displayItems = FXCollections.observableArrayList();
        int remaining = 5; // limit to top 5 assignments total

        remaining = addGroupWithLimit("Overdue", overdue, displayItems, remaining);
        if (remaining > 0) remaining = addGroupWithLimit("Due Soon (Next 3 days)", dueSoon, displayItems, remaining);
        if (remaining > 0) remaining = addGroupWithLimit("High Priority", highPriority, displayItems, remaining);

        importantListView.setItems(displayItems);
        setupImportantListCellFactory();
    }

    
    private LocalDate parseDateSafe(String s) {
        if (s == null || s.isBlank()) return LocalDate.MAX;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.MAX;
        }
    }

    // Helper method: add header + items
    private int addGroupWithLimit(String title,
                                  List<DashboardItem> items,
                                  ObservableList<Object> display,
                                  int remaining) {
        if (items.isEmpty() || remaining <= 0) return remaining;

        display.add(new DashboardHeader(title));

        int count = 0;
        for (DashboardItem di : items) {
            if (count >= remaining) break;
            display.add(di);
            count++;
        }

        return remaining - count;
    }


    private void setupImportantListCellFactory() {
        importantListView.setCellFactory(list -> new ListCell<Object>() {

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                // Header row
                if (item instanceof DashboardHeader header) {
                    Label headerLabel = new Label(header.getTitle());
                    headerLabel.getStyleClass().add("dashboard-header");
                    setGraphic(headerLabel);
                    setText(null);
                    return;
                }

                // Assignment row
                if (item instanceof DashboardItem di) {
                    Assignment a = di.getAssignment();

                    Label titleLabel = new Label(a.getTitle());
                    Label metaLabel = new Label(buildMetaText(a));
                    ProgressBar progressBar = new ProgressBar(a.getProgress() / 100.0);
                    Label progressLabel = new Label(a.getProgress() + "%");

                    progressBar.setPrefWidth(140);

                    HBox progressBox = new HBox(8, progressBar, progressLabel);
                    progressBox.getStyleClass().add("important-progress-box");

                    VBox container = new VBox(2, titleLabel, metaLabel, progressBox);
                    container.getStyleClass().add("important-item");

                    String baseStyle = "-fx-padding: 8; -fx-background-radius: 10;";
                    switch (di.getGroup()) {
                        case OVERDUE -> container.setStyle(baseStyle + "-fx-background-color: #FF6962;");
                        case DUE_SOON -> container.setStyle(baseStyle + "-fx-background-color: #FFE08E;");
                        case HIGH_PRIORITY -> container.setStyle(baseStyle + "-fx-background-color: #FFB346;");
                    }

                    titleLabel.getStyleClass().add("important-title");
                    metaLabel.getStyleClass().add("important-meta");

                    setGraphic(container);
                    setText(null);
                }
            }

            private String buildMetaText(Assignment a) {
                String dueText = (a.getDueDate() != null && !a.getDueDate().isBlank())
                        ? "Due " + a.getDueDate()
                        : "No due date";
                return a.getSubject() + " • " + dueText;
            }
        });
    }


    
    
    @FXML
    public void handleExit(ActionEvent event) {
        Platform.exit();
    }
    
    public void setUser(User user) {
        this.user = user;
        loadImportantAssignments();
    }
    
    public void setUsernameGreeting() {
        usernameContainer.setText("Hi " + capitalizeFirstLetter(this.user.getUsername()) + "!");
    }
    
    public String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    
}

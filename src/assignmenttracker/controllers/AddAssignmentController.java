package assignmenttracker.controllers;

import assignmenttracker.models.User;
import assignmenttracker.services.AssignmentService;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AddAssignmentController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField subjectField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private Label subjectError;
    @FXML private Label titleError;
    @FXML private Label dueDateError;

    private User user;

    private final AssignmentService assignmentService = new AssignmentService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusCombo.getItems().addAll("Pending", "In Progress", "Done");
        statusCombo.setValue("Pending");

        priorityCombo.getItems().addAll("1 - High", "2 - Normal", "3 - Low");
        priorityCombo.setValue("3 - Low");
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        String subject = subjectField.getText();
        String description = "";
        LocalDate dueDate = dueDatePicker.getValue();
        String status = statusCombo.getValue();
        String priorityString = priorityCombo.getValue();

        boolean flagError = false;

        if (title == null || title.isBlank()) {
            titleError.setText("Required Field!");
            flagError = true;
        } else {
            titleError.setText("");
        }

        if (subject == null || subject.isBlank()) {
            subjectError.setText("Required Field!");
            flagError = true;
        } else {
            subjectError.setText("");
        }

        if (dueDate == null) {
            dueDateError.setText("Required Field!");
            flagError = true;
        } else {
            dueDateError.setText("");
        }

        if (flagError) {
            return;
        }

        int priorityFinal = Integer.parseInt(priorityString.substring(0, 1));

        assignmentService.createAssignment(
                user,
                title,
                subject,
                description,
                dueDate,
                status,
                priorityFinal
        );
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}

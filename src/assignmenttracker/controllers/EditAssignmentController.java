package assignmenttracker.controllers;

import assignmenttracker.models.Assignment;
import assignmenttracker.services.AssignmentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;

public class EditAssignmentController {

    @FXML private TextField titleField;
    @FXML private TextField subjectField;
    @FXML private DatePicker dueDatePicker;
    @FXML private Slider progressSlider;
    @FXML private Label progressLabel;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private Label subjectError;
    @FXML private Label titleError;
    @FXML private Label dueDateError;

    private final AssignmentService assignmentService = new AssignmentService();
    private Assignment assignment;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Pending", "In Progress", "Done");
        priorityCombo.getItems().addAll("1 - High", "2 - Normal", "3 - Low");

        if (progressSlider != null) {
            progressSlider.setMin(0);
            progressSlider.setMax(100);
            progressSlider.setBlockIncrement(5);
            progressSlider.setMajorTickUnit(25);
            progressSlider.setMinorTickCount(4);
            progressSlider.setShowTickMarks(true);
            progressSlider.setShowTickLabels(true);

            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int value = newVal.intValue();
                if (progressLabel != null) {
                    progressLabel.setText(value + "%");
                }
            });
            
            progressSlider.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!progressSlider.isPressed()) {
                e.consume();
            }
        });
        }
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;

        titleField.setText(assignment.getTitle());
        subjectField.setText(assignment.getSubject());

        if (assignment.getDueDate() != null) {
            dueDatePicker.setValue(LocalDate.parse(assignment.getDueDate()));
        }

        statusCombo.setValue(assignment.getStatus());

        String priorityLabel = switch (assignment.getPriority()) {
            case 1 -> "1 - High";
            case 2 -> "2 - Normal";
            case 3 -> "3 - Low";
            default -> "3 - Low";
        };
        
        priorityCombo.setValue(priorityLabel);

        if (progressSlider != null) {
            progressSlider.setValue(assignment.getProgress());
            if (progressLabel != null) {
                progressLabel.setText(assignment.getProgress() + "%");
            }
        }
    }

    @FXML
    private void handleSave() {
        if (assignment == null) return;

        String title = titleField.getText();
        String subject = subjectField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        String status = statusCombo.getValue();
        String priorityString = priorityCombo.getValue();
        int progress = (int) Math.round(progressSlider.getValue());

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

        assignment.setTitle(title);
        assignment.setSubject(subject);
        assignment.setDueDate(dueDate.toString());
        assignment.setStatus(status);
        assignment.setPriority(priorityFinal);
        assignment.setProgress(progress);
        assignmentService.updateAssignment(assignment);
       
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}

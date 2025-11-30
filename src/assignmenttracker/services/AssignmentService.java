package assignmenttracker.services;

import assignmenttracker.models.Assignment;
import assignmenttracker.models.User;
import assignmenttracker.repository.AssignmentRepository;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.time.LocalDate;

public class AssignmentService {

    private final AssignmentRepository repo = new AssignmentRepository();

    public ObservableList<Assignment> getAssignmentsForUser(int userId) {
        return repo.getAssignmentsByUser(userId);
    }

    public boolean createAssignment(
            User user,
            String title,
            String subject,
            String description,
            LocalDate dueDate,
            String status,
            int priority 
    ) {
        
       
        int progress = 0; 

        Assignment a = new Assignment(
                0,                          // id (auto-incremens in database)
                title,
                description != null ? description : "",
                subject,
                dueDate.toString(),         // yyyy-MM-dd
                status,
                progress,
                priority,
                user.getId()
        );

        repo.insert(a);
        return true;
    }

    public ObservableList<Assignment> getImportantAssignmentsForUser(int userId) {
        ObservableList<Assignment> all = repo.getAssignmentsByUser(userId);
        ObservableList<Assignment> important = FXCollections.observableArrayList();

        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3); //  get next 3 days

        for (Assignment a : all) {
            
            if ("Done".equalsIgnoreCase(a.getStatus())) {
                continue;
            }
             
            LocalDate due = null;
            if (a.getDueDate() != null && !a.getDueDate().isBlank()) {
                try {
                    due = LocalDate.parse(a.getDueDate()); // yyyy-MM-dd
                } catch (Exception ignored) {}
            }

            boolean isTopPriority = a.getPriority() == 1;
            boolean isOverdue = (due != null && due.isBefore(today) && a.getProgress() < 100);
            boolean isDueSoon = (due != null && !due.isBefore(today) && !due.isAfter(soon));

            if (isTopPriority || isOverdue || isDueSoon) {
                important.add(a);
            }
        }

        // sort by due date
        important.sort((a1, a2) -> {
            LocalDate d1 = a1.getDueDate() != null && !a1.getDueDate().isBlank()
                    ? LocalDate.parse(a1.getDueDate()) : LocalDate.MAX;
            LocalDate d2 = a2.getDueDate() != null && !a2.getDueDate().isBlank()
                    ? LocalDate.parse(a2.getDueDate()) : LocalDate.MAX;
            return d1.compareTo(d2);
        });

        return important;
    }
    
    public boolean updateAssignment(Assignment a) {
        if (a == null) return false;
        if (a.getTitle() == null || a.getTitle().isBlank()) return false;
        if (a.getSubject() == null || a.getSubject().isBlank()) return false;
        if (a.getDueDate() == null) return false;
        if (a.getProgress() < 0 || a.getProgress() > 100) return false;

        repo.update(a);
        return true;
    }

    public void deleteAssignment(int id) {
        repo.delete(id);
    }
}

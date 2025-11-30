package assignmenttracker.models;

public class Assignment {
    private final int id;
    private final int userId;
    private String title;
    private String description;
    private String subject;
    private String dueDate;
    private String status;
    private int priority;
    private int progress;

    public Assignment(int id, String title, String description, String subject, String dueDate, 
                      String status, int progress, int priority, int userId) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.dueDate = dueDate;
        this.status = status;
        this.progress = progress;
        this.priority = priority;
        this.userId = userId;
        
    }

    
    public int getId() { 
        return id; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public String getSubject() { 
        return subject; 
    }
    
    public String getDueDate() { 
        return dueDate; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public int getProgress() {
        return progress;
    }
    
    public int getPriority() { 
        return priority; 
    }
    
    public int getUserId() { 
        return userId; 
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}


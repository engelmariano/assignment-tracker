package assignmenttracker.controllers;

import assignmenttracker.services.UserService;
import assignmenttracker.models.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController implements Initializable {
    @FXML
    private Label authErrMessage;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    
    //user for initial positioning of the screen
    private double xOffset = 0; 
    private double yOffset = 0;
    
    private final UserService userService = new UserService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        authErrMessage.setText("");
    }  
    
    @FXML
    private void handleLogin(ActionEvent event) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
               authErrMessage.setStyle("-fx-text-fill: red");
               authErrMessage.setText("Username or Password cannot be empty!");
               
            } else {
                User user = userService.checkLogin(username, password);

                if (user != null) {
                    authErrMessage.setStyle("-fx-text-fill: green");
                    authErrMessage.setText("Login successful!");
                    
                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        
                        closeLoginWindow();
                        openMainWindow(user); //pass the user object to Main Window
                       
                    });
                    pause.play(); //used this delay show success message when login is succesful
        
                } else {
                    authErrMessage.setStyle("-fx-text-fill: red");
                    authErrMessage.setText("Invalid username or password.");
                }
            }
               
    }
    
    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignmenttracker/fxml/MainFXML.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.setUser(user);
            controller.setUsernameGreeting();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT); 
               
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
           
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            scene.getStylesheets().add(getClass().getResource("/assignmenttracker/css/styles.css").toExternalForm());
            stage.show();
            
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void closeLoginWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    public void handleExit(ActionEvent event) {
        Platform.exit();
    }
    
}

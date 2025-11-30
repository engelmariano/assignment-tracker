package assignmenttracker.services;

import assignmenttracker.models.User;
import assignmenttracker.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    public User checkLogin(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
        
    }
}

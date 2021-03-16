package pl.coderslab.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import pl.coderslab.entity.User;

public interface UserService extends UserDetailsService {
    User findUserByEmail(String email);
    void save(User user);
    boolean verifyPassword(String rawPassword, String hashedPassword);
    String hashPassword(String rawPassword);
}

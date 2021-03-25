package my.application.service;

import my.application.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User findUserByEmail(String email);
    void save(User user);
    boolean verifyPassword(String rawPassword, String hashedPassword);
    String hashPassword(String rawPassword);
}

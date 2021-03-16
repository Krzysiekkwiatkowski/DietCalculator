package pl.coderslab.pojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.coderslab.entity.User;
import pl.coderslab.repository.UserRepository;

import javax.annotation.PostConstruct;

@Component
public class ContextHelper {

    private static UserRepository userRepository;

    @Autowired
    private UserRepository autowiredRepository;

    @PostConstruct
    private void init(){
        this.userRepository = autowiredRepository;
    }

    public static User getUserFromContext(){
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        org.springframework.security.core.userdetails.User sessionUser = (org.springframework.security.core.userdetails.User) object;
        return userRepository.findTopByEmail(sessionUser.getUsername());
    }
}

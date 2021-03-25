package my.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/diet")
public class HomeController {

    @RequestMapping("/home")
    public String home(HttpSession session){
        Object sessionObject = session.getAttribute("user");
        if(sessionObject == null){
            return "redirect:/diet/user/login";
        }
        return "home";
    }
}

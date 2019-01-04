package pl.coderslab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/diet")
public class HomeController {
    @RequestMapping("/home")
    public String home(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        return "home";
    }
}

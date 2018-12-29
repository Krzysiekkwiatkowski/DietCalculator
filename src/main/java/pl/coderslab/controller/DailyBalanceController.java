package pl.coderslab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/diet/daily")
@Controller
public class DailyBalanceController {

    @RequestMapping("/add")
    public String update(){
        return "home";
    }
}

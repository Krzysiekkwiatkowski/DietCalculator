package my.application.controller;

import my.application.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import my.application.entity.Category;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/diet/category")
@Controller
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model){
        model.addAttribute("addCategory", "addCategory");
        model.addAttribute("category", new Category());
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@Valid Category category, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("addCategory", "addCategory");
            return "home";
        }
        categoryRepository.save(category);
        return "home";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editGet(@PathVariable("id") Long id, Model model){
        model.addAttribute("editCategory", "editCategory");
        model.addAttribute("category", categoryRepository.findById(id));
        return "home";
    }

    @RequestMapping(value = "/edit/*", method = RequestMethod.POST)
    public String editPost(@Valid Category category, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("editCategory", "editCategory");
            return "home";
        }
        categoryRepository.save(category);
        return "home";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String all(Model model){
        model.addAttribute("allCategories", allCategories());
        return "home";
    }

    @RequestMapping(value = "/option", method = RequestMethod.GET)
    public String option(Model model){
        model.addAttribute("categoryOption","categoryOption");
        return "home";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String deleteGet(@PathVariable("id") Long id, Model model){
        model.addAttribute("allCategories", "allCategories");
        model.addAttribute("confirm", id);
        return "home";
    }

    @RequestMapping(value = "/delete/{id}/yes", method = RequestMethod.GET)
    public String deleteConfirm(@PathVariable("id") Long id){
        categoryRepository.deleteById(id);
        return "redirect:/diet/category/all";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories(){
        List<Category> categories = categoryRepository.findAll();
        categories.sort((c1,c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return categories;
    }
}

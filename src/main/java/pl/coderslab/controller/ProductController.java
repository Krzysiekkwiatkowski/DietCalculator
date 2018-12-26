package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.coderslab.entity.Category;
import pl.coderslab.entity.Product;
import pl.coderslab.repository.CategoryRepository;
import pl.coderslab.repository.ProductRepository;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/diet/product")
@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("categories", allCategories());
        return "addProduct";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@Valid Product product, BindingResult result){
        if(result.hasErrors()){
            return "addProduct";
        }
        productRepository.save(product);
        return "home";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories(){
        return categoryRepository.findAll();
    }
}

package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String addPost(@Valid Product product, BindingResult result, Model model){
        if(result.hasErrors()){
            return "addProduct";
        }
        if((product.getProtein() + product.getCarbohydrates() + product.getFat()) > 100){
            model.addAttribute("mistake", "mistake");
            return "addProduct";
        }
        product.setWeight(100);
        productRepository.save(product);
        return "home";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editGet(@PathVariable("id") Long id, Model model){
        model.addAttribute("product", productRepository.findById(id));
        return "editProduct";
    }

    @RequestMapping(value = "/edit/*", method = RequestMethod.POST)
    public String editPost(@Valid Product product, BindingResult result){
        if(result.hasErrors()){
            return "editProduct";
        }
        productRepository.save(product);
        return "home";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String all(Model model){
        model.addAttribute("products", allProducts());
        return "home";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") Long id){
        productRepository.deleteById(id);
        return "home";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories(){
        return categoryRepository.findAll();
    }

    @ModelAttribute("products")
    public List<Product> allProducts(){
        return productRepository.findAll();
    }
}

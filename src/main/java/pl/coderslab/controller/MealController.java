package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pl.coderslab.entity.Category;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.Product;
import pl.coderslab.repository.CategoryRepository;
import pl.coderslab.repository.MealRepository;
import pl.coderslab.repository.ProductRepository;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/meal")
@Controller
public class MealController {
    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model, HttpSession session){
        Object object = session.getAttribute("meal");
        if(object != null){
            List<Product> mealProducts = (List<Product>)object;
            model.addAttribute("mealProducts", mealProducts);
        }
        model.addAttribute("categories", allCategories());
        return "selectCategory";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@ModelAttribute Category category, Model model){
        model.addAttribute("product", new Product());
        model.addAttribute("productList", allProductsByCategory(category.getId()));
        return "selectProduct";
    }

    @RequestMapping(value = "/addProduct", method = RequestMethod.POST)
    public String addProductPost(@ModelAttribute Product product, HttpSession session, Model model){
        Product loadedProduct = productRepository.findTopById(product.getId());
        Object object = session.getAttribute("meal");
        List<Product> mealProducts;
        if(object == null){
            mealProducts = new ArrayList<>();
        } else {
            mealProducts = (List<Product>)object;
        }
        mealProducts.add(loadedProduct);
        model.addAttribute("mealProducts", mealProducts);
        session.setAttribute("meal", mealProducts);
        return "redirect:/diet/meal/add";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories(){
        return categoryRepository.findAll();
    }

    @ModelAttribute("productList")
    public List<Product> allProductsByCategory(Long id) {
        return productRepository.findByCategory(categoryRepository.findTopById(id));
    }
}

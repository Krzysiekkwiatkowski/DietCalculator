package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.Category;
import pl.coderslab.entity.Product;
import pl.coderslab.repository.CategoryRepository;
import pl.coderslab.repository.ProductRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/product")
@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", allCategories());
        model.addAttribute("addProduct", "addProduct");
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@Valid Product product, BindingResult result, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if (result.hasErrors()) {
            model.addAttribute("addProduct", "addProduct");
            return "home";
        }
        if ((product.getProtein() + product.getCarbohydrates() + product.getFat()) > 100) {
            model.addAttribute("addProduct", "addProduct");
            model.addAttribute("mistake", "mistake");
            return "home";
        }
        product.setWeight(100);
        productRepository.save(product);
        return "home";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editGet(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("editProduct", "editProduct");
        model.addAttribute("product", productRepository.findTopById(id));
        return "home";
    }

    @RequestMapping(value = "/edit/*", method = RequestMethod.POST)
    public String editPost(@Valid Product product, BindingResult result, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if (result.hasErrors()) {
            model.addAttribute("editProduct", "editProduct");
            return "home";
        }
        productRepository.save(product);
        return "home";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestParam("name") String name, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        if (name.equals("")) {
            return "redirect:/diet/product/all";
        }
        model.addAttribute("search", "search");
        model.addAttribute("logged", "logged");
        model.addAttribute("allProducts", "allProducts");
        model.addAttribute("products", productRepository.findByNameContaining(name));
        return "home";
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String all(HttpServletRequest request, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        int count = productRepository.countAllProduct();
        List<Product> limitedProducts = null;
        List<Integer> pages = new ArrayList<>();
        int numberOfPages = count / 12;
        Integer page = 0;
        if (count > 0) {
            String exist = request.getParameter("page");
            if (exist == null) {
                limitedProducts = productRepository.findAll(12, 0);
            } else {
                page = Integer.parseInt(exist);
                limitedProducts = productRepository.findAll(12, (page - 1) * 12);
            }
            if (count % 12 != 0) {
                for (int i = 0; i < numberOfPages + 1; i++) {
                    pages.add(i, i + 1);
                }
            } else {
                for (int i = 0; i < numberOfPages; i++) {
                    pages.add(i, i + 1);
                }
            }
        }
        model.addAttribute("pages", pages);
        model.addAttribute("logged", "logged");
        model.addAttribute("allProducts", "allProducts");
        model.addAttribute("limitedProducts", limitedProducts);
        model.addAttribute("page", (page == 0 ? 1 : page));
        return "home";
    }

    @RequestMapping(value = "/option", method = RequestMethod.GET)
    public String option(Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("productOption", "productOption");
        return "home";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String deleteGet(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("confirm", id);
        model.addAttribute("search", "search");
        model.addAttribute("logged", "logged");
        model.addAttribute("allProducts", "allProducts");
        return "home";
    }

    @RequestMapping(value = "/delete/{id}/yes", method = RequestMethod.GET)
    public String deleteConfirm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        productRepository.deleteById(id);
        return "redirect:/diet/product/all";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories() {
        List<Category> categories = categoryRepository.findAll();
        categories.sort((c1,c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return categories;
    }

    @ModelAttribute("products")
    public List<Product> allProducts() {
        List<Product> products = productRepository.findAll();
        products.sort((p1,p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return products;
    }

}

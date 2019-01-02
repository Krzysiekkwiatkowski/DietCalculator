package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.*;
import pl.coderslab.repository.*;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/meal")
@Controller
public class MealController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model, HttpSession session) {
        Object object = session.getAttribute("meal");
        if (object != null) {
            List<Product> mealProducts = (List<Product>) object;
            model.addAttribute("mealProducts", mealProducts);
        }
        model.addAttribute("categories", allCategories());
        return "selectCategory";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@ModelAttribute Category category, Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("productList", allProductsByCategory(category.getId()));
        return "selectProduct";
    }

    @RequestMapping(value = "/addProduct", method = RequestMethod.POST)
    public String addProductPost(@RequestParam("weight") int weight, @ModelAttribute Product product, HttpSession session, Model model) {
        Product loadedProduct = productRepository.findTopById(product.getId());
        double multiplier = weight / 100.0;
        loadedProduct.setProtein(loadedProduct.getProtein() * multiplier);
        loadedProduct.setCarbohydrates(loadedProduct.getCarbohydrates() * multiplier);
        loadedProduct.setFat(loadedProduct.getFat() * multiplier);
        loadedProduct.setCalories((int) (loadedProduct.getCalories() * multiplier));
        loadedProduct.setWeight(weight);
        Object object = session.getAttribute("meal");
        List<Product> mealProducts;
        if (object == null) {
            mealProducts = new ArrayList<>();
        } else {
            mealProducts = (List<Product>) object;
        }
        mealProducts.add(loadedProduct);
        model.addAttribute("mealProducts", mealProducts);
        session.setAttribute("meal", mealProducts);
        return "redirect:/diet/meal/add";
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public String confirmGet(HttpSession session) {
        Object objectMeal = session.getAttribute("meal");
        Object objectUser = session.getAttribute("user");
        if (objectMeal != null && objectUser != null) {
            User user = (User) objectUser;
            User loadedUser = userRepository.findTopByEmail(user.getEmail());
            List<DailyBalance> dailyBalances;
            DailyBalance dailyBalance = null;
            List<Meal> meals = null;
            if(loadedUser.getDailyBalances() == null){
                dailyBalances = new ArrayList<>();
            } else {
                dailyBalances = loadedUser.getDailyBalances();
            }
            List<Product> mealProducts = (List<Product>) objectMeal;
            Meal meal = new Meal();
            double proteinSum = 0.0;
            double carbohydratesSum = 0.0;
            double fatSum = 0.0;
            int caloriesSum = 0;
            for (Product product : mealProducts) {
                proteinSum += product.getProtein();
                carbohydratesSum += product.getCarbohydrates();
                fatSum += product.getFat();
                caloriesSum += product.getCalories();
            }
            meal.setProducts(mealProducts);
            meal.setTotalProtein(proteinSum);
            meal.setTotalCarbohydrates(carbohydratesSum);
            meal.setTotalFat(fatSum);
            meal.setTotalCalories(caloriesSum);
            int exist = dailyBalanceRepository.countByUserIdAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            if (exist == 1) {
                dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
                meals = dailyBalance.getMeals();
                if (meals.size() == 0) {
                    meals = new ArrayList<>();
                    dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                    dailyBalance.setUser(loadedUser);
                    dailyBalance.setNeeded(loadedUser.getTotalCalories() + loadedUser.getTraining().getDailyCalories());
                } else {
                    meals = dailyBalance.getMeals();
                }
            } else {
                dailyBalance = new DailyBalance();
                meals = new ArrayList<>();
                dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                dailyBalance.setUser(loadedUser);
                if(loadedUser.getTraining() != null){
                    dailyBalance.setNeeded(loadedUser.getTotalCalories() + loadedUser.getTraining().getDailyCalories());
                } else {
                    dailyBalance.setNeeded(loadedUser.getTotalCalories());
                }
            }
            dailyBalance.setReceived(dailyBalance.getReceived() + meal.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            meal.setMealNumber(meals.size() + 1);
            meals.add(meal);
            dailyBalance.setMeals(meals);
            dailyBalances.add(dailyBalance);
            loadedUser.setDailyBalances(dailyBalances);
            mealRepository.save(meal);
            dailyBalanceRepository.save(dailyBalance);
            userRepository.save(loadedUser);
            session.removeAttribute("meal");
        }
        return "home";
    }

    @RequestMapping("/view/{id}")
    public String viewMeal(@PathVariable("id") Long id, Model model){
        Meal meal = mealRepository.findTopById(id);
        model.addAttribute("meal", meal);
        return "viewMeal";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories() {
        return categoryRepository.findAll();
    }

    @ModelAttribute("productList")
    public List<Product> allProductsByCategory(Long id) {
        return productRepository.findByCategory(categoryRepository.findTopById(id));
    }
}
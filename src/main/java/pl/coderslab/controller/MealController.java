package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.*;
import pl.coderslab.repository.*;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.text.DecimalFormat;
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
        Object objectMeal = session.getAttribute("meal");
        Object objectUser = session.getAttribute("user");
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if (objectMeal != null) {
            List<Product> mealProducts = (List<Product>) objectMeal;
            model.addAttribute("selectCategory", "selectCategory");
            model.addAttribute("mealProducts", mealProducts);
            model.addAttribute("categories", allCategories());
            return "home";
        }
        model.addAttribute("selectCategory", "selectCategory");
        model.addAttribute("categories", allCategories());
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@ModelAttribute Category category, Model model, HttpSession session) {
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("selectProduct", "selectProduct");
        model.addAttribute("product", new Product());
        model.addAttribute("productList", allProductsByCategory(category.getId()));
        return "home";
    }

    @RequestMapping(value = "/addProduct", method = RequestMethod.POST)
    public String addProductPost(@RequestParam("weight") int weight, @ModelAttribute Product product, HttpSession session, Model model) {
        Object objectUser = session.getAttribute("user");
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
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
    public String confirmGet(HttpSession session, Model model) {
        Object objectMeal = session.getAttribute("meal");
        Object objectUser = session.getAttribute("user");
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if (objectMeal != null) {
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
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            meal.setProducts(mealProducts);
            meal.setTotalProtein(Double.parseDouble(decimalFormat.format(proteinSum).replace(",", ".")));
            meal.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(carbohydratesSum).replace(",", ".")));
            meal.setTotalFat(Double.parseDouble(decimalFormat.format(fatSum).replace(",", ".")));
            meal.setTotalCalories(caloriesSum);
            int exist = dailyBalanceRepository.countByUserIdAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            if (exist == 1) {
                dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
                meals = dailyBalance.getMeals();
                if (meals.size() == 0) {
                    meals = new ArrayList<>();
                    dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                    dailyBalance.setUser(loadedUser);
                    if(loadedUser.getTraining() != null) {
                        dailyBalance.setNeeded(loadedUser.getTotalCalories() + loadedUser.getTraining().getDailyCalories());
                    } else {
                        dailyBalance.setNeeded(loadedUser.getTotalCalories());
                    }
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
        return "redirect:/diet/home";
    }

    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) != null){
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            List<Meal> meals = dailyBalance.getMeals();
            Meal toDelete = null;
            for (Meal meal : meals) {
                if(meal.getId() == id){
                    mealRepository.delete(meal);
                    toDelete = meal;
                }
            }
            if(toDelete != null){
                meals.remove(toDelete);
            }
            int totalReceived = 0;
            for (Meal meal : meals) {
                totalReceived += meal.getTotalCalories();
            }
            dailyBalance.setReceived(totalReceived);
            dailyBalance.setBalance(totalReceived - dailyBalance.getNeeded());
            dailyBalance.setMeals(meals);
            dailyBalanceRepository.save(dailyBalance);

        }
        return "redirect:/diet/home";
    }

    @RequestMapping("/view/{id}")
    public String viewMeal(@PathVariable("id") Long id, Model model, HttpSession session){
        Object objectUser = session.getAttribute("user");
        Object objectMeal = mealRepository.findTopById(id);
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(objectMeal == null){
            model.addAttribute("exist", null);
            model.addAttribute("viewMeal", "viewMeal");
            return "home";
        }
        User user = (User)objectUser;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object object = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())).getMeals();
        if(object != null) {
            for (Meal searchMeal : (List<Meal>)object) {
                if(searchMeal.getId() == id){
                    Meal meal = (Meal)objectMeal;
                    model.addAttribute("exist", "exist");
                    model.addAttribute("meal", meal);
                    model.addAttribute("viewMeal", "viewMeal");
                    return "home";
                }
            }
        }
        model.addAttribute("exist", null);
        model.addAttribute("viewMeal", "viewMeal");
        return "home";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories() {
        return categoryRepository.findAll();
    }

    @ModelAttribute("products")
    public List<Product> allProducts() {
        return productRepository.findAll();
    }

    @ModelAttribute("productList")
    public List<Product> allProductsByCategory(Long id) {
        return productRepository.findByCategory(categoryRepository.findTopById(id));
    }
}
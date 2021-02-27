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
import java.util.*;

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
    public String addGet(Model model, HttpSession session){
        Object objectUser = session.getAttribute("user");
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        Object objectMeal = session.getAttribute("mealProducts");
        if(objectMeal != null){
            List<Product> mealProducts = (List<Product>) objectMeal;
            model.addAttribute("mealProducts", mealProducts);
        }
        model.addAttribute("categories", allCategories());
        model.addAttribute("products", allProducts());
        model.addAttribute("addMeal", "addMeal");
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@RequestParam("id") long id, @RequestParam("weight") int weight, Model model, HttpSession session){
        System.out.println("\n\n\nID: " + id + ", WEIGHT: " + weight + "\n\n\n");
        Object objectUser = session.getAttribute("user");
        if(objectUser == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        Object objectMeal = session.getAttribute("mealProducts");
        List<Product> products;
        if(objectMeal != null){
            products = (List<Product>) objectMeal;
        } else {
            products = new ArrayList<>();
        }
        Product product = productRepository.findTopById(id);
        double multiplier = weight / 100.0;
        product.setProtein(product.getProtein() * multiplier);
        product.setCarbohydrates(product.getCarbohydrates() * multiplier);
        product.setFat(product.getFat() * multiplier);
        product.setCalories((int) (product.getCalories() * multiplier));
        product.setWeight(weight);
        products.add(product);
        session.setAttribute("mealProducts", products);
        model.addAttribute("product", new Product());
        return "redirect:/diet/meal/add";
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.GET)
    public String confirmGet(HttpSession session, Model model) {
        Object objectMeal = session.getAttribute("mealProducts");
        Object objectUser = session.getAttribute("user");
        if (objectUser == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if (objectMeal != null) {
            User user = (User) objectUser;
            User loadedUser = userRepository.findTopByEmail(user.getEmail());
            List<DailyBalance> dailyBalances;
            DailyBalance dailyBalance;
            List<Meal> meals;
            if (loadedUser.getDailyBalances() == null) {
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
            double glycemicChargeSum = 0.0;
            for (Product product : mealProducts) {
                proteinSum += product.getProtein();
                carbohydratesSum += product.getCarbohydrates();
                fatSum += product.getFat();
                caloriesSum += product.getCalories();
                glycemicChargeSum += (product.getCarbohydrates() * product.getGlycemicIndex()) / 100.0;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            meal.setProducts(mealProducts);
            meal.setTotalProtein(Double.parseDouble(decimalFormat.format(proteinSum).replace(",", ".")));
            meal.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(carbohydratesSum).replace(",", ".")));
            meal.setTotalFat(Double.parseDouble(decimalFormat.format(fatSum).replace(",", ".")));
            meal.setTotalCalories(caloriesSum);
            meal.setGlycemicCharge(glycemicChargeSum);
            int exist = dailyBalanceRepository.countByUserIdAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            if (exist == 1) {
                dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
                meals = dailyBalance.getMeals();
                if (meals.size() == 0) {
                    meals = new ArrayList<>();
                    dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                    dailyBalance.setUser(loadedUser);
                    dailyBalance.setNeeded(loadedUser.getTotalCalories());
                } else {
                    meals = dailyBalance.getMeals();
                }
            } else {
                dailyBalance = new DailyBalance();
                meals = new ArrayList<>();
                dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                dailyBalance.setUser(loadedUser);
                dailyBalance.setNeeded(loadedUser.getTotalCalories());
            }
            dailyBalance.setReceived(dailyBalance.getReceived() + meal.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalance.setTotalProtein(loadedUser.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(loadedUser.getTotalCarbohydrates());
            dailyBalance.setTotalFat(loadedUser.getTotalFat());
            int lastMeal = 0;
            if(dailyBalance.getMeals() != null) {
                for (Meal check : dailyBalance.getMeals()) {
                    if (check.getMealNumber() > lastMeal) {
                        lastMeal = check.getMealNumber();
                    }
                }
                meal.setMealNumber(lastMeal + 1);
            } else {
                meal.setMealNumber(lastMeal + 1);
            }
            meals.add(meal);
            dailyBalance.setMeals(meals);
            dailyBalances.add(dailyBalance);
            loadedUser.setDailyBalances(dailyBalances);
            mealRepository.save(meal);
            dailyBalanceRepository.save(dailyBalance);
            userRepository.save(loadedUser);
            session.removeAttribute("mealProducts");
        } else {
            model.addAttribute("selectCategory", "selectCategory");
            model.addAttribute("categories", allCategories());
            model.addAttribute("emptyValue", "emptyValue");
            return "home";
        }
        return "redirect:/diet/home";
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
        model.addAttribute("mealOption", "mealOption");
        return "home";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") Long id, Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User) object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object objectDaily = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
        if (objectDaily != null) {
            DailyBalance dailyBalance = (DailyBalance) objectDaily;
            List<Meal> meals = dailyBalance.getMeals();
            model.addAttribute("meals", meals);
            model.addAttribute("exist", "exist");
            model.addAttribute("confirm", id);
            model.addAttribute("viewMeals", "viewMeals");
            return "home";
        }
        return "redirect:/diet/meal/view";
    }

    @RequestMapping(value = "/delete/{id}/yes", method = RequestMethod.GET)
    public String deleteConfirm(@PathVariable("id") Long id, HttpSession session, Model model) {
        Object object = session.getAttribute("user");
        if (object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User) object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if (dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            List<Meal> meals = dailyBalance.getMeals();
            Meal toDelete = null;
            for (Meal meal : meals) {
                if (meal.getId().equals(id)) {
                    toDelete = meal;
                    mealRepository.delete(meal);
                }
            }
            if (toDelete != null) {
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
        return "redirect:/diet/meal/view";
    }

    @RequestMapping("/view")
    public String viewMeals(Model model, HttpSession session) {
        Object objectUser = session.getAttribute("user");
        if (objectUser == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User) objectUser;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object object = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
        if (object != null) {
            DailyBalance dailyBalance = (DailyBalance)object;
            List<Meal> meals = mealRepository.findAllById(dailyBalance.getId());
            if(meals.size() == 0){
                model.addAttribute("exist", null);
                model.addAttribute("viewMeals", "viewMeals");
                return "home";
            }
            model.addAttribute("exist", "exist");
            model.addAttribute("meals", meals);
            model.addAttribute("viewMeals", "viewMeals");
            return "home";
        }
        model.addAttribute("exist", null);
        model.addAttribute("viewMeals", "viewMeals");
        return "home";
    }

    @RequestMapping("/view/{mealNumber}")
    public String viewMeal(@PathVariable("mealNumber") Integer mealNumber, Model model, HttpSession session) {
        Object objectUser = session.getAttribute("user");
        if (objectUser == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User) objectUser;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object object = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())).getMeals();
        if (object != null) {
            List<Meal> meals = (List<Meal>) object;
            for (Meal meal : meals) {
                if(meal.getMealNumber() == mealNumber) {
                    model.addAttribute("meal", meal);
                    model.addAttribute("viewMeal", "viewMeal");
                    return "home";
                }
            }
        }
        return "redirect:/diet/meal/option";
    }

    @RequestMapping("/plan")
    public String plan(Model model, HttpSession session){
        Object objectUser = session.getAttribute("user");
        if (objectUser == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        return "redirect:/diet/home";
    }

    @ModelAttribute("categories")
    public List<Category> allCategories() {
        List<Category> categories = categoryRepository.findAll();
        categories.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return categories;
    }

    @ModelAttribute("products")
    public List<Product> allProducts() {
        List<Product> products = productRepository.findAll();
        products.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return products;
    }

    @ModelAttribute("productList")
    public List<Product> allProductsByCategory(Long id) {
        List<Product> productList = productRepository.findByCategory(categoryRepository.findTopById(id));
        productList.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return productList;
    }

    @ModelAttribute("categoriesProducts")
    public Map<Category, List<Product>> allCategoriesAndItsProducts(){
        Map<Category, List<Product>> categoriesProducts = new LinkedHashMap<>();
        allCategories().forEach((c) -> {
            List<Product> products = allProductsByCategory(c.getId());
            if (products != null) {
                categoriesProducts.put(c, products);
            } else {
                categoriesProducts.put(c, new ArrayList<>());
            }
        });
        return categoriesProducts;
    }
}
package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.*;
import pl.coderslab.helper.ContextHelper;
import pl.coderslab.pojo.GraphResult;
import pl.coderslab.pojo.MissingMacro;
import pl.coderslab.repository.*;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@RequestMapping("/diet/meal")
@Controller
public class MealController {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

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
        if (objectMeal != null) {
            User user = ContextHelper.getUserFromContext();
            List<DailyBalance> dailyBalances;
            DailyBalance dailyBalance;
            List<Meal> meals;
            if (user.getDailyBalances() == null) {
                dailyBalances = new ArrayList<>();
            } else {
                dailyBalances = user.getDailyBalances();
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
            int exist = dailyBalanceRepository.countByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            if (exist == 1) {
                dailyBalance = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
                meals = dailyBalance.getMeals();
                if (meals.size() == 0) {
                    meals = new ArrayList<>();
                    dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                    dailyBalance.setUser(user);
                    dailyBalance.setNeeded(user.getTotalCalories());
                } else {
                    meals = dailyBalance.getMeals();
                }
            } else {
                dailyBalance = new DailyBalance();
                meals = new ArrayList<>();
                dailyBalance.setDate(Date.valueOf(LocalDate.now()));
                dailyBalance.setUser(user);
                dailyBalance.setNeeded(user.getTotalCalories());
            }
            dailyBalance.setReceived(dailyBalance.getReceived() + meal.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalance.setTotalProtein(user.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(user.getTotalCarbohydrates());
            dailyBalance.setTotalFat(user.getTotalFat());
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
            user.setDailyBalances(dailyBalances);
            mealRepository.save(meal);
            dailyBalanceRepository.save(dailyBalance);
            userRepository.save(user);
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
    public String option(Model model) {
        model.addAttribute("mealOption", "mealOption");
        return "home";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") Long id, Model model){
        User user = ContextHelper.getUserFromContext();
        Object objectDaily = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
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
    public String deleteConfirm(@PathVariable("id") Long id) {
        User user = ContextHelper.getUserFromContext();
        if (dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
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
    public String viewMeals(Model model) {
        User user = ContextHelper.getUserFromContext();
        Object object = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
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
    public String viewMeal(@PathVariable("mealNumber") Integer mealNumber, Model model) {
        User user = ContextHelper.getUserFromContext();
        Object object = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now())).getMeals();
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

    @RequestMapping(value = "/plan", method = RequestMethod.GET)
    public String planGet(Model model, HttpSession session){
        User user = ContextHelper.getUserFromContext();
        MissingMacro missingMacro = getMissingMacro(user.getId());
        if(missingMacro != null){
            List<GraphResult> graphResults = new ArrayList<>();
            graphResults.add(new GraphResult("Białko: 0/" + replaceComma(missingMacro.getProtein() + ""), "0%", "width: 0px; background-color: green;", true));
            graphResults.add(new GraphResult("Węglowodany: 0/" + replaceComma(missingMacro.getCarbohydrates() + ""), "0%", "width: 0px; background-color: red;", true));
            graphResults.add(new GraphResult("Tłuszcz: 0/" + replaceComma(missingMacro.getFat() + ""), "0%", "width: 0px; background-color: yellow;", true));
            graphResults.add(new GraphResult("Kalorie: 0/" + missingMacro.getCalories(), "0%", "width: 0px; background-color: blue;", true));
            graphResults.add(new GraphResult("%", "", "", false));
            graphResults.add(new GraphResult("Ładunek posiłku: ", "0.0","width: 0px; background-color: orange;", true));
            graphResults.add(new GraphResult("", "", "", false));
            model.addAttribute("graphResults", graphResults);
        }
        Object chosenProductsObject = session.getAttribute("chosenProducts");
        if(chosenProductsObject != null){
            List<Product> chosenProducts = (List<Product>) chosenProductsObject;
            model.addAttribute("chosenProducts", chosenProducts);
        }
        model.addAttribute("categories", allCategories());
        model.addAttribute("products", allProducts());
        model.addAttribute("missingMacro", missingMacro);
        model.addAttribute("planning", "planning");
        return "home";
    }

    @RequestMapping(value = "/plan", method = RequestMethod.POST)
    public String planPost(@RequestParam("id") long id, HttpSession session){
        Object chosenProductsObject = session.getAttribute("chosenProducts");
        List<Product> chosenProducts;
        if(chosenProductsObject != null){
            chosenProducts = (List<Product>) chosenProductsObject;
        } else {
            chosenProducts = new ArrayList<>();
        }
        Product product = productRepository.findTopById(id);
        chosenProducts.add(product);
        session.setAttribute("chosenProducts", chosenProducts);
        return "redirect:/diet/meal/plan";
    }

    @RequestMapping(value = "/plan/delete/{productId}", method = RequestMethod.GET)
    public String delete(@PathVariable("productId") long id, HttpSession session){
        Object chosenProductsObject = session.getAttribute("chosenProducts");
        List<Product> chosenProducts = null;
        if(chosenProductsObject != null){
            chosenProducts = (List<Product>) chosenProductsObject;
            int index = -1;
            for (Product product : chosenProducts) {
                if(product.getId() == id){
                    index = chosenProducts.indexOf(product);
                }
            }
            if(index != -1){
                chosenProducts.remove(index);
            }
        }
        session.setAttribute("chosenProducts", chosenProducts);
        return "redirect:/diet/meal/plan";
    }

    @RequestMapping(value = "/plan/option", method = RequestMethod.GET)
    public String backToOptions(HttpSession session){
        Object chosenMealsObject = session.getAttribute("chosenProducts");
        if(chosenMealsObject != null){
            session.removeAttribute("chosenProducts");
        }
        return "redirect:/diet/meal/option";
    }

    private MissingMacro getMissingMacro(long userId){
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndDate(userRepository.findTopById(userId).getId(), Date.valueOf(LocalDate.now()));
        if(dailyBalance != null){
            double receivedProtein = 0.0;
            double receivedCarbohydrates = 0.0;
            double receivedFat = 0.0;
            List<Meal> meals = dailyBalance.getMeals();
            if(meals != null && meals.size() > 0){
                for(Meal meal : meals){
                    receivedProtein += meal.getTotalProtein();
                    receivedCarbohydrates += meal.getTotalCarbohydrates();
                    receivedFat += meal.getTotalFat();
                }
                return new MissingMacro(Double.parseDouble(DECIMAL_FORMAT.format((dailyBalance.getTotalProtein() - receivedProtein)).replace(",", ".")),
                        Double.parseDouble(DECIMAL_FORMAT.format((dailyBalance.getTotalCarbohydrates() - receivedCarbohydrates)).replace(",", ".")),
                                Double.parseDouble(DECIMAL_FORMAT.format((dailyBalance.getTotalFat() - receivedFat)).replace(",", ".")),
                                                dailyBalance.getNeeded() - dailyBalance.getReceived());
            }
        }
        return null;
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

    private String replaceComma(String number){
        return number.replace(",", ".");
    }
}
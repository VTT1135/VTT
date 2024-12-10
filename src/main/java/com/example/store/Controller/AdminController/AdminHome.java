package com.example.store.Controller.AdminController;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@CrossOrigin("*")
public class AdminHome {
    @GetMapping("/AdminHome")
    public String adminHome() {
        return "/Admin-views/index";
    }
    @GetMapping("/Admin-views/Admin-Statistics.html")
    public String adminStatistics() {
        return "Admin-views/Admin-Statistics";
    }

    @GetMapping("/Admin-views/Admin-Users.html")
    public String adminUser() {
        return "Admin-views/Admin-Users";
    }

    @GetMapping("/Admin-views/Admin-Products.html")
    public String adminProducts() {
        return "Admin-views/Admin-Products";
    }

    @GetMapping("/Admin-views/Admin-Categories.html")
    public String adminCategories() {
        return "Admin-views/Admin-Categories";
    }

    @GetMapping("/Admin-views/Admin-Brands.html")
    public String adminBrands() {
        return "Admin-views/Admin-Brands";
    }

    @GetMapping("/Admin-views/Admin-Promotions.html")
    public String adminPromotions() {
        return "Admin-views/Admin-Promotions";
    }

    @GetMapping("/Admin-views/Admin-Orders.html")
    public String adminOrders() {
        return "Admin-views/Admin-Orders";
    }

}

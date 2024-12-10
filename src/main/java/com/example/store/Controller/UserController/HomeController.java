package com.example.store.Controller.UserController;


import com.example.store.Entity.User;
import com.example.store.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;


@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;



    @GetMapping({"/", "/index"})
    public String home(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            // Lấy thông tin người dùng từ cơ sở dữ liệu
            Optional<User> optionalUser = userRepository.findById(userId);

            if (optionalUser.isPresent()) {
                User currentUser = optionalUser.get();
                model.addAttribute("userId", currentUser.getId());
                model.addAttribute("username", currentUser.getUsername());
            }
        } else {
            model.addAttribute("userId", null);
            model.addAttribute("username", null);
        }

        return "index";
    }


    @GetMapping("/views/home.html")
    public String home(){
        return "views/home";
    }

    @GetMapping("/views/about.html")
    public String about(){
        return "views/about";
    }

    @GetMapping("/views/news.html")
    public String news(){
        return "views/news";
    }


    @GetMapping("/views/contact.html")
    public String contact(){
        return "views/contact";
    }


    @GetMapping("/views/shop.html")
    public String shop(){
        return "views/shop";
    }


    @GetMapping("/views/product-details.html")
    public String productDetails() {
        return "views/product-details";
    }


    @GetMapping("/views/cart.html")
    public String cart(){
        return "views/cart";
    }

    @GetMapping("/views/order.html")
    public String order(){
        return "views/order";
    }


    @GetMapping("/views/order-history.html")
    public String orderHistory(){
        return "views/order-history";
    }

    @GetMapping("/views/address.html")
    public String address(){
        return "views/address";
    }


}

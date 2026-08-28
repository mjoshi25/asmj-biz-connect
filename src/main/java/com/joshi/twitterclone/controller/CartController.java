package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.cart.Cart;
import com.joshi.twitterclone.model.cart.Order;
import com.joshi.twitterclone.service.CartService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User currentUser = userService.getUserByUsername(username);
        Cart cart = cartService.getOrCreateCart(username);
        List<Order> myOrders = cartService.getUserOrders(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cart", cart);
        model.addAttribute("orders", myOrders);
        model.addAttribute("userHasAddress", currentUser.getLocation() != null && !currentUser.getLocation().isBlank());

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("itemId") String itemId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "redirect", defaultValue = "/cart") String redirect) {
        cartService.addToCart(userDetails.getUsername(), itemId, quantity);
        return "redirect:" + redirect;
    }

    @PostMapping("/update")
    public String updateQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("itemId") String itemId,
                                 @RequestParam("quantity") int quantity) {
        cartService.updateItemQuantity(userDetails.getUsername(), itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("itemId") String itemId) {
        cartService.removeFromCart(userDetails.getUsername(), itemId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam("fullName") String fullName,
                           @RequestParam("phone") String phone,
                           @RequestParam("streetAddress") String streetAddress,
                           @RequestParam("city") String city,
                           @RequestParam("state") String state,
                           @RequestParam("postalCode") String postalCode) {
        cartService.checkout(userDetails.getUsername(), fullName, phone, streetAddress, city, state, postalCode);
        return "redirect:/cart?ordered=true";
    }
}
package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.service.CartService;
import com.joshi.twitterclone.service.MarketplaceService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String viewMarketplace(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(value = "tab", defaultValue = "shop") String tab,
                                  @RequestParam(value = "category", required = false) ProductCategory category,
                                  @RequestParam(value = "city", required = false) String city,
                                  Model model) {
        String username = userDetails != null ? userDetails.getUsername() : "";
        User currentUser = userService.getUserByUsername(username);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab != null ? tab.trim().toLowerCase() : "shop");
        model.addAttribute("products", marketplaceService.getApprovedProducts(category));
        model.addAttribute("vehicles", marketplaceService.getApprovedVehicles(city));
        model.addAttribute("insuranceAds", marketplaceService.getApprovedInsuranceAds());
        model.addAttribute("cart", cartService.getOrCreateCart(username));
        model.addAttribute("selectedCity", city != null ? city : "");
        model.addAttribute("selectedCategory", category);

        return "marketplace";
    }

    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("productId") String productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            Model model) {
        ShoppingCart cart = cartService.addToCart(userDetails.getUsername(), productId, quantity);
        model.addAttribute("cart", cart);
        return "fragments/cart-drawer :: cart-drawer-content";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("productId") String productId,
                                 @RequestParam("delta") int delta,
                                 Model model) {
        ShoppingCart cart = cartService.updateItemQuantity(userDetails.getUsername(), productId, delta);
        model.addAttribute("cart", cart);
        return "fragments/cart-drawer :: cart-drawer-content";
    }

    @PostMapping("/cart/remove")
    public String removeCartItem(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("productId") String productId,
                                 Model model) {
        ShoppingCart cart = cartService.removeItem(userDetails.getUsername(), productId);
        model.addAttribute("cart", cart);
        return "fragments/cart-drawer :: cart-drawer-content";
    }

    @PostMapping("/cart/checkout")
    public String checkoutOrder(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("buyerName") String name,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                @RequestParam(value = "paymentMethod", defaultValue = "DIRECT_INVOICE") String paymentMethod) {
        cartService.checkoutOrder(userDetails.getUsername(), name, phone, address, paymentMethod);
        return "redirect:/messages";
    }

    @PostMapping("/products/post")
    public String postProduct(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute MarketplaceProduct product,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        marketplaceService.createProduct(userDetails.getUsername(), product, images);
        return "redirect:/marketplace?tab=shop&posted=true";
    }

    @PostMapping("/vehicles/post")
    public String postVehicle(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute VehicleListing listing,
                              @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        marketplaceService.createVehicleListing(userDetails.getUsername(), listing, images);
        return "redirect:/marketplace?tab=vehicles&posted=true";
    }

    @PostMapping("/vehicles/book")
    public String bookVehicle(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("listingId") String listingId,
                              @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam("customerPhone") String phone,
                              @RequestParam("drivingLicenseNumber") String license) {
        marketplaceService.bookVehicle(userDetails.getUsername(), listingId, startDate, endDate, phone, license);
        return "redirect:/messages";
    }

    @PostMapping("/insurance/post")
    public String postInsuranceAd(@AuthenticationPrincipal UserDetails userDetails,
                                  @ModelAttribute InsuranceAd ad,
                                  @RequestParam(value = "banner", required = false) MultipartFile banner,
                                  @RequestParam(value = "brochure", required = false) MultipartFile brochure) {
        marketplaceService.createInsuranceAd(userDetails.getUsername(), ad, banner, brochure);
        return "redirect:/marketplace?tab=insurance&posted=true";
    }

    @PostMapping("/insurance/quote")
    public String requestInsuranceQuote(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam("adId") String adId,
                                        @RequestParam("name") String name,
                                        @RequestParam("email") String email,
                                        @RequestParam("phone") String phone,
                                        @RequestParam("age") int age,
                                        @RequestParam(value = "estimatedValue", defaultValue = "0") BigDecimal estimatedValue) {
        marketplaceService.calculateAndRequestQuote(userDetails.getUsername(), adId, name, email, phone, age, estimatedValue);
        return "redirect:/messages";
    }

    @GetMapping("/admin/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAdminModeration(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userService.getUserByUsername(userDetails.getUsername()));
        model.addAttribute("pendingProducts", marketplaceService.getPendingProducts());
        model.addAttribute("pendingVehicles", marketplaceService.getPendingVehicles());
        model.addAttribute("pendingAds", marketplaceService.getPendingInsuranceAds());
        return "admin-moderation";
    }

    @PostMapping("/admin/products/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateProduct(@PathVariable("id") String id,
                                  @RequestParam("approve") boolean approve,
                                  @RequestParam(value = "rejectionReason", required = false) String reason) {
        marketplaceService.moderateProduct(id, approve, reason);
        return "redirect:/marketplace/admin/moderation";
    }

    @PostMapping("/admin/vehicles/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateVehicle(@PathVariable("id") String id,
                                  @RequestParam("approve") boolean approve,
                                  @RequestParam(value = "rejectionReason", required = false) String reason) {
        marketplaceService.moderateVehicle(id, approve, reason);
        return "redirect:/marketplace/admin/moderation";
    }

    @PostMapping("/admin/insurance/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateInsurance(@PathVariable("id") String id,
                                    @RequestParam("approve") boolean approve,
                                    @RequestParam(value = "rejectionReason", required = false) String reason) {
        marketplaceService.moderateInsuranceAd(id, approve, reason);
        return "redirect:/marketplace/admin/moderation";
    }
}
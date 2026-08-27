package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.InsuranceAd;
import com.joshi.twitterclone.model.marketplace.VehicleListing;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.service.EventService;
import com.joshi.twitterclone.service.MarketplaceService;
import com.joshi.twitterclone.service.ProductServiceItemService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
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
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final ProductServiceItemService productServiceItemService;
    private final UserService userService;
    private final EventService eventService;

    @GetMapping
    public String viewMarketplace(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(value = "tab", defaultValue = "products") String tab,
                                  @RequestParam(value = "category", required = false) ItemCategory category,
                                  @RequestParam(value = "city", required = false) String city,
                                  Model model) {
        String username = userDetails != null ? userDetails.getUsername() : "";
        User currentUser = userService.getUserByUsername(username);

        List<ProductServiceItem> products = productServiceItemService.getApprovedItems(category, city);
        List<VehicleListing> vehicles = marketplaceService.getApprovedVehicles(city);
        List<InsuranceAd> insuranceAds = marketplaceService.getApprovedInsuranceAds();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab != null ? tab.trim().toLowerCase() : "products");
        model.addAttribute("products", products != null ? products : Collections.emptyList());
        model.addAttribute("vehicles", vehicles != null ? vehicles : Collections.emptyList());
        model.addAttribute("insuranceAds", insuranceAds != null ? insuranceAds : Collections.emptyList());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCity", city != null ? city : "");
        model.addAttribute("upcomingEvents", eventService.getTopUpcomingEvents(3));

        return "marketplace";
    }

    @GetMapping("/manage")
    public String manageMarketplaceVendorPortal() {
        return "redirect:/products-services/manage";
    }

    // --- Product & Service Posting from Marketplace Tab ---

    @PostMapping("/products/post")
    public String postProductService(@AuthenticationPrincipal UserDetails userDetails,
                                     @ModelAttribute ProductServiceItem item,
                                     @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        productServiceItemService.createListing(userDetails.getUsername(), item, images);
        return "redirect:/marketplace?tab=products&posted=true";
    }

    // --- Vehicle Rental Endpoints ---

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

    // --- Insurance Policy & Quote Endpoints ---

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

    // --- Admin Moderation Endpoints ---

    @GetMapping("/admin/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAdminModeration(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("currentUser", userService.getUserByUsername(userDetails.getUsername()));
        model.addAttribute("pendingVehicles", marketplaceService.getPendingVehicles());
        model.addAttribute("pendingAds", marketplaceService.getPendingInsuranceAds());
        model.addAttribute("pendingProducts", productServiceItemService.getPendingItems());
        return "admin-moderation";
    }

    @PostMapping("/admin/vehicles/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateVehicle(@PathVariable("id") String id,
                                  @RequestParam("approve") boolean approve,
                                  @RequestParam(value = "rejectionReason", required = false) String reason) {
        marketplaceService.moderateVehicle(id, approve, reason);
        return "redirect:/admin/analytics";
    }

    @PostMapping("/admin/insurance/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateInsurance(@PathVariable("id") String id,
                                    @RequestParam("approve") boolean approve,
                                    @RequestParam(value = "rejectionReason", required = false) String reason) {
        marketplaceService.moderateInsuranceAd(id, approve, reason);
        return "redirect:/admin/analytics";
    }
}
package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductListing;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.service.FileStorageService;
import com.joshi.twitterclone.service.ProductServiceItemService;
import com.joshi.twitterclone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/products-services")
@RequiredArgsConstructor
public class ProductServiceController {

    private final ProductServiceItemService itemService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String viewCatalog(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(value = "category", required = false) ItemCategory category,
                              @RequestParam(value = "city", required = false) String city,
                              Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<ProductServiceItem> items = itemService.getApprovedItems(category, city);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("items", items != null ? items : Collections.emptyList());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCity", city != null ? city : "");

        return "products-services";
    }

    @PostMapping("/post")
    public String postItem(@AuthenticationPrincipal UserDetails userDetails,
                           @ModelAttribute ProductServiceItem item,
                           @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        itemService.createListing(userDetails.getUsername(), item, images);
        return "redirect:/products-services/manage?posted=true";
    }

    @PostMapping("/inquire")
    public String inquireItem(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("itemId") String itemId,
                              @RequestParam("message") String message) {
        itemService.sendInquiry(userDetails.getUsername(), itemId, message);
        return "redirect:/messages";
    }

    @GetMapping("/manage")
    public String viewVendorDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        List<ProductServiceItem> myListings = itemService.getVendorListings(userDetails.getUsername());

        long approvedCount = myListings.stream().filter(i -> "APPROVED".equalsIgnoreCase(i.getStatus().name())).count();
        long pendingCount = myListings.stream().filter(i -> "PENDING_APPROVAL".equalsIgnoreCase(i.getStatus().name())).count();
        long rejectedCount = myListings.stream().filter(i -> "REJECTED".equalsIgnoreCase(i.getStatus().name())).count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("myListings", myListings);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);

        return "product-manage";
    }

    @PostMapping("/admin/{id}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateItem(@PathVariable("id") String id,
                               @RequestParam("approve") boolean approve,
                               @RequestParam(value = "rejectionReason", required = false) String reason) {
        itemService.moderateItem(id, approve, reason);
        return "redirect:/admin/analytics";
    }
    
    @PostMapping("/update/{id}")
    public String updateProduct(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") String id,
                                @ModelAttribute ProductListing updatedProduct,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        
        String username = userDetails != null ? userDetails.getUsername() : "";
        String imageUrl = (imageFile != null && !imageFile.isEmpty()) ? fileStorageService.saveFile(imageFile) : null;
        
        itemService.updateProduct(id, username, updatedProduct, imageUrl);
        return "redirect:/products-services/manage?updated=true";
    }
}
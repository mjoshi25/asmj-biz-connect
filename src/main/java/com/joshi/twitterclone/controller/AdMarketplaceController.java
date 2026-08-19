package com.joshi.twitterclone.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.service.AdMarketplaceService;
import com.joshi.twitterclone.service.TweetService;
import com.joshi.twitterclone.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdMarketplaceController {

    private final AdMarketplaceService adMarketplaceService;
    private final TweetService tweetService;
    private final UserService userService;

    // Public Ad Marketplace (Approved Ads only)
    @GetMapping
    public String viewPublicMarketplace(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String currentUsername = userDetails != null ? userDetails.getUsername() : null;

        model.addAttribute("ads", adMarketplaceService.getApprovedPublicAds());
        model.addAttribute("myCampaigns", currentUsername != null ? adMarketplaceService.getAdvertiserCampaigns(currentUsername) : List.of());
        model.addAttribute("trending", tweetService.getTrendingHashtags());
        model.addAttribute("suggestedUsers", currentUsername != null ? userService.getSuggestedUsersToFollow(currentUsername, 4) : List.of());

        return "marketplace";
    }

    // Modal: Advertiser Submit Form
    @GetMapping("/create")
    public String getCreateAdModal() {
        return "fragments/ad-modals :: create-ad-modal";
    }

    @PostMapping("/create")
    public String submitAd(@RequestParam("title") String title,
                           @RequestParam(value = "description", required = false) String description,
                           @RequestParam("targetUrl") String targetUrl,
                           @RequestParam(value = "media", required = false) MultipartFile media,
                           @AuthenticationPrincipal UserDetails userDetails) {
        adMarketplaceService.submitAdvertisement(userDetails.getUsername(), title, description, targetUrl, media);
        return "redirect:/ads";
    }

    // One-click: User initiates conversation with advertiser about a specific product
    @PostMapping("/{adId}/inquire")
    public String inquireAboutAd(@PathVariable("adId") String adId,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Conversation convo = adMarketplaceService.startUserToAdvertiserChat(userDetails.getUsername(), adId);
        return "redirect:/messages?convo=" + convo.getId();
    }

    // Admin Control Panel (Secured with ROLE_ADMIN)
    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewPendingApprovals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("pendingAds", adMarketplaceService.getPendingApprovalAds());
        return "ad-moderation";
    }

    @PostMapping("/admin/review/{adId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String reviewAd(@PathVariable("adId") String adId,
                           @RequestParam("approve") boolean approve,
                           @RequestParam(value = "feedback", required = false) String feedback,
                           @AuthenticationPrincipal UserDetails userDetails) {
        adMarketplaceService.reviewAdvertisement(adId, userDetails.getUsername(), approve, feedback);
        return "redirect:/ads/admin/reviews";
    }
}
package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.repository.marketplace.ProductServiceInquiryRepository;
import com.joshi.twitterclone.repository.marketplace.ProductServiceListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceManager {

    private final ProductServiceListingRepository listingRepository;
    private final ProductServiceInquiryRepository inquiryRepository;
    private final UserService userService;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    public ProductServiceListing createListing(String username, ProductServiceListing listing, List<MultipartFile> images, MultipartFile brochure) {
        User user = userService.getUserByUsername(username);

        listing.setVendorUsername(user.getUsername());
        listing.setVendorDisplayName(user.getDisplayName());
        listing.setStatus(ListingStatus.PENDING_APPROVAL);
        listing.setInquiryCount(0);
        listing.setCreatedAt(LocalDateTime.now());

        List<String> uploadedImages = new ArrayList<>();
        if (images != null) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    uploadedImages.add(fileStorageService.saveImageOptimized(img));
                }
            }
        }
        listing.setImageUrls(uploadedImages);

        if (brochure != null && !brochure.isEmpty()) {
            listing.setBrochurePdfUrl(fileStorageService.saveFile(brochure));
        }

        return listingRepository.save(listing);
    }

    public List<ProductServiceListing> getApprovedOfferings(ProductServiceType type, String city) {
        if (type != null) {
            return listingRepository.findByStatusAndOfferingTypeOrderByCreatedAtDesc(ListingStatus.APPROVED, type);
        }
        if (city != null && !city.isBlank()) {
            return listingRepository.findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus.APPROVED, city.trim());
        }
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public List<ProductServiceListing> getListingsByVendor(String vendorUsername) {
        return listingRepository.findByVendorUsernameOrderByCreatedAtDesc(vendorUsername);
    }

    public ProductServiceInquiry submitInquiry(String buyerUsername, String listingId, String name, String email, 
                                               String phone, String company, int qty, BigDecimal budget, String requirements) {
        ProductServiceListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product/Service listing not found"));

        if (listing.getStatus() != ListingStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Listing is not available for inquiries");
        }

        ProductServiceInquiry inquiry = ProductServiceInquiry.builder()
                .listingId(listingId)
                .listingTitle(listing.getTitle())
                .buyerUsername(buyerUsername)
                .buyerName(name)
                .buyerEmail(email)
                .buyerPhone(phone)
                .buyerCompanyName(company)
                .vendorUsername(listing.getVendorUsername())
                .requestedQuantity(qty)
                .proposedBudget(budget)
                .projectRequirements(requirements)
                .status("NEW")
                .createdAt(LocalDateTime.now())
                .build();

        ProductServiceInquiry saved = inquiryRepository.save(inquiry);

        listing.setInquiryCount(listing.getInquiryCount() + 1);
        listingRepository.save(listing);

        // Open chat channel with vendor
        Conversation convo = directMessageService.getOrCreateDirectConversation(buyerUsername, listing.getVendorUsername());
        String msg = String.format("Hi! I submitted a B2B inquiry for your listing '%s'. Company: %s | Qty/Scope: %d. Let's connect!",
                listing.getTitle(), company != null ? company : "Direct Client", qty);
        directMessageService.sendMessage(buyerUsername, convo.getId(), msg, null);

        return saved;
    }

    public List<ProductServiceInquiry> getInquiriesForVendor(String vendorUsername) {
        return inquiryRepository.findByVendorUsernameOrderByCreatedAtDesc(vendorUsername);
    }

    public List<ProductServiceInquiry> getInquiriesByBuyer(String buyerUsername) {
        return inquiryRepository.findByBuyerUsernameOrderByCreatedAtDesc(buyerUsername);
    }

    // --- Admin Moderation ---

    public List<ProductServiceListing> getPendingListings() {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateListing(String listingId, boolean approve, String rejectionReason) {
        ProductServiceListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        listing.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) listing.setRejectionReason(rejectionReason);
        listingRepository.save(listing);
    }
}
package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.repository.products.ProductServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceItemService {

    private final ProductServiceRepository itemRepository;
    private final UserService userService;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    public ProductServiceItem createListing(String username, ProductServiceItem item, List<MultipartFile> images) {
        User user = userService.getUserByUsername(username);

        item.setVendorUsername(user.getUsername());
        item.setVendorDisplayName(user.getDisplayName());
        item.setStatus(ListingStatus.PENDING_APPROVAL);
        item.setCreatedAt(LocalDateTime.now());

        List<String> uploadedUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    uploadedUrls.add(fileStorageService.saveImageOptimized(img));
                }
            }
        }
        item.setImageUrls(uploadedUrls);

        return itemRepository.save(item);
    }

    public List<ProductServiceItem> getApprovedItems(ItemCategory category, String city) {
        if (city != null && !city.isBlank()) {
            return itemRepository.findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus.APPROVED, city.trim());
        }
        if (category != null) {
            return itemRepository.findByStatusAndCategoryOrderByCreatedAtDesc(ListingStatus.APPROVED, category);
        }
        return itemRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
    }

    public List<ProductServiceItem> getVendorListings(String username) {
        return itemRepository.findByVendorUsernameOrderByCreatedAtDesc(username);
    }

    public List<ProductServiceItem> getPendingItems() {
        return itemRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.PENDING_APPROVAL);
    }

    public void moderateItem(String itemId, boolean approve, String rejectionReason) {
        ProductServiceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        item.setStatus(approve ? ListingStatus.APPROVED : ListingStatus.REJECTED);
        if (!approve) {
            item.setRejectionReason(rejectionReason);
        }
        itemRepository.save(item);
    }

    public void sendInquiry(String buyerUsername, String itemId, String inquiryMessage) {
        ProductServiceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        Conversation convo = directMessageService.getOrCreateDirectConversation(buyerUsername, item.getVendorUsername());
        String fullMsg = String.format("Inquiry regarding '%s' (₹%s): %s", item.getTitle(), item.getPrice(), inquiryMessage);
        directMessageService.sendMessage(buyerUsername, convo.getId(), fullMsg, null);
    }
}
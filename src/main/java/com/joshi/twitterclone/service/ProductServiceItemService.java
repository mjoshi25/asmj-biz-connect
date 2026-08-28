package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DirectMessageService directMessageService;

    public ProductServiceItem createListing(String username, ProductServiceItem item, List<MultipartFile> images) {
        User user = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        item.setVendorUsername(user.getUsername());
        item.setVendorDisplayName(user.getDisplayName());
        item.setStatus(ListingStatus.PENDING_APPROVAL);
        item.setCreatedAt(LocalDateTime.now());

        if (images != null && !images.isEmpty()) {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    urls.add(fileStorageService.saveImageOptimized(file));
                }
            }
            item.setImageUrls(urls);
        }

        return itemRepository.save(item);
    }

    public ProductServiceItem updateListing(String username, String itemId, ProductServiceItem updated) {
        ProductServiceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (!item.getVendorUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to edit this listing");
        }

        item.setTitle(updated.getTitle());
        item.setBusinessName(updated.getBusinessName());
        item.setCategory(updated.getCategory());
        item.setPrice(updated.getPrice());
        item.setPriceUnit(updated.getPriceUnit());
        item.setLocationCity(updated.getLocationCity());
        item.setContactNumber(updated.getContactNumber());
        item.setDescription(updated.getDescription());
        item.setStatus(ListingStatus.PENDING_APPROVAL);

        return itemRepository.save(item);
    }

    public void deleteListing(String username, String itemId) {
        ProductServiceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        if (!item.getVendorUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this listing");
        }

        itemRepository.delete(item);
    }

    public List<ProductServiceItem> getApprovedItems(ItemCategory category, String city) {
        List<ProductServiceItem> items = itemRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.APPROVED);
        return items.stream()
                .filter(i -> category == null || i.getCategory() == category)
                .filter(i -> city == null || city.isBlank() || (i.getLocationCity() != null && i.getLocationCity().equalsIgnoreCase(city.trim())))
                .toList();
    }

    public List<ProductServiceItem> getVendorListings(String vendorUsername) {
        return itemRepository.findByVendorUsernameOrderByCreatedAtDesc(vendorUsername.toLowerCase().trim());
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

    public void sendInquiry(String inquirerUsername, String itemId, String inquiryMessage) {
        ProductServiceItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        var conversation = directMessageService.getOrCreateDirectConversation(inquirerUsername, item.getVendorUsername());

        String initialMessage = String.format("👋 Inquiry regarding '%s' (Category: %s, Price: ₹%s %s): %s",
                item.getTitle(),
                item.getCategory(),
                item.getPrice(),
                item.getPriceUnit() != null ? item.getPriceUnit() : "",
                inquiryMessage);

        directMessageService.sendMessage(inquirerUsername, conversation.getId(), initialMessage, null);
    }
}
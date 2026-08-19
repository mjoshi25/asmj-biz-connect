package com.joshi.twitterclone.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.joshi.twitterclone.model.Advertisement;
import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.repository.AdvertisementRepository;
import com.joshi.twitterclone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdMarketplaceService {

    private final AdvertisementRepository adRepository;
    private final UserRepository userRepository;
    private final DirectMessageService directMessageService;
    private final FileStorageService fileStorageService;

    // Advertiser creates a campaign
    public Advertisement submitAdvertisement(String advertiserUsername, String title, String description,
                                            String targetUrl, MultipartFile mediaFile) {
        User advertiser = userRepository.findByUsername(advertiserUsername.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advertiser not found"));

        Advertisement ad = new Advertisement();
        ad.setAdvertiserId(advertiser.getId());
        ad.setAdvertiserUsername(advertiser.getUsername());
        ad.setAdvertiserDisplayName(advertiser.getDisplayName());
        ad.setTitle(title.trim());
        ad.setDescription(description != null ? description.trim() : "");
        ad.setTargetUrl(targetUrl.trim());

        if (mediaFile != null && !mediaFile.isEmpty()) {
            String mediaPath = fileStorageService.saveFile(mediaFile);
            ad.setMediaUrl(mediaPath);
            ad.setMediaType(mediaFile.getContentType() != null && mediaFile.getContentType().startsWith("video/") ? "VIDEO" : "IMAGE");
        }

        ad.setStatus("PENDING_APPROVAL");
        return adRepository.save(ad);
    }

    // Admin reviews and approves/rejects campaign
    public Advertisement reviewAdvertisement(String adId, String adminUsername, boolean approve, String feedback) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found"));

        ad.setStatus(approve ? "APPROVED" : "REJECTED");
        ad.setAdminFeedback(feedback);
        ad.setReviewedByAdminUsername(adminUsername);
        ad.setReviewedAt(LocalDateTime.now());
        if (approve && ad.getStartsAt() == null) {
            ad.setStartsAt(LocalDateTime.now());
            ad.setExpiresAt(LocalDateTime.now().plusDays(30));
        }

        Advertisement savedAd = adRepository.save(ad);

        // System notification dispatch: Admin -> Advertiser
        String systemMsg = approve
                ? "🎉 Your ad '" + ad.getTitle() + "' has been approved by admin @" + adminUsername + " and is now live on the marketplace."
                : "⚠️ Your ad '" + ad.getTitle() + "' was rejected. Reason: " + (feedback != null ? feedback : "Creative does not meet guidelines.");

        Conversation adminChat = directMessageService.getOrCreateDirectConversation(adminUsername, ad.getAdvertiserUsername());
        directMessageService.sendMessage(adminUsername, adminChat.getId(), systemMsg, null);

        return savedAd;
    }

    // Connects a user directly with an advertiser referencing the specific ad
    public Conversation startUserToAdvertiserChat(String currentUsername, String adId) {
        Advertisement ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found"));

        Conversation convo = directMessageService.getOrCreateDirectConversation(currentUsername, ad.getAdvertiserUsername());
        
        // Track ad-generated conversation conversion
        ad.setMessageInquiriesCount(ad.getMessageInquiriesCount() + 1);
        adRepository.save(ad);

        return convo;
    }

    public List<Advertisement> getApprovedPublicAds() {
        return adRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
    }

    public List<Advertisement> getPendingApprovalAds() {
        return adRepository.findByStatusOrderByCreatedAtAsc("PENDING_APPROVAL");
    }

    public List<Advertisement> getAdvertiserCampaigns(String advertiserUsername) {
        return adRepository.findByAdvertiserUsernameOrderByCreatedAtDesc(advertiserUsername.toLowerCase());
    }
}
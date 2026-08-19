package com.joshi.twitterclone.repository;

import com.joshi.twitterclone.model.Advertisement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends MongoRepository<Advertisement, String> {

    // Fetch approved ads for the public marketplace
    List<Advertisement> findByStatusOrderByCreatedAtDesc(String status);

    // Fetch pending ads for the admin moderation queue
    List<Advertisement> findByStatusOrderByCreatedAtAsc(String status);

    // Fetch campaigns created by a specific advertiser
    List<Advertisement> findByAdvertiserUsernameOrderByCreatedAtDesc(String advertiserUsername);

    // Count ads by status (useful for admin dashboards)
    long countByStatus(String status);
}
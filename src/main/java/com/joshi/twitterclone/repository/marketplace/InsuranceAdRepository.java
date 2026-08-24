package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.InsuranceAd;
import com.joshi.twitterclone.model.marketplace.ListingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceAdRepository extends MongoRepository<InsuranceAd, String> {
    List<InsuranceAd> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<InsuranceAd> findByInsurerUsernameOrderByCreatedAtDesc(String insurerUsername);
}
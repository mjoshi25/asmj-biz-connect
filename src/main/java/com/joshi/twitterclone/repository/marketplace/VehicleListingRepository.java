package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.marketplace.VehicleListing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleListingRepository extends MongoRepository<VehicleListing, String> {
    List<VehicleListing> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<VehicleListing> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
    List<VehicleListing> findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus status, String city);
}
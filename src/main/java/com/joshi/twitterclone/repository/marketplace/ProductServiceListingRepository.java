package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.marketplace.ProductServiceListing;
import com.joshi.twitterclone.model.marketplace.ProductServiceType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductServiceListingRepository extends MongoRepository<ProductServiceListing, String> {
    List<ProductServiceListing> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<ProductServiceListing> findByStatusAndOfferingTypeOrderByCreatedAtDesc(ListingStatus status, ProductServiceType offeringType);
    List<ProductServiceListing> findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus status, String city);
    List<ProductServiceListing> findByVendorUsernameOrderByCreatedAtDesc(String vendorUsername);
}
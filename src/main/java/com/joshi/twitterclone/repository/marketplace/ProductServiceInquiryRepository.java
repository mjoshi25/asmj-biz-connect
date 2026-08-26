package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.ProductServiceInquiry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductServiceInquiryRepository extends MongoRepository<ProductServiceInquiry, String> {
    List<ProductServiceInquiry> findByVendorUsernameOrderByCreatedAtDesc(String vendorUsername);
    List<ProductServiceInquiry> findByBuyerUsernameOrderByCreatedAtDesc(String buyerUsername);
    List<ProductServiceInquiry> findByListingIdOrderByCreatedAtDesc(String listingId);
}
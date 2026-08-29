package com.joshi.twitterclone.repository.products;

import com.joshi.twitterclone.model.products.ProductListing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductListingRepository extends MongoRepository<ProductListing, String> {
}
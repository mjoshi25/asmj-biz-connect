package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.marketplace.MarketplaceProduct;
import com.joshi.twitterclone.model.marketplace.ProductCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceProductRepository extends MongoRepository<MarketplaceProduct, String> {
    List<MarketplaceProduct> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<MarketplaceProduct> findByStatusAndCategoryOrderByCreatedAtDesc(ListingStatus status, ProductCategory category);
    List<MarketplaceProduct> findBySellerUsernameOrderByCreatedAtDesc(String sellerUsername);
}
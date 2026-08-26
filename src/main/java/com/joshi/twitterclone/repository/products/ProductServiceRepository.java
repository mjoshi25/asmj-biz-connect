package com.joshi.twitterclone.repository.products;

import com.joshi.twitterclone.model.marketplace.ListingStatus;
import com.joshi.twitterclone.model.products.ItemCategory;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductServiceRepository extends MongoRepository<ProductServiceItem, String> {
    List<ProductServiceItem> findByStatusOrderByCreatedAtDesc(ListingStatus status);
    List<ProductServiceItem> findByStatusAndCategoryOrderByCreatedAtDesc(ListingStatus status, ItemCategory category);
    List<ProductServiceItem> findByVendorUsernameOrderByCreatedAtDesc(String vendorUsername);
    List<ProductServiceItem> findByStatusAndLocationCityIgnoreCaseOrderByCreatedAtDesc(ListingStatus status, String city);
}
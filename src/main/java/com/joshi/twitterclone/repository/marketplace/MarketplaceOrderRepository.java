package com.joshi.twitterclone.repository.marketplace;

import com.joshi.twitterclone.model.marketplace.MarketplaceOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceOrderRepository extends MongoRepository<MarketplaceOrder, String> {
    List<MarketplaceOrder> findByBuyerUsernameOrderByPlacedAtDesc(String buyerUsername);
}
package com.joshi.twitterclone.model.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String title;
    private String category;
    private String sellerUsername;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal itemTotal;
}
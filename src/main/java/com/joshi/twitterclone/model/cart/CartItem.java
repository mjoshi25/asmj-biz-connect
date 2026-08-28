package com.joshi.twitterclone.model.cart;

import com.joshi.twitterclone.model.products.ItemCategory;
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
    private String itemId;
    private String title;
    private String vendorUsername;
    private String businessName;
    private ItemCategory category;
    private BigDecimal unitPrice;
    private int quantity;
    private String imageUrl;
    private String priceUnit;

    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
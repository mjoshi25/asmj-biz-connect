package com.joshi.twitterclone.model.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "marketplace_orders")
public class MarketplaceOrder {

    @Id
    private String id;

    @Indexed
    private String buyerUsername;
    private String buyerName;
    private String contactPhone;
    private String shippingAddress;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private BigDecimal totalAmount;
    private String paymentMethod;

    @Builder.Default
    private String orderStatus = "CONFIRMED";

    @Builder.Default
    private LocalDateTime placedAt = LocalDateTime.now();
}
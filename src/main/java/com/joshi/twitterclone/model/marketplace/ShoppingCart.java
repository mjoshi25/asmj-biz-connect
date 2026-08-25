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
@Document(collection = "shopping_carts")
public class ShoppingCart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Builder.Default
    private int totalItemCount = 0;

    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
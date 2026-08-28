package com.joshi.twitterclone.model.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String orderNumber;

    @Indexed
    private String buyerUsername;
    private String buyerFullName;
    private String buyerPhone;

    // Delivery Address
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    @Builder.Default
    private String orderStatus = "CONFIRMED"; // CONFIRMED, PROCESSING, SHIPPED, DELIVERED

    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    public String getFormattedDate() {
        if (orderDate == null) return "";
        return orderDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a"));
    }
}
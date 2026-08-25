package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.Conversation;
import com.joshi.twitterclone.model.marketplace.*;
import com.joshi.twitterclone.repository.marketplace.MarketplaceOrderRepository;
import com.joshi.twitterclone.repository.marketplace.MarketplaceProductRepository;
import com.joshi.twitterclone.repository.marketplace.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final MarketplaceProductRepository productRepository;
    private final MarketplaceOrderRepository orderRepository;
    private final DirectMessageService directMessageService;

    public ShoppingCart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> cartRepository.save(ShoppingCart.builder()
                        .username(username)
                        .items(new ArrayList<>())
                        .grandTotal(BigDecimal.ZERO)
                        .totalItemCount(0)
                        .lastUpdated(LocalDateTime.now())
                        .build()));
    }

    public ShoppingCart addToCart(String username, String productId, int quantity) {
        MarketplaceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStockQuantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is currently out of stock");
        }

        ShoppingCart cart = getOrCreateCart(username);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        int qtyToAdd = quantity > 0 ? quantity : 1;

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + qtyToAdd);
            item.setItemTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            String imgUrl = (product.getImageUrls() != null && !product.getImageUrls().isEmpty())
                    ? product.getImageUrls().get(0) : "";

            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .category(product.getCategory().name())
                    .sellerUsername(product.getSellerUsername())
                    .imageUrl(imgUrl)
                    .unitPrice(product.getUnitPrice())
                    .quantity(qtyToAdd)
                    .itemTotal(product.getUnitPrice().multiply(BigDecimal.valueOf(qtyToAdd)))
                    .build();
            cart.getItems().add(newItem);
        }

        recalculateCart(cart);
        return cartRepository.save(cart);
    }

    public ShoppingCart updateItemQuantity(String username, String productId, int delta) {
        ShoppingCart cart = getOrCreateCart(username);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + delta;
            if (newQty <= 0) {
                cart.getItems().remove(item);
            } else {
                item.setQuantity(newQty);
                item.setItemTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            }
            recalculateCart(cart);
            return cartRepository.save(cart);
        }
        return cart;
    }

    public ShoppingCart removeItem(String username, String productId) {
        ShoppingCart cart = getOrCreateCart(username);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        recalculateCart(cart);
        return cartRepository.save(cart);
    }

    public void clearCart(String username) {
        ShoppingCart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cart.setGrandTotal(BigDecimal.ZERO);
        cart.setTotalItemCount(0);
        cart.setLastUpdated(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public MarketplaceOrder checkoutOrder(String username, String buyerName, String phone, String address, String paymentMethod) {
        ShoppingCart cart = getOrCreateCart(username);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        MarketplaceOrder order = MarketplaceOrder.builder()
                .buyerUsername(username)
                .buyerName(buyerName)
                .contactPhone(phone)
                .shippingAddress(address)
                .items(new ArrayList<>(cart.getItems()))
                .totalAmount(cart.getGrandTotal())
                .paymentMethod(paymentMethod != null ? paymentMethod : "DIRECT_INVOICE")
                .orderStatus("CONFIRMED")
                .placedAt(LocalDateTime.now())
                .build();

        MarketplaceOrder savedOrder = orderRepository.save(order);

        // Open chat threads with sellers automatically
        for (CartItem item : cart.getItems()) {
            if (item.getSellerUsername() != null && !item.getSellerUsername().equalsIgnoreCase(username)) {
                try {
                    Conversation convo = directMessageService.getOrCreateDirectConversation(username, item.getSellerUsername());
                    String orderMsg = String.format("🛍️ Order #%s Placed!\nItem: %s (Qty: %d, ₹%s).\nShipping: %s\nPhone: %s",
                            savedOrder.getId().substring(Math.max(0, savedOrder.getId().length() - 6)),
                            item.getTitle(), item.getQuantity(), item.getItemTotal(), address, phone);
                    directMessageService.sendMessage(username, convo.getId(), orderMsg, null);
                } catch (Exception e) {
                    log.error("Failed to notify seller @{}: {}", item.getSellerUsername(), e.getMessage());
                }
            }
        }

        clearCart(username);
        return savedOrder;
    }

    private void recalculateCart(ShoppingCart cart) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CartItem item : cart.getItems()) {
            total = total.add(item.getItemTotal());
            count += item.getQuantity();
        }
        cart.setGrandTotal(total);
        cart.setTotalItemCount(count);
        cart.setLastUpdated(LocalDateTime.now());
    }
}
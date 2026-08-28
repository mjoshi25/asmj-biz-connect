package com.joshi.twitterclone.service;

import com.joshi.twitterclone.model.User;
import com.joshi.twitterclone.model.cart.Cart;
import com.joshi.twitterclone.model.cart.CartItem;
import com.joshi.twitterclone.model.cart.Order;
import com.joshi.twitterclone.model.products.ProductServiceItem;
import com.joshi.twitterclone.repository.UserRepository;
import com.joshi.twitterclone.repository.cart.CartRepository;
import com.joshi.twitterclone.repository.cart.OrderRepository;
import com.joshi.twitterclone.repository.products.ProductServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductServiceRepository productRepository;
    private final UserRepository userRepository;
    private final DirectMessageService directMessageService;

    public Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username.toLowerCase().trim())
                .orElseGet(() -> {
                    Cart c = Cart.builder()
                            .username(username.toLowerCase().trim())
                            .items(new ArrayList<>())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(c);
                });
    }

    public Cart addToCart(String username, String itemId, int quantity) {
        ProductServiceItem product = productRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product/Service not found"));

        Cart cart = getOrCreateCart(username);
        
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            String imgUrl = (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) 
                    ? product.getImageUrls().get(0) 
                    : "";

            CartItem newItem = CartItem.builder()
                    .itemId(product.getId())
                    .title(product.getTitle())
                    .vendorUsername(product.getVendorUsername())
                    .businessName(product.getBusinessName())
                    .category(product.getCategory())
                    .unitPrice(product.getPrice())
                    .priceUnit(product.getPriceUnit())
                    .quantity(Math.max(1, quantity))
                    .imageUrl(imgUrl)
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String username, String itemId, int quantity) {
        Cart cart = getOrCreateCart(username);

        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getItemId().equals(itemId));
        } else {
            cart.getItems().stream()
                    .filter(i -> i.getItemId().equals(itemId))
                    .findFirst()
                    .ifPresent(i -> i.setQuantity(quantity));
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    public Cart removeFromCart(String username, String itemId) {
        return updateItemQuantity(username, itemId, 0);
    }

    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public Order checkout(String username, String fullName, String phone, String streetAddress, String city, String state, String postalCode) {
        Cart cart = getOrCreateCart(username);
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        // Auto-save address to user profile if user has no address configured
        User user = userRepository.findByUsername(username.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getLocation() == null || user.getLocation().isBlank()) {
            user.setLocation(city + ", " + state);
            userRepository.save(user);
        }

        String orderNum = "ASMJ-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNum)
                .buyerUsername(user.getUsername())
                .buyerFullName(fullName)
                .buyerPhone(phone)
                .streetAddress(streetAddress)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .items(new ArrayList<>(cart.getItems()))
                .totalAmount(cart.getTotalAmount())
                .orderStatus("CONFIRMED")
                .orderDate(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Notify vendors automatically via chat
        for (CartItem item : cart.getItems()) {
            try {
                var convo = directMessageService.getOrCreateDirectConversation(username, item.getVendorUsername());
                String msg = String.format("🎉 New Order [%s]: Placed order for '%s' (Qty: %d, ₹%s). Shipping to: %s, %s.",
                        orderNum, item.getTitle(), item.getQuantity(), item.getSubtotal(), streetAddress, city);
                directMessageService.sendMessage(username, convo.getId(), msg, null);
            } catch (Exception ignored) {
            }
        }

        clearCart(username);
        return savedOrder;
    }

    public List<Order> getUserOrders(String username) {
        return orderRepository.findByBuyerUsernameOrderByOrderDateDesc(username.toLowerCase().trim());
    }
}
package com.example.kfcrena;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<Product> cartItems;
    private String appliedCouponCode = null;
    private double discountPercentage = 0.0;
    private double fixedDiscountAmount = 0.0;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addProduct(Product product) {
        cartItems.add(product);
    }

    public void removeProduct(Product product) {
        cartItems.remove(product);
        if (cartItems.isEmpty()) {
            removeCoupon();
        }
    }

    public List<Product> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Product item : cartItems) {
            total += item.getPrice();
        }
        return total;
    }

    public boolean applyCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        String cleanCode = code.trim().toUpperCase();

        if (cleanCode.equals("FRETEKFC")) {
            appliedCouponCode = "FRETEKFC";
            fixedDiscountAmount = 10.00;
            discountPercentage = 0.0;
            return true;
        } else if (cleanCode.equals("BALDE50")) {
            appliedCouponCode = "BALDE50";
            discountPercentage = 0.50;
            fixedDiscountAmount = 0.0;
            return true;
        } else if (cleanCode.equals("KFC10")) {
            appliedCouponCode = "KFC10";
            discountPercentage = 0.10;
            fixedDiscountAmount = 0.0;
            return true;
        }

        return false;
    }

    public void removeCoupon() {
        appliedCouponCode = null;
        discountPercentage = 0.0;
        fixedDiscountAmount = 0.0;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public double getDiscountAmount() {
        if (cartItems.isEmpty()) {
            return 0.0;
        }

        double subtotal = getTotalPrice();
        double discount = 0.0;

        if (fixedDiscountAmount > 0) {
            discount = fixedDiscountAmount;
        } else if (discountPercentage > 0) {
            discount = subtotal * discountPercentage;
        }

        return Math.min(discount, subtotal);
    }

    public double getFinalPrice() {
        return Math.max(0, getTotalPrice() - getDiscountAmount());
    }

    public void clearCart() {
        cartItems.clear();
        removeCoupon();
    }
}
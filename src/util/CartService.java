package util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CartService {
    // Sepete eklenen oyunların ID'lerini tutan liste
    private static final List<Integer> cartItemIds = new ArrayList<>();
    private static final Set<Integer> cartItems = new HashSet<>();

    public static void addToCart(int gameId) {
        cartItems.add(gameId);
    }

    public static void removeFromCart(int gameId) {
        cartItems.remove(gameId);
    }

    public static boolean isInCart(int gameId) {
        return cartItems.contains(gameId);
    }

    public static Set<Integer> getCartItems() {
        return cartItems;
    }

    public static void addGame(int gameId) {
        if (!cartItemIds.contains(gameId)) { // Aynı oyunu 2 kere eklemeyi engeller
            cartItemIds.add(gameId);
        }
    }

    public static void removeGame(int gameId) {
        cartItemIds.remove(Integer.valueOf(gameId));
    }

    public static List<Integer> getCart() {
        return cartItemIds;
    }

    public static void clearCart() {
        cartItemIds.clear();
    }
}
package util;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    // TEK bir liste — her şey buradan
    private static final List<Integer> cartItems = new ArrayList<>();

    public static void addToCart(int gameId) {
        if (!cartItems.contains(gameId)) {
            cartItems.add(gameId);
        }
    }

    public static void removeGame(int gameId) {
        cartItems.remove(Integer.valueOf(gameId));
    }

    public static boolean isInCart(int gameId) {
        return cartItems.contains(gameId);
    }

    public static List<Integer> getCart() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
    }
}
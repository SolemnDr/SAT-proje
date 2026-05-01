package util;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    // Sepete eklenen oyunların ID'lerini tutan liste
    private static final List<Integer> cartItemIds = new ArrayList<>();

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
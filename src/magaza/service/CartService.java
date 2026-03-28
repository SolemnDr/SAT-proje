package magaza.service;

import magaza.model.Game;
import kutuphane.dao.LibraryDAO;

import java.util.ArrayList;
import java.util.List;

public class CartService {

    // 1. GEÇİCİ DEPO (Sadece uygulama açıkken yaşar)
    private final List<Game> cartGames = new ArrayList<>();
    private final LibraryDAO libraryDAO = new LibraryDAO();

    // 2. SEPETE EKLEME VE ÇIKARMA
    public void addToCart(Game game) {
        // Aynı oyunu 2 kere eklemesin diye ufak bir kontrol
        boolean alreadyInCart = cartGames.stream().anyMatch(g -> g.getId() == game.getId());
        if (!alreadyInCart) {
            cartGames.add(game);
        }
    }

    public void removeFromCart(int gameId) {
        cartGames.removeIf(g -> g.getId() == gameId);
    }

    public List<Game> getCartItems() {
        return cartGames;
    }

    // 3. TOPLAM FİYAT HESAPLAMA
    // Tuğalp arayüzde "Sepet Toplamı: X TL" yazmak için bu metodu çağıracak
    public double getTotalPrice() {
        double total = 0;
        for (Game g : cartGames) {
            total += g.getPrice();
            // Eğer indirim sistemi aktifse burayı g.getDiscountedPrice() gibi güncelleyebilirsiniz
        }
        return total;
    }

    // 4. ÖDEME VE TESLİMAT (Tuğalp'in Kart Ekranına Bağlanacak Metot)
    public boolean checkout(int userId, String cardNumber) {
        // Yalandan bir kart güvenlik kontrolü (Örn: 16 haneli mi girilmiş?)
        if (cardNumber == null || cardNumber.length() < 16) {
            return false; // Kart geçersiz hatası döner
        }

        // Ödeme başarılıysa sepetteki her oyunu adamın kütüphanesine ekle
        for (Game game : cartGames) {
            libraryDAO.addGameToLibrary(userId, game.getId());
        }

        // Oyunlar teslim edildi, sepeti boşalt!
        cartGames.clear();
        return true;
    }
}
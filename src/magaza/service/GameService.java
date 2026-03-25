package magaza.service;

import magaza.dao.GameDAO;
import magaza.model.Game;
import util.IgdbService;
import magaza.dao.PurchaseDAO;
import java.util.ArrayList;
import java.util.List;
import magaza.dao.CartDAO;

public class GameService {

    private final GameDAO gameDAO = new GameDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final CartDAO cartDAO = new CartDAO();

    // Sepete ekle
    public void addToCart(int userId, int gameId) throws Exception {
        if (purchaseDAO.hasPurchased(userId, gameId)) {
            throw new Exception("Bu oyun zaten satın alınmış!");
        }
        if (cartDAO.isInCart(userId, gameId)) {
            throw new Exception("Bu oyun zaten sepette!");
        }
        cartDAO.add(userId, gameId);
    }

    // Sepetten çıkar
    public void removeFromCart(int userId, int gameId) throws Exception {
        cartDAO.remove(userId, gameId);
    }

    // Sepetteki toplam fiyat
    public double getCartTotal(int userId) throws Exception {
        List<Game> games = getCart(userId);
        double total = 0;
        for (Game g : games) {
            total += getDiscountedPrice(g);
        }
        return total;
    }

    // Sepeti satın al
    public void purchaseCart(int userId) throws Exception {
        List<Integer> ids = cartDAO.getCartGameIds(userId);
        if (ids.isEmpty()) {
            throw new Exception("Sepet boş!");
        }
        for (int gameId : ids) {
            purchaseGame(userId, gameId);
        }
        cartDAO.clear(userId);
    }

    // İndirimli fiyatı hesapla
    public double getDiscountedPrice(Game game) {
        if (game.getDiscountPercent() > 0) {
            return game.getPrice() * (1 - game.getDiscountPercent() / 100);
        }
        return game.getPrice();
    }

    // Çok satanları getir
    public List<Game> getBestSellers() throws Exception {
        return gameDAO.findBestSellers();
    }

    // İndirimli oyunları getir
    public List<Game> getDiscountedGames() throws Exception {
        return gameDAO.findDiscounted();
    }

    // İndirim uygula (publisher yapacak)
    public void applyDiscount(int gameId, double discountPercent) throws Exception {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new Exception("İndirim 0-100 arasında olmalı!");
        }
        gameDAO.applyDiscount(gameId, discountPercent);
    }


    // Tüm oyunları getir
    public List<Game> getAllGames() throws Exception {
        return gameDAO.findAll();
    }

    // İsme göre ara
    public List<Game> searchByName(String name) throws Exception {
        return gameDAO.findByName(name);
    }

    // Kategoriye göre ara
    public List<Game> searchByGenre(String genre) throws Exception {
        return gameDAO.findByGenre(genre);
    }

    // IGDB'den oyun çek ve kaydet
    public List<Game> fetchFromIgdb(String gameName) throws Exception {
        return IgdbService.searchAndSave(gameName);
    }
    // Oyun sil
    public void deleteGame(int gameId) throws Exception {
        gameDAO.delete(gameId);
    }

    // Oyun güncelle
    public void updateGame(Game game) throws Exception {
        gameDAO.update(game);
    }

    // Publisher'ın oyunlarını getir
    public List<Game> getPublisherGames(int publisherId) throws Exception {
        return gameDAO.findByPublisher(publisherId);
    }

    // Fiyata göre sırala
    public List<Game> getGamesSortedByPrice() throws Exception {
        return gameDAO.findAllSortedByPrice();
    }

    // Puana göre sırala
    public List<Game> getGamesSortedByRating() throws Exception {
        return gameDAO.findAllSortedByRating();
    }

    // Fiyat belirle
    public void setPrice(int gameId, double price) throws Exception {
        Game game = gameDAO.findAll()
                .stream()
                .filter(g -> g.getId() == gameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Oyun bulunamadı"));
        game.setPrice(price);
        gameDAO.update(game);
    }
    // Satın alınan oyunların kategorilerine göre öneri
    public List<Game> getRecommendations(int userId) throws Exception {
        // Kullanıcının sahip olduğu oyunları al
        List<Game> ownedGames = getPurchasedGames(userId);

        if (ownedGames.isEmpty()) {
            // Hiç oyunu yoksa çok satanları öner
            return getBestSellers();
        }

        // Sahip olduğu oyunların kategorilerini topla
        List<String> genres = new ArrayList<>();
        for (Game g : ownedGames) {
            if (g.getGenres() != null) {
                for (String genre : g.getGenres().split(", ")) {
                    if (!genres.contains(genre)) {
                        genres.add(genre);
                    }
                }
            }
        }

        // Sahip olmadığı, aynı kategorideki oyunları bul
        List<Game> allGames = gameDAO.findAll();
        List<Integer> ownedIds = ownedGames.stream()
                .map(Game::getId)
                .toList();

        List<Game> recommendations = new ArrayList<>();
        for (Game g : allGames) {
            if (ownedIds.contains(g.getId())) continue; // zaten sahip
            if (g.getGenres() == null) continue;
            for (String genre : genres) {
                if (g.getGenres().contains(genre)) {
                    recommendations.add(g);
                    break;
                }
            }
        }

        return recommendations;
    }
    // Sepeti getir (Optimize Edildi - O(N) karmaşıklığı)
    public List<Game> getCart(int userId) throws Exception {
        List<Integer> ids = cartDAO.getCartGameIds(userId);
        List<Game> games = new ArrayList<>();
        for (int id : ids) {
            Game g = gameDAO.findById(id); // Tüm tabloyu çekmek yerine sadece 1 oyunu çeker
            if (g != null) games.add(g);
        }
        return games;
    }

    // Kullanıcının satın aldığı oyunları getir (Optimize Edildi)
    public List<Game> getPurchasedGames(int userId) throws Exception {
        List<Integer> ids = purchaseDAO.getPurchasedGameIds(userId);
        List<Game> games = new ArrayList<>();
        for (int id : ids) {
            Game g = gameDAO.findById(id);
            if (g != null) games.add(g);
        }
        return games;
    }

    // Satın alma güncelle (Satış sayısını artır - Optimize Edildi)
    public void purchaseGame(int userId, int gameId) throws Exception {
        if (purchaseDAO.hasPurchased(userId, gameId)) {
            throw new Exception("Bu oyun zaten satın alınmış!");
        }

        Game game = gameDAO.findById(gameId); // Tüm tabloyu belleğe almaktan kurtulduk
        if (game == null) {
            throw new Exception("Oyun bulunamadı");
        }

        double finalPrice = getDiscountedPrice(game);
        purchaseDAO.save(userId, gameId, finalPrice);
        gameDAO.incrementSalesCount(gameId);
    }
}
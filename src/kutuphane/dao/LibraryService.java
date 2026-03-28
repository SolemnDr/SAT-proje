package kutuphane.service;

import kutuphane.dao.LibraryDAO;
import magaza.model.Game;

import java.util.List;

public class LibraryService {
    private final LibraryDAO libraryDAO = new LibraryDAO();

    public LibraryService() {
        // Servis ilk çalıştığında tabloların olduğundan emin ol
        libraryDAO.createTablesIfNotExists();
    }

    // Kullanıcının kütüphanesini (sadece gizli olmayanları) getir
    public List<Game> getUserGames(int userId) throws Exception {
        return libraryDAO.getVisibleLibrary(userId);
    }

    // Kütüphanedeki bir oyunu gizle
    public void hideGame(int userId, int gameId) throws Exception {
        libraryDAO.setGameHiddenStatus(userId, gameId, true);
    }

    // Yeni özel liste oluştur
    public void createCustomList(int userId, String listName) throws Exception {
        if (listName == null || listName.trim().isEmpty()) {
            throw new Exception("Liste adı boş olamaz!");
        }
        libraryDAO.createCollection(userId, listName);
    }

    // Oyunu özel listeye ekle
    public void addGameToList(int collectionId, int gameId) throws Exception {
        libraryDAO.addGameToCollection(collectionId, gameId);
    }
}
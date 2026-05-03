import gui.MainApp;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        // --- 1. İLK KURULUM: VERİTABANINI OYUNLARLA DOLDURMA ---
        // DİKKAT: Projeyi sıfırdan ilk kez ayağa kaldırıyorsanız aşağıdaki bloktaki
        // yorum satırlarını (/* ve */) kaldırıp 1 defaya mahsus çalıştırın.
        // API'den veriler çekildikten sonra tekrar yoruma alabilirsiniz.

        /*
        try {
            System.out.println("Kurulum: IGDB API üzerinden oyun verileri çekiliyor. Lütfen bekleyin...");
            util.IgdbService.seedStoreDatabase();
            System.out.println("Kurulum Başarılı: Oyunlar veritabanına işlendi!");
        } catch (Exception e) {
            System.out.println("Hata: Oyunlar çekilirken bir sorun oluştu!");
            e.printStackTrace();
        }*/


        // --- 2. VERİTABANI MİMARİSİ VE TABLO KONTROLLERİ ---
        System.out.println("Sistem: Veritabanı tabloları ve mimari güncellemeler kontrol ediliyor...");

        // Rol (Oyuncu/Yayıncı) sütunu kontrolü
        kullanici.dao.UserDAO userDAO = new kullanici.dao.UserDAO();
        userDAO.upgradeTableForRoles();

        // Yorumlar (Reviews) tablosu kontrolü
        magaza.dao.ReviewDAO reviewDAO = new magaza.dao.ReviewDAO();
        reviewDAO.createTableIfNotExists();

        // İndirim motoru (Discount) sütunu kontrolü
        magaza.dao.GameDAO gameDAO = new magaza.dao.GameDAO();
        gameDAO.upgradeTableForDiscounts();

        // --- 3. JAVAFX ARAYÜZÜNÜ BAŞLATMA ---
        System.out.println("GameStore: Arayüz başlatılıyor...");
        Application.launch(MainApp.class, args);
    }
}
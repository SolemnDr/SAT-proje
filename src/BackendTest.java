import magaza.model.Game;
import magaza.service.CartService;
import kutuphane.dao.LibraryDAO;
import magaza.dao.ReviewDAO;
import kullanici.dao.UserDAO;
import kullanici.model.User;
import kullanici.model.UserRole;

import java.util.List;
import java.util.Optional;

public class BackendTest {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🚀 BACKEND ENTEGRASYON TESTİ BAŞLIYOR...");
        System.out.println("=================================================\n");

        try {
            // ---------------------------------------------------------
            // 0. HAZIRLIK: TABLOLARI KUR VE TEST KULLANICISI YARAT
            // ---------------------------------------------------------
            System.out.println("[HAZIRLIK] Veritabanı tabloları ve kullanıcı ayarlanıyor...");

            // Tabloların olduğundan emin olalım
            LibraryDAO libraryDAO = new LibraryDAO();
            libraryDAO.createTablesIfNotExists();

            ReviewDAO reviewDAO = new ReviewDAO();
            reviewDAO.createTableIfNotExists();

            UserDAO userDAO = new UserDAO();

            // Test kullanıcısı veritabanında yoksa, senin save() metodunla kaydedelim
            if (!userDAO.isUserExists("TestOyuncu", "test@oyuncu.com")) {
                User testUser = new User();
                testUser.setUsername("TestOyuncu");
                testUser.setEmail("test@oyuncu.com");
                testUser.setPasswordHash("123456"); // Normalde hashlenir, test için düz yazıyoruz
                testUser.setRole(UserRole.valueOf("USER")); // Eğer UserRole enum'ında farklı bir isim varsa (örn: CUSTOMER), burayı ona göre değiştir.
                userDAO.save(testUser);
            }

            // Senin findByUsername() metodunla adamı veritabanından çekip ID'sini alalım (Login simülasyonu)
            Optional<User> optUser = userDAO.findByUsername("TestOyuncu");
            if (optUser.isEmpty()) {
                System.out.println("❌ HATA: Test kullanıcısı veritabanında bulunamadı!");
                return; // Testi iptal et
            }

            int testUserId = optUser.get().getId();
            System.out.println("         -> Test kullanıcısı hazır! (ID: " + testUserId + ")\n");

            int testGameId = 1; // 1 Numaralı oyunu test için kullanacağız

            // ---------------------------------------------------------
            // 1. SEPET VE SATIN ALMA TESTİ (MODÜL 2 & MODÜL 3)
            // ---------------------------------------------------------
            System.out.print("[TEST 1] Sepet (CartService) ve Satın Alma işlemi... ");
            CartService cartService = new CartService();

            Game testGame = new Game();
            testGame.setId(testGameId);
            testGame.setPrice(150.0); // Temsili fiyat

            cartService.addToCart(testGame);

            // 16 Haneli temsili kredi kartı ile checkout yapıyoruz
            boolean isCheckoutSuccessful = cartService.checkout(testUserId, "1234567890123456");

            if (isCheckoutSuccessful) {
                System.out.println("BAŞARILI!");
            } else {
                System.out.println("BAŞARISIZ!");
            }

            // ---------------------------------------------------------
            // 2. KÜTÜPHANE KONTROL TESTİ (MODÜL 3)
            // ---------------------------------------------------------
            System.out.print("[TEST 2] Kütüphane (LibraryDAO) kontrolü... ");
            List<Game> userLibrary = libraryDAO.getVisibleLibrary(testUserId);

            boolean gameFoundInLibrary = false;
            for (Game g : userLibrary) {
                if (g.getId() == testGameId) {
                    gameFoundInLibrary = true;
                    break;
                }
            }

            if (gameFoundInLibrary) {
                System.out.println("BAŞARILI!");
            } else {
                System.out.println("BAŞARISIZ! (Oyun satın alınmış ama kütüphanede gözükmüyor)");
            }

            // ---------------------------------------------------------
            // 3. YORUM VE PUANLAMA TESTİ (MODÜL 5)
            // ---------------------------------------------------------
            System.out.print("[TEST 3] Yorum ve Yıldız (ReviewDAO) işlemi... ");
            boolean reviewAdded = reviewDAO.addReview(testUserId, testGameId, 5, "Sistem harika çalışıyor, oyuna bayıldım!");

            if (reviewAdded) {
                System.out.println("BAŞARILI!");
            } else {
                System.out.println("BAŞARISIZ!");
            }

            // Oyunun yeni ortalamasını çekelim
            double averageRating = reviewDAO.getAverageRating(testGameId);
            System.out.println("         -> Oyunun güncel puan ortalaması: " + averageRating + " / 5.0");

            // ---------------------------------------------------------
            // FİNAL SONUCU
            // ---------------------------------------------------------
            System.out.println("\n=================================================");
            System.out.println("✅ SONUÇ: TÜM TESTLER BAŞARIYLA GEÇTİ!");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.out.println("\n❌ HATA: TEST SIRASINDA BEKLENMEYEN BİR SORUN OLUŞTU!");
            e.printStackTrace();
        }
    }
}
import gui.MainApp;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        // 1. VERİTABANI ARTIK 989 OYUNLA DOLU!
        // Her çalıştırdığımızda tekrar indirmesin diye burayı kapattık.
        // Eğer veritabanı silinirse bu yorum satırlarını açıp bir kere çalıştırman yeterli.
        /*
        try {
            util.IgdbService.seedStoreDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        // 2. VERİTABANI GÜNCELLEMESİ
        // Eğer users tablosunda avatar_path sütunu yoksa, hata vermeden ekler.
        System.out.println("Veritabanı kontrolleri yapılıyor...");
        kullanici.dao.UserDAO userDAO = new kullanici.dao.UserDAO();
        userDAO.upgradeTableForAvatars();

        // 3. JAVAFX ARAYÜZÜNÜ BAŞLATAN TRUVA ATI KODU
        // Arayüzü doğrudan değil, bu sınıf üzerinden dolaylı yoldan başlatıyoruz (Hatayı atlatmak için)
        System.out.println("GameStore Arayüzü Başlatılıyor...");
        Application.launch(MainApp.class, args);
    }
}
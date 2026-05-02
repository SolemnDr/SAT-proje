import gui.MainApp;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        // 1. VERİTABANI ARTIK 989 OYUNLA DOLU!
        // Her çalıştırdığımızda tekrar indirmesin diye burayı kapattık.
        // Eğer veritabanı silinirse bu yorum satırlarını açıp bir kere çalıştırman yeterli.

        /*try {
            util.IgdbService.seedStoreDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        // 2. VERİTABANI GÜNCELLEMESİ VE KONTROLLERİ
        System.out.println("Veritabanı kontrolleri yapılıyor...");

        // Rol (Oyuncu/Yayıncı) sütunu kontrolü - Avatar yerine bunu ekledik
        kullanici.dao.UserDAO userDAO = new kullanici.dao.UserDAO();
        userDAO.upgradeTableForRoles();

        // Yorumlar (Reviews) tablosu kontrolü
        magaza.dao.ReviewDAO reviewDAO = new magaza.dao.ReviewDAO();
        reviewDAO.createTableIfNotExists();

        magaza.dao.GameDAO gameDAO = new magaza.dao.GameDAO();
        gameDAO.upgradeTableForDiscounts();

        // 3. JAVAFX ARAYÜZÜNÜ BAŞLATAN TRUVA ATI KODU
        // Arayüzü doğrudan değil, bu sınıf üzerinden dolaylı yoldan başlatıyoruz
        System.out.println("GameStore Arayüzü Başlatılıyor...");
        Application.launch(MainApp.class, args);
    }
}
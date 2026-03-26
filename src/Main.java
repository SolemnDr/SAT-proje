import gui.MainApp;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        // 1. VERİTABANI ARTIK 1000 OYUNLA DOLU!
        // Her çalıştırdığımızda tekrar indirmesin diye burayı kapattık.
        // Eğer veritabanı silinirse bu yorum satırlarını açıp bir kere çalıştırman yeterli.
        /*
        try {
            util.IgdbService.seedStoreDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        // 2. JAVAFX ARAYÜZÜNÜ BAŞLATAN TRUVA ATI KODU
        // Arayüzü doğrudan değil, bu sınıf üzerinden dolaylı yoldan başlatıyoruz (Hatayı atlatmak için)
        System.out.println("GameStore Arayüzü Başlatılıyor...");
        Application.launch(MainApp.class, args);
    }
}
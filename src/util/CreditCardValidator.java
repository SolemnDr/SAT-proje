package util;

public class CreditCardValidator {
    // Kart numarasını alır, doğrular ve markasını döndürür. Geçersizse hata fırlatır.
    public static String validateAndGetBrand(String cardNumber) throws Exception {
        // 1. Adım: Kullanıcı aralara boşluk veya tire koyduysa onları temizle
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");

        // 2. Adım: Kart sadece rakamlardan mı oluşuyor ve tam 16 hane mi? (Basit uzunluk kontrolü)
        if (!cleanNumber.matches("\\d{16}")) {
            throw new Exception("Ödeme Reddedildi: Geçersiz kart numarası! Kart 16 haneli rakamlardan oluşmalıdır.");
        }

        // 3. Adım: İlk hanelere göre (BIN numarası) kart markasını tespit etme
        if (cleanNumber.startsWith("4")) {
            return "Visa";
        }
        // MasterCard 51-55 veya 22-27 arasıyla başlar
        else if (cleanNumber.matches("^(5[1-5]|2[2-7]).*")) {
            return "MasterCard";
        }
        // Troy Kart (Türkiye) 9792 ile başlar (Hocaya şov kısmı)
        else if (cleanNumber.startsWith("9792")) {
            return "Troy";
        }
        // Discover Card 6 ile başlar
        else if (cleanNumber.startsWith("6")) {
            return "Discover";
        }
        else {
            return "Standart Kredi Kartı";
        }
    }
}

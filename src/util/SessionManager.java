package util;

public class SessionManager {

    // Şimdilik testlerin bozulmasın diye varsayılan olarak 1 veriyoruz.
    // Tuğalp'in sistemi gelince burası program açıldığında -1 (kimse girmemiş) olarak başlayacak.
    private static int currentUserId = 1;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    // Kullanıcı çıkış yaptığında (Logout) çağırılacak metot
    public static void logout() {
        currentUserId = -1; // Veya yönlendirme mantığınıza göre sıfırlayın
        CartService.clearCart(); // Çıkış yapınca sepeti de temizle
    }
}
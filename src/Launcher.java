public class Launcher {
    public static void main(String[] args) {

        // --- BİR KERELİK VERİTABANI KURULUM OPERASYONU BAŞLANGICI ---

        /*try {
            // 1. Adım: Mock (Sahte) oyunları temizle
            try (java.sql.Statement stmt = util.DBConnection.get().createStatement()) {
                stmt.execute("DELETE FROM games WHERE id IN (1, 2, 3)");
                System.out.println("Görev Başarılı: Mock oyunlar imha edildi!");
            }

            // 2. Adım: 1000 gerçek oyunu indir
            util.IgdbService.seedStoreDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        // --- BİR KERELİK VERİTABANI KURULUM OPERASYONU BİTİŞİ ---

        // Asıl motoru çalıştır
        Main.main(args);
    }
}
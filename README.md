# Dijital Oyun Dağıtım ve Kütüphane Yönetim Sistemi
**BLM2042 Sistem Analizi ve Tasarımı Dönem Projesi**

## 1. Proje Bilgileri
Bu proje, kullanıcıların dijital oyunları inceleyebileceği, satın alabileceği, kütüphanelerini yönetebileceği ve arkadaşlarıyla etkileşime geçebileceği bir platformun prototipidir.
* **Geliştirme Dili:** Java
* **Arayüz (GUI):** JavaFX (FXML)
* **Veritabanı:** SQLite
* **Mimari:** Katmanlı Mimari (Model - DAO - Controller)

---
## 2. Gerekli Kütüphaneler ve Ortam
Uygulamanın çalışabilmesi için lib klasöründe bulunan tüm .jar dosyalarının projenin Build Path'ine eklenmiş olması gerekmektedir. Kütüphanelerin işlevleri aşağıda kategorize edilmiştir:
1. Veritabanı (SQLite JDBC): sqlite-jdbc-3.45.1.0.jar dosyası, uygulamanın SQLite veritabanı ile iletişimini sağlar.
2. Arayüz (JavaFX 19): javafx-base, javafx-controls, javafx-fxml ve javafx-graphics kütüphaneleri masaüstü arayüzünün (GUI) oluşturulması ve FXML dosyalarının işlenmesi için gereklidir.
3. Ağ ve API Entegrasyonu: unirest-java, httpclient, httpcore ve httpasyncclient kütüphaneleri IGDB API üzerinden oyun verilerinin çekilmesi için kullanılmaktadır.
4. Veri İşleme (JSON): gson-2.10.jar, API'den gelen verilerin Java nesnelerine dönüştürülmesini sağlar.
5. Güvenlik (Encryption): jbcrypt-0.4.jar kütüphanesi, kullanıcı şifrelerinin veritabanında güvenli bir şekilde hashlenerek saklanması amacıyla kullanılmaktadır.
6. Loglama ve Yardımcı Araçlar: slf4j ve commons-logging kütüphaneleri sistem loglarının takibi; commons-codec ise veri şifreleme/çözme işlemleri için dahil edilmiştir.
---
## 3. Veri Tabanı Bağlantısı
Kılavuzda belirtilen veri tabanı bağlantı detayları şu şekildedir:
1. Bağlantı Türü: JDBC (Java Database Connectivity) üzerinden SQLite sürücüsü ile yerel bağlantı kurulmaktadır.
2. Bağlantı Yolu (URL): jdbc:sqlite:gamestore.db.
3. Dosya Konumu: Veritabanı dosyası (gamestore.db), uygulamanın çalıştığı kök dizinde yer almalıdır.
---
## 4. Ortam Değişkenleri
Uygulamanın hatasız çalışması için sistemde şu ortam değişkenlerinin tanımlı olması önerilir:
1. Java SDK: Uygulama Java 19 ve üzeri sürüm ile uyumludur.
2. JAVA_HOME: JDK kurulum dizini sistem değişkenlerine eklenmiş olmalıdır.
3. PATH: JDK içerisindeki bin klasörü (%JAVA_HOME%\bin) sistem yoluna eklenmiş olmalıdır.
4. JavaFX Modül Ayarları: Eğer kütüphaneler IDE üzerinden otomatik tanınmazsa, VM Options kısmına şu argüman eklenmelidir:
   --module-path "lib" --add-modules javafx.controls,javafx.fxml
---
## 5. İlk Çalıştırma ve Kurulum (DİKKAT!)
Sistemi ilk kez ayağa kaldırırken:
1. `Main` sınıfını açınız.
2. Kodun içerisindeki **IGDB API verilerini çeken yorum satırındaki kod bloğunu** bulunuz.
3. Bu kod bloğunu yorum satırından çıkartıp (uncomment) uygulamayı **sadece 1 defaya mahsus** çalıştırınız.
4. Kurulumdan sonra o kodu tekrar yorum satırına alabilirsiniz.

---
## 6. Sisteme Giriş ve Test
İlk kurulum kodunu çalıştırdıktan sonra uygulamanın **"Kayıt Ol"** ekranı üzerinden anında yeni bir 'User' veya 'Publisher' hesabı oluşturarak sistemi test edebilirsiniz.
# 🎮 Dijital Oyun Dağıtım ve Kütüphane Yönetim Sistemi
**BLM2042 Sistem Analizi ve Tasarımı Dönem Projesi**

## 📌 1. Proje Bilgileri
Bu proje, kullanıcıların dijital oyunları inceleyebileceği, satın alabileceği, kütüphanelerini yönetebileceği ve arkadaşlarıyla etkileşime geçebileceği bir platformun prototipidir.
* **Geliştirme Dili:** Java
* **Arayüz (GUI):** JavaFX (FXML)
* **Veritabanı:** SQLite
* **Mimari:** Katmanlı Mimari (Model - DAO - Controller)

---
## ⚙️ 2. Gerekli Kütüphaneler ve Ortam
1. Projeyi IDE ile açın.
2. **Veritabanı Bağlantısı:** Sistem, gömülü SQLite kullanmaktadır.
3. **JDBC Sürücüsü:** Kodun veritabanı ile iletişim kurabilmesi için projeye (Build Path / Dependencies üzerinden) `sqlite-jdbc.jar` kütüphanesi mutlaka eklenmelidir.

---
## 🚀 3. İlk Çalıştırma ve Kurulum (DİKKAT!)
Sistemi ilk kez ayağa kaldırırken:
1. `Main` sınıfını açınız.
2. Kodun içerisindeki **IGDB API verilerini çeken yorum satırındaki kod bloğunu** bulunuz.
3. Bu kod bloğunu yorum satırından çıkartıp (uncomment) uygulamayı **sadece 1 defaya mahsus** çalıştırınız.
4. Kurulumdan sonra o kodu tekrar yorum satırına alabilirsiniz.

---
## 🔑 4. Sisteme Giriş ve Test
İlk kurulum kodunu çalıştırdıktan sonra uygulamanın **"Kayıt Ol"** ekranı üzerinden anında yeni bir 'User' veya 'Publisher' hesabı oluşturarak sistemi test edebilirsiniz.
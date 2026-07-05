# 📱 MetNET V14 Android Projesi - ATILIM Sürümü 🚀

MetNET, gelişmiş meteorolojik verileri işlemek, analiz etmek ve Android kullanıcılarına yüksek doğrulukla sunmak amacıyla geliştirilmiş bir mobil hava tahmini ve gözlem uygulamasıdır. **V14 ATILIM** sürümüyle birlikte mobil mimari altyapı tamamen modernize edilmiş; daha dinamik, hızlı ve kullanıcı dostu bir deneyim sunmak üzere optimize edilmiştir. ✨

---

## 📌 Proje Hakkında ve Temel Amaç 🎯

MetNET Android uygulaması, mikro iklim hareketlerinden makro düzeydeki hava sistemlerine kadar geniş bir yelpazedeki verileri mobil cihazlarda akıcı bir şekilde görselleştirmek amacıyla geliştirilmiştir. 🌪️ Bu projenin temel amacı; dağınık haldeki gözlem istasyonları verileri ile yüksek çözünürlüklü radar sinyallerini cihaz üzerinde ve sunucu tarafında işleyerek kullanıcıya anlık bildirimler ve doğru tahminler ulaştırmaktır. 🗺️

V14 sürümüyle birlikte uygulama, statik veri gösteriminden çıkarak dinamik tahminleme, anlık veri akışı ve modern bileşen tabanlı arayüz mimarisine geçiş yapmıştır. ⚡

---

## 🛠️ Neler Yapıldı ve Mimari Yenilikler ⚙️

### 1. Gözlem ve Radar Sistemlerinin Merkezileştirilmesi 📡
* 🏢 Kocaeli genelinde aktif olarak çalışan tüm otomatik ve manuel meteoroloji gözlem istasyonları, mobil arayüzde veri karmaşasını önlemek amacıyla tek bir menü altında birleştirildi.
* 🧠 Derince merkezli olarak konumlandırılan yeni nesil Yapay Zekâ destekli radar sistemi uygulamaya entegre edildi. Sistem, güncel radar verilerini arka planda analiz ederek gökyüzü anomalileri ve anlık yağış yoğunluklarına dair işlevsel ve kısa vadeli tahminler üretir. ⛈️

### 2. Gelişmiş Sayısal Hava Tahmin Modellerinin Entegrasyonu 📈
Mobil uygulamanın tahmin yeteneklerini artırmak amacıyla küresel ve bölgesel düzeyde başarısını kanıtlamış gelişmiş sayısal hava tahmin modelleri sisteme dahil edilmiştir:

* 🌦️ **MGM ALARO Anlık:** 1.5 km çözünürlüğe sahiptir. Radar ve bulutlanma verileriyle anlık güncellenir. 15 dakika aralıklarla saatlik tahmin üretir ve toplamda 1.5 günlük tahmin sunar.
* 🌤️ **MGM ALARO Standart:** 4.5 km çözünürlüğe sahiptir. 1 saat aralıklarla 3 günlük tahmin sunar.
* 🌍 **UKMO İngiltere:** 10 km çözünürlüğe sahiptir. Tüm parametreleri barındırır. Dünyanın en fazla sayısal denklem çözen modeli olduğu için erişimi kısıtlıdır.

---

## 🧰 Teknolojik Altyapı ve Yararlanılan Bileşenler 💻

MetNET Android APK projesi geliştirilirken yüksek performans, düşük pil tüketimi ve akici animasyonlar hedeflenmiştir. 🔋 Projede yararlanılan temel teknolojiler şunlardır:

* ☕ **Dil ve Arayüz Mimarisi:** Uygulama modern Android standartlarına uygun olarak Kotlin diliyle yazılmış ve arayüz tasarımı bileşen odaklı mimariyle (Jetpack Compose) şekillendirilmiştir. 🎨
* 🔄 **Arka Plan Görevleri ve Veri Akışı:** İstasyon verilerinin ve radar grafiklerinin arka planda donmadan yüklenmesi için asenkron programlama bileşenleri (Coroutines & Flow) kullanılmıştır.
* 🌐 **Ağ Yönetimi:** MGM API servisleri ve radar veri merkezleri ile iletişim sağlamak, JSON verilerini güvenli şekilde indirmek için gelişmiş ağ kütüphanelerinden (Retrofit) yararlanılmıştır.
* 💾 **Yerel Veri Tabanı:** Uygulamanın çevrimdışı modda da son alınan tahminleri gösterebilmesi için cihaz içi yerel veri tabanı mimarisi (Room DB) entegre edilmiştir.

---

## 🚀 Kurulum ve Çalıştırma 🛠️

Projeyi yerel geliştirme ortamınızda açmak ve test APK dosyasını derlemek için aşağıdaki adımları takip edebilirsiniz.

### 📋 Gereksinimler
* Android Studio Jellyfish veya üzeri bir sürüm 💻
* Android SDK 34 (Android 14) ve üzeri 🤖
* Java Development Kit (JDK) 17 ☕

### ⚙️ Adımlar
1. Bu depoyu bilgisayarınıza klonlayın:
   ```bash
   git clone [https://github.com/kullanici-adiniz/MetNET-Android.git](https://github.com/kullanici-adiniz/MetNET-Android.git)

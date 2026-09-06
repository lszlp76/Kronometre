# INDUSTRIAL CHRONOMETER - TEKNİK DOKÜMANTASYONU

## 📱 PROJE GENEL BAKIŞ

**Industrial Chronometer**, endüstriyel zaman ölçümü ve cycle time analizi için geliştirilmiş gelişmiş bir Android kronometre uygulamasıdır.

### 🎯 Temel Özellikler
- **Çoklu Zaman Birimleri**: Saniye, Santidakika, Desimdakika
- **Cycle Time Analizi**: Lap kayıtları ve istatistiksel analiz
- **Veri Dışa Aktarma**: Excel formatında raporlama
- **Grafiksel Gösterim**: Real-time performans grafikleri (MPAndroidChart)
- **Widget Desteği**: Ana ekrandan hızlı erişim
- **Arka Plan Çalışması**: Foreground service ile sürekli zaman ölçümü
- **Tema Desteği**: Light, Dark ve System Theme seçenekleri (Yüksek kontrastlı okunabilirlik odaklı)
- **Uygulama İçi Satın Alma (In-App Purchase)**: Google Play Billing Library 8.3.0 ile "Remove Ads" (Reklamları Kaldır) entegrasyonu ve dinamik menü gizleme.

## 🏗️ MİMARİ YAPI & TEKNOLOJİ YIĞINI

### Teknoloji Yığını
- **Dil**: Java
- **Mimari**: MVVM + Fragments
- **Veri Paylaşımı**: ViewModel + LiveData + LocalBroadcastManager
- **Servis**: Foreground Service (`specialUse`)
- **Veritabanı/Kayıt**: SharedPreferences + File Storage
- **Build Sistemi**: Android Gradle Plugin (AGP) **9.3.2**, Gradle **9.5.0**
- **Performans & Optimizasyon**: R8 Code Shrinking & Resource Optimization (`minifyEnabled true`, `shrinkResources true`)
- **Monetization**: Google Play Billing Library **8.3.0**

### Modül Yapısı
```
app/
├── MainActivity (Ana kontrolör, Navigation, Tema ve Billing yönetimi)
├── TimerFragment (Zaman ölçüm ekranı, Lap yönetimi ve Banner reklam entegrasyonu)
├── ChartFragment (Grafik analiz)
├── FileList (Dosya yönetimi)
├── ChronometerService (Arka plan servisi)
├── PageViewModel (Veri paylaşımı)
└── CustomAlertDialogFragment (Özel diyaloglar)
```

## 📄 KRİTİK SINIF DOKÜMANTASYONU

### 1. MainActivity.java

#### 📋 Sınıf Tanımı
Uygulamanın ana aktivitesi; Navigation Drawer, Tab yapısı, Tema yönetimi ve Google Play Billing istemcisini yönetir.

#### 🔧 Temel Sorumluluklar
- **Edge-to-Edge Desteği**: Modern Android ekran uyumluluğu (`EdgeToEdge.enable(this)`).
- **Theme Manager**: Light, Dark ve System tema tercihini `SharedPreferences` ile kaydetme ve `AppCompatDelegate.setDefaultNightMode()` ile uygulama genelinde uygulama.
- **Google Play Billing 8.3.0**: Reklamları kaldırma (`remove_ads`) ürününü sorgulama, satın alma akışı (`launchBillingFlow`), mevcut satın alımları doğrulama (`checkExistingPurchases`) ve satın alım başarılı olduğunda (`hideAdsFromUI`) banner/geçiş reklamlarını devre dışı bırakıp menüdeki "Remove Ads" öğesini gizleme.

### 2. TimerFragment.java

#### 📋 Sınıf Tanımı
Zaman ölçümü, lap yönetimi, veri kaydetme ve reklam gösteriminin yapıldığı ana fragment.

#### 🔧 Temel Sorumluluklar
- Zaman birimi modülleri (`modul`, `milis`) yönetimi.
- Lap listesi ve istatistik hesaplamaları (`min`, `max`, `ave`).
- Banner reklam (`AdView`) yönetimi (Kullanıcı reklamları kaldırdıysa otomatik olarak `GONE` durumuna getirilir ve durdurulur).

### 3. ChronometerService.java

#### 📋 Sınıf Tanımı
Arka planda zaman ölçümünü sürdüren foreground service.

### 4. PageViewModel.java

#### 📋 Sınıf Tanımı
Fragment'lar arası veri paylaşımını sağlayan ViewModel.

## 🎨 TEMA VE KONTRAST YÖNETİMİ

Uygulama, Light (Açık) ve Dark (Koyu) modlarda kusursuz okunabilirlik sağlamak üzere yüksek kontrastlı renk paletleriyle donatılmıştır:
- **Light Mode (`res/values/colors.xml`)**: Ferah ve açık arkaplan (`#F1F5F9`), yüksek kontrastlı koyu sayaç ve metin renkleri (`#0F172A`) ve koyu grafik hatları (`#1E293B`) ile her koşulda mükemmel okunabilirlik sunar.
- **Dark Mode (`res/values-night/colors.xml`)**: Göz yormayan koyu arkaplanlar (`#2D2D30`) ve parlak/kontrastlı metinler (`#FFFFFFFF`).

## 💰 GOOGLE PLAY BILLING 8.3.0 ENTEGRASYONU

- **Güvenli Başlatma**: `BillingClient` oluşturulurken `.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())` kullanılarak eksiksiz işlem desteği sağlanmıştır.
- **Dinamik Menü Yönetimi**: Kullanıcı reklamları kaldırma (`remove_ads`) satın alımını gerçekleştirdiğinde veya geri yüklediğinde:
  - Interstitial (Geçiş) ve Banner reklamlar tamamen devre dışı bırakılır.
  - Navigasyon menüsündeki "Remove Ads" öğesi otomatik olarak gizlenir (`setVisible(false)`).

## 🛠️ KURULUM VE YAPILANDIRMA

### Gereksinimler
- **Minimum SDK**: API 29 (Android 10)
- **Target SDK**: API 36 (Android 15+)
- **AGP**: 9.3.2
- **Gradle**: 9.5.0

### Build Konfigürasyonu & R8 Optimizasyonu
```gradle
android {
    compileSdk 37
    defaultConfig {
        applicationId "com.lszlp.choronometre"
        minSdk 29
        targetSdk 36
        versionCode 52
        versionName '6.0'
    }
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

**Doküman Versiyonu**: 2.0  
**Son Güncelleme**: 2026 (C) LsZLP

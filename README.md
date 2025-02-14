Android uygulamanız için Türkçe dokümantasyon hazırlıyorum:

# Endüstriyel Kronometre Uygulaması Dokümantasyonu

## Genel Bakış

Bu uygulama, endüstriyel zaman etüdü çalışmaları için geliştirilmiş bir kronometre uygulamasıdır. Temel özellikleri:

- Kronometre ölçümü
- Tur zamanı kaydetme
- Grafik görüntüleme 
- Excel formatında veri kaydetme ve paylaşma
- Farklı zaman birimleri desteği (saniye, dakika)

## Ana Bileşenler

### 1. TimerFragment

Ana kronometre işlevselliğini sağlayan fragment:

- Start/Stop kontrolü
- Tur zamanı kaydetme
- Temel istatistikler (min/max/ortalama)
- Zaman birimi değiştirme

```java
public class TimerFragment extends Fragment {
    // Kronometre mantığı
    public void start() { ... }
    public void stop() { ... }
    public void takeLap() { ... }
    public void reset() { ... }
}
```

### 2. ChartFragment 

Tur zamanlarının grafik gösterimini sağlar:

- MPAndroidChart kütüphanesi kullanılır
- Minimum, maksimum ve ortalama değerler gösterilir
- Gerçek zamanlı güncelleme

### 3. ExcelSave

Verilerin Excel formatında kaydedilmesi ve paylaşılması:

```java
public class ExcelSave {
    public void save() { ... } // Excel dosyası oluşturma
    public void share() { ... } // Dosya paylaşımı
}
```

## Veri Akışı

1. TimerFragment'ta kronometre çalıştırılır
2. Tur zamanları kaydedilir
3. PageViewModel üzerinden ChartFragment'a veriler aktarılır
4. Veriler grafik olarak gösterilir
5. İstenirse Excel olarak kaydedilir

## Dosya Yapısı

- `app/src/main/java/com/lszlp/choronometre/`
  - `TimerFragment.java` - Ana kronometre mantığı
  - `ChartFragment.java` - Grafik gösterimi
  - `ExcelSave.java` - Excel işlemleri
  - `FileList.java` - Kaydedilen dosyaların listesi
  - `MainActivity.java` - Ana aktivite

## Önemli Özellikler

1. **Zaman Birimleri**
   - Saniye
   - Yüzdelik dakika (Cmin)
   - Ondalık dakika (Dmin)

2. **Veri Kaydetme**
   - Excel formatında kayıt
   - Dosya paylaşımı
   - Notlar ekleme

3. **Grafik Özellikleri**
   - Gerçek zamanlı güncelleme
   - Min/Max/Ortalama göstergeleri
   - Özelleştirilebilir görünüm

## Kullanılan Kütüphaneler

- MPAndroidChart - Grafik gösterimi
- JExcel - Excel dosya işlemleri
- AndroidX bileşenleri
- Google Play Services (reklam entegrasyonu)

## İzinler

AndroidManifest.xml'de tanımlanan izinler:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Ekran Koruyucu

Uygulama ayarlarından ekran koruyucu özelliği kontrol edilebilir:
```java
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
```

## Tema ve Görünüm

- Özelleştirilebilir renk şeması
- Karanlık/Aydınlık tema desteği
- Özel buton stilleri


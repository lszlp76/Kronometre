⏱️ Multi-Unit Chronometer Projesi (lszlp/choronometre)
🌟 Proje Tanımı
Bu proje, profesyonel kullanıma uygun, gelişmiş bir Android kronometre uygulamasıdır. Standart saniye bazlı zamanlamanın yanı sıra, Santidakika (Cmin) ve Desimdakika (Dmh) gibi bilimsel ve endüstriyel standartlara uygun zaman birimlerinde de ölçüm yapabilme yeteneği sunar. Uygulama, arka planda güvenilir bir şekilde çalışırken detaylı tur (lap) kaydı ve veri analizi özelliklerini içerir.

✨ Temel Özellikler
Çoklu Zaman Birimi Desteği:

Saniye Bazlı (Sec.)

Santidakika Bazlı (Cmin.)

Desimdakika Bazlı (Dmh.)

Güvenilir Arka Plan Çalışması: Android'in Foreground Service yapısı sayesinde uygulama arkaplanda veya ekran kapalıyken bile kesintisiz ve hassas ölçüm yapar.

Detaylı Tur (Lap) Kaydı: Her tur için ayrıntılı zamanlama bilgisi kaydedilir.

Veri Yönetimi ve Analizi:

Turların minimum, maksimum ve ortalama süreleri (PageViewModel tarafından) hesaplanır.

Veriler bir grafik görünümünde (ChartFragment) görselleştirilir.

Kayıtlı veriler (tur listeleri) dosya olarak saklanır (FileList).

Esnek Kullanıcı Arayüzü: ViewPager ve TabLayout ile kronometre, grafik ve dosya listesi arasında kolay geçiş.

Veri Dışa Aktarımı: Kaydedilen verilerin dışa aktarılmasına (örneğin Excel'e) olanak tanır (ExcelSave sınıfı).

Bildirim Entegrasyonu: Çalışma durumu, devam eden bildirim (Notification) aracılığıyla anlık olarak takip edilebilir ve kontrol edilebilir (Durdur/Başlat).

Not Ekleme: Her bir tura özel notlar eklenebilir.

💻 Kullanılan Teknolojiler
Kategori	Teknoloji / Bileşen	Açıklama
Platform	Android SDK	Uygulamanın temel geliştirme ortamı.
Dil	Java	Projenin ana programlama dili.
Mimari	MVVM Prensibi	Veri yönetimi için ViewModel ve LiveData kullanılır (PageViewModel).
Arkaplan	Foreground Service (ChronometerService)	Kronometrenin kesintisiz çalışması ve 5 saniye kuralına uyum.
İletişim	Local Broadcast Manager	Servis (ChronometerService) ve UI (TimerFragment) arasındaki veri akışı.
UI	View Pager, Tab Layout, Fragment	Sekmeli ve kaydırılabilir arayüz yapısı.
Arayüz	View Binding (FragmentTimerBinding, LaprowsBinding)	View'lara daha güvenli erişim.
Hata Yönetimi	Handler ve Runnable	Zamanlayıcı ve UI Thread güvenliği için kullanılır.
Ek	AdMob	Reklam entegrasyonu (MainActivity'de belirtilmiştir).
🚀 Kurulum ve Çalıştırma
Geliştirme ortamınızda projeyi ayağa kaldırmak için aşağıdaki adımları takip edin:

Projeyi Klonlayın:

Bash
git clone [repo_adresi]
Android Studio'da Açın: Proje klasörünü Android Studio'da açın.

SDK Gereksinimleri: Projenin gerektirdiği minimum ve hedef SDK versiyonlarının kurulu olduğundan emin olun.

Derleme: Projeyi derleyin ve bir emülatör veya fiziksel cihaza yükleyin.

🛠️ Temel Proje Yapısı
Dosya Adı	Açıklama
TimerFragment.java	Kronometre ekranı, başlatma/durdurma mantığı ve lap listesi.
ChronometerService.java	Arka plan zamanlama mantığını yöneten temel hizmet sınıfı. Foreground Service burada tanımlanır.
PageViewModel.java	Fragmentlar arası veri paylaşımı ve grafik/istatistik verilerini tutar (Min/Max/Avg/Lap değerleri).
Constants.java	Uygulama genelinde kullanılan Action, Extra ve Bildirim sabitlerini barındırır.
Lap.java / LapListAdapter.java	Tur verileri (lap) modeli ve RecyclerView adaptörü.
FileList.java	Kaydedilen Excel/CSV dosyalarını yönetme ekranı.
CustomAlertDialogFragment.java	Reset, Save ve Not Ekleme gibi özel diyalog pencerelerini yönetir.
👨‍💻 Katkıda Bulunma
Bu projeye katkıda bulunmaktan memnuniyet duyarım! Lütfen herhangi bir hata bildirimi veya özellik önerisi için bir Issue açın veya bir Pull Request gönderin.

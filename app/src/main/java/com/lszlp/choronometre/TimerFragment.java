package com.lszlp.choronometre;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager; // Bu import kontrol edilmeli
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Handler;
import android.os.Looper;
import com.lszlp.choronometre.databinding.FragmentTimerBinding;
import com.lszlp.choronometre.main.PageViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
public class TimerFragment extends Fragment {
    public String Timeunit;

    private final Handler uiHandler = new Handler(Looper.getMainLooper()); // UI Thread Handler'ı
    private static final long REQUEST_DELAY_MS = 100; // 100 milisaniye gecikme
    private FragmentTimerBinding _binding;

    private FragmentTimerBinding getBinding() {
        return _binding;
    }

    private BroadcastReceiver timeUpdateReceiver;
    SpannableString spannableString;
    TextView maxvalue, minvalue, totalObservationTime, cycPerHour, cycPerMinute;
    Button button2, button1, button3, button4, button;
    String lapToWrite;
    String timeString;
    Lap lapValue;
    ArrayList<Lap> lapsArray = new ArrayList<>();
    ArrayList<Lap> ListElementsArrayList;
    LapListAdapter lapListAdapter;
    // TimerFragment.java (Sınıf değişkenleri arasına ekleyin)
    private BroadcastReceiver precisionUpdateReceiver;
    private boolean isServicePaused = false; // statusResponseReceiver içinde kullanıldığı için eklenmeli/kontrol edilmeli
    // YENİ: Servisten gelen durum (zaman ve çalışma/duraklatma) yanıtlarını dinler

    private final BroadcastReceiver statusResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_STATUS_RESPONSE.equals(intent.getAction())) {

                // 1. Servis'ten gelen değerleri al
                long serviceElapsedTime = intent.getLongExtra(Constants.EXTRA_ELAPSED_TIME, 0);
                boolean serviceIsRunning = intent.getBooleanExtra(Constants.EXTRA_IS_RUNNING, false);
                boolean serviceIsPaused = intent.getBooleanExtra(Constants.EXTRA_IS_PAUSED, false);

                Log.d("TimerFragment", "STATUS_RESPONSE alındı. Elapsed: " + serviceElapsedTime);

                // 2. Fragment'ın kendi değişkenlerini güncelle
                lastKnownElapsedTime = serviceElapsedTime; // Fragment'ın kendi değişkeni olmalı
                Boolean isRunning = serviceIsRunning;
               // Boolean isPaused = serviceIsPaused;
                // 🔥 KRİTİK EKLEME: ÜYE DEĞİŞKENİ GÜNCELLE-12.11.2025 uı backgroun uyumusuzluk problemi
                isServicePaused = serviceIsPaused; // Servis'ten gelen duraklatma durumunu Fragment'ا kaydet!
                // 3. UI'ı GÜNCELLE
                updateTimeDisplay(lastKnownElapsedTime); // Bu metot, TextView'i güncelleyen metottur.
                updateButtonStates();
                ; // Bu metot, butonu başlat/duraklat durumuna göre günceller.

                // 4. KRİTİK: Eğer Servis ÇALIŞIYOR ise, Fragment da zaman güncellemelerini almaya devam etmeli.
                if (isRunning) {
                    // Zaman güncelleme döngüsünü Fragment'ta başlatmak yerine,
                    // Servis zaten zaman güncelleme yayınları (ACTION_TIME_UPDATE) gönderiyorsa
                    // Fragment bu yayınları beklemelidir.
                    // Eğer Servis, ACTION_TIME_UPDATE göndermeyi bıraktıysa, UI güncellenmez.

                    // Eğer zaman anında güncellenmiyorsa:
                    // updateTimeDisplay(lastKnownElapsedTime); çağrısının hemen ardından
                    // Zaman güncelleme yayınlarını (ACTION_TIME_UPDATE) beklemeye başlanır.
                    // Bu yüzden 3. adımdaki UI güncellemesi HAYATİDİR.
                }
            }
        }
    };

    private final BroadcastReceiver adsRemovedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_ADS_REMOVED.equals(intent.getAction())) {
                if (_binding != null && _binding.adView != null) {
                    _binding.adView.setVisibility(View.GONE);
                    _binding.adView.pause();
                }
            }
        }
    };

    //BUNU tekrar kullan

    //    private BroadcastReceiver statusResponseReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Constants.ACTION_STATUS_RESPONSE)) {
//
//                boolean isRunning = intent.getBooleanExtra(Constants.EXTRA_IS_RUNNING, false);
//                boolean isPausedFromService = intent.getBooleanExtra(Constants.EXTRA_IS_PAUSED, false);
//                long elapsed = intent.getLongExtra(Constants.EXTRA_ELAPSED_TIME, 0L);
//
//                // Fragment'ın kendi durum değişkenini servisten gelen değere göre ayarla
//                isServicePaused = isPausedFromService;
//
//                // Eğer servis çalışıyorsa (ÇALIŞIYOR veya DURAKLATILMIŞ)
//                if (isRunning || isPausedFromService) {
//                    // Fragment'a gelen son zaman değerini UI'da göster
//                    // Fragment'ta tanımlı formatTime(long millis) metodunu kullanın
//                    String timeString = formatTime(elapsed);
//                    pageViewModel.setTimerValue(timeString);
//                } else {
//                    // Servis tamamen durmuş (RESET)
//                    resetTimerUI(); // UI'ı sıfırlayan bir metot çağırın
//                }
//
//                // Buton durumlarını güncelle
//                updateButtonStates();
//            }
//        }
//    };
    // YENİ METOT: UI'ı sıfırlama işlevini gruplandır
    private void resetTimerUI() {
        // ViewModel'i ve UI'ı sıfırla
        if (pageViewModel != null) {
            // "00:00:00.000" sizin formatınıza göre ayarlanmalıdır.
            pageViewModel.setTimerValue("00:00:00.000");
        }
        // Diğer sıfırlama işlemleri (örneğin lap listesi) burada yapılır.
    }


    public void setUnitDisplay(String unitValue) {
        Log.d("lifeCycle","setUnitDisplay çalıştı");

        // Handle null or empty unit value
        if (unitValue == null || unitValue.trim().isEmpty()) {
            unitValue = "No Unit";
        }
        FragmentTimerBinding currentBinding = getBinding();

        // Binding'in ve Fragment'ın hazır olduğunu kontrol et
        if (currentBinding == null || !isAdded()) {
            Log.w("TimerFragment", "Cannot set unit text, view is destroyed or fragment detached.");
            return;
        }


            this.unit = unitValue;

        // TextView'ı güncelle
        if (currentBinding.unitValue != null) {
            currentBinding.unitValue.setText(unitValue);
        }
    }

    /*Timer düzeltme için
    Runnable runnable;
    Handler handler;

 String hh, mm, ss, msec;
    long MillisecondTime, StopTime, StartTime, TimeBuff, UpdateTime = 0L;
     int Hours, Seconds, Minutes, MilliSeconds;

long MillisecondTime, StopTime, StartTime, TimeBuff, UpdateTime = 0L;

// String hh, mm, ss, msec;
// long elapsedTime = 0;
// boolean running = false;
// private long startTime = 0;
    private long startTime = 0;
     */
    int Hours, Seconds, Minutes, MilliSeconds;

    boolean go;
    int number, micro;
    String unit, diffTime;
    String second = null, minute = null, hour = null;
    String timer = "00:00:00.000";
    String departure = "---";
    int modul;
    int milis;
    int m;
    int h;

    int lapsayisi = 0;
    ArrayList<String> laps = new ArrayList<>();

    boolean Auth;
    ArrayList<Double> lapsval = new ArrayList<>();
    int lapnomax = 0, lapnomin = 0;
    double min, max, ave;
    PageViewModel pageViewModel;
    ExcelSave excelSave = new ExcelSave();
    List<String> saveValue;
    FragmentTimerBinding currentBinding;

    String currentDateandTimeStop;
    String currentDateandTimeStart;
    Date timeStop, timeStart;
    boolean isNoPressed;
    private DateFormat df = new SimpleDateFormat("ddMMyy@HHmm");
    private DateFormat df2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ", Locale.ENGLISH);
    private String m_Text = "";
    private TextView avevalue;
    private long lastKnownElapsedTime = 0L; // Servisten gelen son zamanı burada tutacağız

    public static TimerFragment newInstance() {
        return new TimerFragment();
    }

    private String currentDecimalFormatPattern = "#0.0"; // Varsayılan: 1 ondalık

    // State'leri kaydet
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Auth=false (çalışıyor), Auth=true (durmuş)
        outState.putBoolean("Auth", Auth);

        // 'elapsedTime' yerine servisten gelen son zamanı kaydediyoruz
        outState.putLong("lastKnownElapsedTime", lastKnownElapsedTime);


    }

    // Fragment ön plana geldiğinde (arka plandan geri dönme dahil)
    @Override
    public void onResume() {
        super.onResume();
//      Kullanıcı ayarları değiştirip geri gelirse format desenini güncelle
        currentDecimalFormatPattern = getDecimalFormatPattern();
        // 1. Alıcıları KAYDET (Çok önemli, her zaman burada yapılmalı)
        // Zaman Güncelleme Alıcısı
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                timeUpdateReceiver,
                new IntentFilter(Constants.ACTION_TIME_UPDATE)
        );
        // Durum Yanıtı Alıcısı
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                statusResponseReceiver,
                new IntentFilter(Constants.ACTION_STATUS_RESPONSE)
        );
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                adsRemovedReceiver,
                new IntentFilter(Constants.ACTION_ADS_REMOVED)
        );
        if (_binding != null && _binding.adView != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            if (prefs.getBoolean("is_ads_removed", false)) {
                _binding.adView.setVisibility(View.GONE);
                _binding.adView.pause();
            }
        }
        //IntentFilter timeUpdateFilter = new IntentFilter(Constants.ACTION_TIME_UPDATE);
        //LocalBroadcastManager.getInstance(requireContext()).registerReceiver(timeUpdateReceiver, timeUpdateFilter);

        //IntentFilter statusResponseFilter = new IntentFilter(Constants.ACTION_STATUS_RESPONSE);
        //LocalBroadcastManager.getInstance(requireContext()).registerReceiver(statusResponseReceiver, statusResponseFilter);

        // ... (Varsa diğer alıcıları da buraya ekleyin)

        // 2. KRİTİK: Servisten mevcut durumu ve zamanı talep et
        // Bu, uygulamanın ön plana geldiğinde zamanın hemen güncellenmesini sağlar.
        requestCurrentStatus();
        Log.d("TimerFragment", "onResume çalıştı");
// KRİTİK DÜZELTME: Durum isteğini küçük bir gecikmeyle gönder.
        // Bu, alıcıların tamamen kaydedildiğinden emin olmak için bir yarış koşulu düzeltmesidir.
        uiHandler.postDelayed(this::requestCurrentStatus, REQUEST_DELAY_MS);

        Log.d("TimerFragment", "onResume: Alıcılar kaydedildi, durum isteği " + REQUEST_DELAY_MS + "ms sonra gönderilecek.");
        // Not: Mevcut zaman güncelleme alıcınızın (timeUpdateReceiver) da burada kaydedildiğinden emin olun!
    }

    // requestCurrentStatus() metodunuzun da var ve doğru çalıştığından emin olun.
// Eğer yoksa, bu metodu ekleyin:
    private void requestCurrentStatus() {
        if (!isAdded()) {
            return; // Fragment View'a bağlı değilse işlemi yapma
        }


        if (isAdded()) { // Fragment'ın bir Context'e bağlı olduğunu kontrol et
            Intent statusIntent = new Intent(Constants.ACTION_REQUEST_STATUS);
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(statusIntent);
            Log.d("TimerFragment", "requestCurrentStatus: Durum isteği Servise gönderildi.");
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {

            // 'running' yerine 'Auth' durumunu geri yükle
            // Varsayılan: true (durmuş)
            Auth = savedInstanceState.getBoolean("Auth", true);

            // 'elapsedTime' yerine 'lastKnownElapsedTime'ı geri yükle
            lastKnownElapsedTime = savedInstanceState.getLong("lastKnownElapsedTime", 0L);

            // Durumu UI'daki butonlara yansıt
            updateButtonStatesBasedOnAuth();

            // Eğer zaman 0'dan büyükse, ekranı son bilinen zamanla güncelle
            // (Bu, ekran döndüğünde "00:00:00" görmeyi engeller)
            if (lastKnownElapsedTime > 0) {
                updateTimeDisplay(lastKnownElapsedTime);
            }
        }

    }

    /**
     * Servisi başlatmadan/durdurmadan,
     * sadece 'Auth' değişkenine göre butonların görünümünü (START/STOP) günceller.
     * Ekran dönüşleri (rotation) için kullanılır.
     */
    private void updateButtonStatesBasedOnAuth() {
        if (getActivity() == null || _binding == null) return;

        if (Auth) { // Durum: Durmuş (Auth = true)
            button2.setText("START");
            button3.setEnabled(false);
            button.setEnabled(true);
            button4.setEnabled(true);
        } else { // Durum: Çalışıyor (Auth = false)
            button2.setText("STOP");
            button3.setEnabled(true);
            button.setEnabled(false);
            button4.setEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
        // UI Thread'e geçişi sağlamak için Handler tanımla
        final Handler uiHandler = new Handler(requireContext().getMainLooper());
currentBinding = getBinding();
        timeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.ACTION_TIME_UPDATE.equals(intent.getAction())) {
                    // Constants.EXTRA_ELAPSED_TIME kullanın
                    long elapsed = intent.getLongExtra(Constants.EXTRA_ELAPSED_TIME, 0);
                    Log.d("ChronoUpdate", "Elapsed Time Received: " + elapsed);
                    lastKnownElapsedTime = elapsed;

                    uiHandler.post(() -> {
                        // 1. ZAMANI GÜNCELLE
                        updateTimeDisplay(elapsed);

                        // 2. VIEWMDOEL'I GÜNCELLE (Chart Fragment için)
                        String timeString = formatTime(elapsed);
                        if (pageViewModel != null) {
                            pageViewModel.setTimerValue(timeString);
                        }

                        // Buton durumlarını güncelle (Gerekirse)
                        updateButtonStates();
                    });
                }
                // PAUSE / RESUME durumlarını yakalamak için
                if (Constants.ACTION_PAUSE.equals(intent.getAction())) {
                    isServicePaused = true;
                    updateButtonStates();
                } else if (Constants.ACTION_RESUME.equals(intent.getAction())) {
                    isServicePaused = false;
                    updateButtonStates();
                }
            }
        };
// YENİ: PRECİSİON Güncelleme Alıcısı Tanımı ve Kaydı
        precisionUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Constants.ACTION_PRECISION_UPDATE.equals(intent.getAction())) {
                    // 1. Yeni format desenini SharedPreferences'tan oku
                    currentDecimalFormatPattern = getDecimalFormatPattern();

                    // 2. UI'daki zamanı hemen yeni formatla güncelle
                    // lastKnownElapsedTime: Servisten gelen son zaman değeri
                    uiHandler.post(() -> {
                        // updateTimeDisplay metodu, zamanı yeni hassasiyetle formatlar.
                        updateTimeDisplay(lastKnownElapsedTime);
                    });

                    Log.d("TimerFragment", "Precision güncellendi. Yeni format: " + currentDecimalFormatPattern);
                }
            }
        };

        // Precision alıcısını kaydet
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                precisionUpdateReceiver,
                new IntentFilter(Constants.ACTION_PRECISION_UPDATE)
        );
        // Filtreyi hem zaman hem de durum güncellemeleri için ayarlayın
        IntentFilter filter = new IntentFilter(Constants.ACTION_TIME_UPDATE);
        filter.addAction(Constants.ACTION_PAUSE);
        filter.addAction(Constants.ACTION_RESUME);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                timeUpdateReceiver,
                filter
        );
        //time unit pref den almak
       loadTimeUnitPreference();

        registerTimeUnitUpdateReceiver(); // Birim değişikliği alıcısını kaydet
     Log.d("TimerFragment","OnCreate çalıştı");
    }

    // BU METODU EKLEYİN (Binding'i temizlemek için KRİTİK!)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null; // View yok edildiğinde binding'i temizle

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeUpdateReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timeUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (precisionUpdateReceiver != null) { // YENİ EKLEME
            try {
                LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(precisionUpdateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(timeUnitUpdateReceiver);
        }
    }

    // TimerFragment.java (updateTimeDisplay metodu)
    @SuppressLint("SetTextI18n")
    private void updateTimeDisplay(long elapsedMillis) {
       currentBinding = getBinding();
        Log.d("TimerFragment","updateTimeDisplay çalıştı");
        if (currentBinding == null || getActivity() == null || !isAdded()) return;

        // 1. YENİ KISIM: Formatlama metodunu çağırın
        timeString = formatTime(elapsedMillis);

        // DEBUG: Eğer hala çalışmıyorsa, bu log'u ekleyin
        Log.d("UpdateCheck", "TextView güncellendi! Değer: " + timeString);

        // UI'da milisaniye kısmının küçük görünmesini sağlayan SpannableString mantığı
        spannableString = new SpannableString(timeString);
        int dotIndex = timeString.lastIndexOf('.');

        // Milisaniye/Santidakika/Desimdakika kısmını küçült
        // Saniye formatı için son 4 karakteri (örn: .000) küçültür.
        // Diğer formatlarda '.' olmadığı için bu kısım çalışmayacaktır, bu istenen davranıştır.
        if (dotIndex != -1 && dotIndex < timeString.length()) {
            spannableString.setSpan(new RelativeSizeSpan(0.5f), dotIndex, spannableString.length(), 0);
        }

        currentBinding.textView.setText(spannableString);
    }

    // TimerFragment.java'ya bu yeni formatlama metodunu ekleyin
    private String formatTimeAccordingToUnit(long elapsedMillis) {
        long totalMinutes, totalSeconds;
        int hours, minutes, seconds, subUnit;
        String result;

        switch (unit) {
            case "Cmin.": // Santidakika (1 dk = 100 cmin)
                // 1 Cmin = 600ms
                long totalCentiminutes = elapsedMillis / 600;
                hours = (int) (totalCentiminutes / 6000); // 1 saat = 6000 Cmin
                minutes = (int) ((totalCentiminutes % 6000) / 100);
                subUnit = (int) (totalCentiminutes % 100); // Santidakika birimi
                // Format: HH:MM:CMIN
                result = String.format("%02d:%02d:%02d", hours, minutes, subUnit);
                break;

            case "Dmh.": // Desimdakika (1 dk = 10 dmh)
                // 1 Dmh = 360ms
                long totalDeciminutes = elapsedMillis / 360;
                hours = (int) (totalDeciminutes / 10000); // 1 saat = 10000 Dmh
                minutes = (int) ((totalDeciminutes % 10000) / 100);
                subUnit = (int) (totalDeciminutes % 100); // Santidakika birimi (Dmh'deki alt birim)
                // Format: HH:DMH:CM
                result = String.format("%02d:%02d:%02d", hours, minutes, subUnit);
                break;

            case "Sec.": // Saniye (Standart)
            default:
                hours = (int) (elapsedMillis / 3600000);
                minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
                //subUnit = (int) (elapsedMillis % 1000); // Milisaniye
                // Format: HH:MM:SS.MMM
                result = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                break;
        }
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("TimerFragment","OnCreateView çalıştı");
        // Başlangıçta format desenini yükle
        currentDecimalFormatPattern = getDecimalFormatPattern();


        _binding = FragmentTimerBinding.inflate(getLayoutInflater()); // Atamayı buraya yapın
        // 🔥 ÖNEMLİ: Binding oluşturulduktan hemen sonra unit preference'ı yükle

        loadTimeUnitPreference();
        return _binding.getRoot();

    }

    private void loadBannerAd() {
        if (_binding == null || _binding.adView == null) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (prefs.getBoolean("is_ads_removed", false)) {
            _binding.adView.setVisibility(View.GONE);
            _binding.adView.pause();
            return;
        }

        AdView adView = _binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("TimerFragment", "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.e("TimerFragment", "Ad failed to load: " + adError.getMessage());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBannerAd();
// 🔥 ÖNEMLİ: Önce view'ları başlat, sonra unit değerini yükle
        initializeViews();
// 🔥 EKLE: View'lar hazır olduğunda unit değerini tekrar yükle
        loadTimeUnitPreference();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        Log.d("TimerFragment","InıtılizeViews çalıştı");
        cycPerHour = _binding.cycPerHour;
        cycPerMinute = _binding.cycPerMinute;
        totalObservationTime = _binding.totalObservationTime;
        button2 = _binding.button2;
        button1 = _binding.button;
        button3 = _binding.button3;
        button4 = _binding.button4;
        button = _binding.button;
        maxvalue = _binding.maxVal;
        minvalue = _binding.minVal;
        avevalue = _binding.aveVal;


        // 🔥 EKLE: unitValue TextView'ını başlat ve değerini ayarla
        if (_binding.unitValue != null) {
            // Eğer unit değişkeni henüz ayarlanmadıysa, SharedPreferences'tan yükle
            if (unit == null) {
                loadTimeUnitPreference();
            } else {
                _binding.unitValue.setText(unit);
            }
        }

        // Butonları görünür yap
        setWidgetsVisibility(false);
        /*
// GEÇİCİ TEST İÇİN EKLEYİN: Varsayılan saniye modunu zorla
        this.modul = 60;   // 60 saniye
        this.milis = 1000; // 1000 milisaniye (1 saniye)
        this.unit = "sec"; // "sec" birimi
*/
        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        _binding.textView.setText(ssp);

        button3.setEnabled(false);
        button.setEnabled(false);
        button4.setEnabled(false);
        // handler = new Handler();
        Auth = true;
    }

    private void setupRecyclerView() {
        Log.d("TimerFragment","SetupRecyclerVİew  çalıştı");
        ListElementsArrayList = new ArrayList<>();
        _binding.lapList.setLayoutManager(new LinearLayoutManager(getContext()));
        lapListAdapter = new LapListAdapter(ListElementsArrayList);
        _binding.lapList.setAdapter(lapListAdapter);

        lapListAdapter.setOnItemClickListener(new LapListAdapter.OnItemClickListener() {
            @Override
            public void onAddMessage(int position) {
                showNoteDialog(position);
            }
        });
        lapListAdapter.setOnItemLongClickListener(new LapListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                showLapDeleteRowDialog(position);

            }
        });
    }


    private void setupClickListeners() {
        Log.d("ModulCheck", "Mevcut Modul Değeri: " + modul); // YENİ SATIR
        button2.setOnClickListener(v -> {
            if (modul > 0) {
                if (Auth) {
                    button2.setText("STOP");
                    start();
                    button.setEnabled(false);
                    button4.setEnabled(false);
                } else {
                    button2.setText("START");
                    stop();
                    button.setEnabled(true);
                    button4.setEnabled(true);
                }
            } else {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).drawer.open();
                }
            }
        });


        button4.setOnClickListener(v -> resetAll());
        button3.setOnClickListener(v -> takeLap());
    }

    // ================================================================
    // DEĞİŞEN METOT: showNoteDialog
    // ================================================================
    private void showLapDeleteRowDialog(int position) {
        if (getActivity() == null) return;
 Lap selectedLap = ListElementsArrayList.get(position);

        int lapNumberToPass = selectedLap.lapsayisi;
  CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForLapDelete(position, lapNumberToPass);
        dialog.show(requireActivity().getSupportFragmentManager(), "DELETE_LAP_DIALOG_TAG");
    }

    private void showNoteDialog(int position) {
        if (getActivity() == null) return;
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawer.setAlpha(0.8f); // Drawer'ı soluklaştır
        }
        // RecyclerView listesinden doğru Lap objesini al
        // (ListElementsArrayList'in ters sıralı olduğunu varsayarak)
        Lap lapToEdit = ListElementsArrayList.get(position);
        int lapNumber = lapToEdit.lapsayisi;
        String currentNote = lapToEdit.message;

        // Eski AlertDialog.Builder kodunu silin ve bunu kullanın:
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForNote(
                position,    // RecyclerView'daki pozisyon
                lapNumber,   // Lap'ın gerçek numarası (örn: Lap 5)
                currentNote  // Mevcut not (varsa)
        );


        // Diyaloğu MainActivity'nin FragmentManager'ı üzerinden göster
        dialog.show(requireActivity().getSupportFragmentManager(), "ADD_NOTE_DIALOG_TAG");
    }
// ================================================================
    // YENİ METOT: updateNoteForLap (MainActivity tarafından çağrılır)
    // ================================================================

    /**
     * CustomAlertDialogFragment'tan gelen veriyi işler ve notu günceller.
     *
     * @param position    RecyclerView'daki öğenin pozisyonu.
     * @param newNoteText Kullanıcının girdiği yeni not.
     */
    public void updateNoteForLap(int position, String newNoteText) {
        if (ListElementsArrayList != null && ListElementsArrayList.size() > position) {

            // 1. Ana UI listesini güncelle (ListElementsArrayList)
            Lap lapInUI = ListElementsArrayList.get(position);
            lapInUI.message = newNoteText;

            // 2. Ana veri kaynağı listesini güncelle (lapsArray)
            // lapsArray'in nasıl sıralandığına bağlı olarak doğru indeksi bulmalıyız.
            // Eski kodunuz (lapsayisi - position - 1) kullanıyordu,
            // ama bu ListElementsArrayList'e bağlıydı.
            // En güvenli yol, Lap numarasını kullanarak asıl objeyi bulmaktır.
            int lapNumberToFind = lapInUI.lapsayisi;

            for (Lap originalLap : lapsArray) {
                if (originalLap.lapsayisi == lapNumberToFind) {
                    originalLap.message = newNoteText;
                    break;
                }
            }

            // 3. RecyclerView'ı uyar
            if (lapListAdapter != null) {
                lapListAdapter.notifyItemChanged(position);
            }

            //Toast.makeText(getContext(), "Note for Lap " + lapNumberToFind + " saved.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void takeLap() {
        if (getActivity() == null) return;
        // 1. Servisten gelen güncel zamanı al
        long currentTimeMillis = lastKnownElapsedTime;
        currentDecimalFormatPattern = getDecimalFormatPattern();
        DecimalFormat newDec = new DecimalFormat(currentDecimalFormatPattern);
        // 2. Zamanı string'e çevir (sadece görüntüleme için)
        String lap = formatTimeAccordingToUnit(lastKnownElapsedTime);
        laps.add(lap);

        double delta;

        if (lapsayisi > 0) {
            // Önceki lap zamanını hesapla (milisaniye cinsinden)
            long previousLapTime = calculatePreviousLapTime();

            // Mevcut ve önceki lap arasındaki farkı unit'e göre hesapla
            long timeDifference = currentTimeMillis - previousLapTime;
            delta = convertMillisToUnit(timeDifference);

        } else {
            // İlk lap - başlangıçtan bu yana geçen süreyi unit'e göre hesapla
            delta = convertMillisToUnit(currentTimeMillis);
        }
//        DecimalFormat dec = new DecimalFormat();
//        dec = new DecimalFormat(currentDecimalFormatPattern);
//        delta = dec.format(delta);
        lapsval.add(delta);
        updateStatistics();

        m_Text = "";
        lapValue = new Lap(newDec.format(delta) + " ", lap, lapsayisi + 1, m_Text);
        lapsArray.add(0, lapValue);             // Düzeltildi: Artık bu da başa ekliyor.

        // lapsArray.add(lapsayisi, lapValue);
        System.out.println("LapsArray içindeki Lap : "+ lapsArray.get(lapsayisi).lap +" Lap numarası :" +lapsArray.get(lapsayisi).lapsayisi);
        ListElementsArrayList.add(0, lapValue);

        updateDisplay();
        lapsayisi++;

        if (pageViewModel != null) {
            pageViewModel.setTimeValue((float) (lapsval.get(lapsayisi - 1) * 1));
            pageViewModel.setMaxTimeValue((float) (max));
            pageViewModel.setMinTimeValue((float) (min));
            pageViewModel.setAvgTimeValue((float) (ave));
            pageViewModel.setIndex(lapsayisi);
            pageViewModel.setTimeUnit(unit);
        }

        lapListAdapter.notifyDataSetChanged();
        // takeLap() fonksiyonunun sonuna ekleyin
        // *** YENİ EKLENEN SATIR ***
        // ViewModel'i güncelleyerek ChartFragment'ı tetikle
        pageViewModel.updateLapsForChart(lapsArray);
    }


    /**
     * Önceki lap'ın milisaniye cinsinden zamanını hesaplar
     */
    private long calculatePreviousLapTime() {
        if (lapsayisi <= 0) return 0;

        // Tüm lap'ların toplam zamanını hesapla
        long totalTime = 0;
        for (int i = 0; i < lapsayisi; i++) {
            double lapValue = lapsval.get(i);
            totalTime += convertUnitToMillis(lapValue);
        }

        return totalTime;
    }

    /**
     * Milisaniyeyi seçilen time unit'e çevirir
     */
    private double convertMillisToUnit(long millis) {
        switch (unit) {
            case "Cmin.": // Santidakika (1 dk = 100 cmin, 1 cmin = 600ms)
                return millis / 600.0;

            case "Dmh.": // Desimdakika (1 dk = 10 dmh, 1 dmh = 360ms)
                return millis / 360.0;

            case "Sec.": // Saniye (1 sn = 1000ms)
            default:
                return millis / 1000.0;
        }
    }

    /**
     * Time unit'i milisaniyeye çevirir
     */
    private long convertUnitToMillis(double unitValue) {
        switch (unit) {
            case "Cmin.": // Santidakika (1 cmin = 600ms)
                return (long) (unitValue * 600);

            case "Dmh.": // Desimdakika (1 dmh = 360ms)
                return (long) (unitValue * 360);

            case "Sec.": // Saniye (1 sn = 1000ms)
            default:
                return (long) (unitValue * 1000);
        }
    }

    private void updateStatistics() {
        if (lapsval.isEmpty()) return;

        double sum = 0;
        for (Double val : lapsval) {
            sum += val;
        }

        min = Collections.min(lapsval);
        lapnomin = lapsval.indexOf(min) + 1;
        max = Collections.max(lapsval);
        lapnomax = lapsval.indexOf(max) + 1;
        ave = (sum / lapsval.size());
        pageViewModel.setAvgTimeValue((float)(ave));
    }

    private void updateDisplay() {
        // Değerleri modul ile çarparak doğru birimde göster
        DecimalFormat newDec = new DecimalFormat(currentDecimalFormatPattern);

        maxvalue.setText(newDec.format(max));
        minvalue.setText(newDec.format(min));
        avevalue.setText(newDec.format(ave));
        cycPerMinute.setText(newDec.format(calculateCycPerMinute(ave)));
        cycPerHour.setText(newDec.format(calculateCycPerHour(ave)));
    }

    private double calculateCycPerMinute(double ave) {
        return (1 / ave) * modul;
    }

    private double calculateCycPerHour(double ave) {
        return (60 / ave) * modul;
    }

    public void save(String fileName) {
        if (getActivity() == null) return;

        if (Auth) {
            excelSave.save(getActivity(), unit, laps, lapsval, ave, modul, diffTime,
                    calculateCycPerHour(ave), calculateCycPerMinute(ave), lapsArray, fileName);
        } else {
            Toast.makeText(getContext(), "Chrono is running!", Toast.LENGTH_SHORT).show();
        }
    }

    public void start() {
        if (getActivity() == null) return;

        Log.d("TimerFragment", "Start method called");
        //debugTimeUnitInfo(); // Debug info

        Auth = false;
        button2.setText("STOP");
        button3.setEnabled(true);
        button.setEnabled(false);
        button4.setEnabled(false);

        startForegroundService();
        getStartTime();
        //startLocalTimer();
    }


    private void getStartTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStart = sdf.format(new Date());
        timeStart = new Date();
        if (currentDateandTimeStart == null || currentDateandTimeStart.trim().isEmpty()) {
            Log.e("TimerFragment", "currentDateandTimeStart is null or empty — stopTime skipped.");
            totalObservationTime.setText("00:00:00");
            return;
        }
        //StartTime = SystemClock.uptimeMillis();
    }

    public void stop() {
        Auth = true;

        // Buton durumlarını güncelle
        button2.setText("START");
        button3.setEnabled(false);
        button.setEnabled(true);
        button4.setEnabled(true);

        // Servisi durdur
        stopForegroundService();

        // Lokal timer'ı durdur
        // stopLocalTimer();
        getStopTime();
    }

    private void getStopTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        currentDateandTimeStop = sdf.format(new Date());
        //StopTime = SystemClock.uptimeMillis();
        // Null kontrolü: start zamanı hiç set edilmediyse hata vermesin
        if (currentDateandTimeStart == null || currentDateandTimeStart.trim().isEmpty()) {
            Log.e("TimerFragment", "currentDateandTimeStart is null or empty — stopTime skipped.");
            totalObservationTime.setText("00:00:00");
            return;
        }
        try {
            Date date1 = sdf.parse(currentDateandTimeStart);
            Date date2 = sdf.parse(currentDateandTimeStop);

            if (date1 != null && date2 != null) {
                long diff = date2.getTime() - date1.getTime();
                int timeInSeconds = (int) (diff / 1000);
                int hours = timeInSeconds / 3600;
                timeInSeconds = timeInSeconds - (hours * 3600);
                int minutes = timeInSeconds / 60;
                timeInSeconds = timeInSeconds - (minutes * 60);
                int seconds = timeInSeconds;

                diffTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                totalObservationTime.setText(diffTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void reset() {
        // Hem lokal timer'ı hem servisi resetle
        // stopLocalTimer();
        stopForegroundService();

        Auth = true;
        button4.setEnabled(false);
        button2.setText("START");

        // Tüm değişkenleri sıfırla
        m = 0;
        h = 0;
        number = 0;
        // Sıfırlanan değişkenleri temizle (handler/runnable ile ilgili olanlar zaten silindi)
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
        lastKnownElapsedTime = 0L; // Yeni eklenen değişkeni sıfırla

        // UI'ı sıfırla
        SpannableString ssp = new SpannableString(timer);
        ssp.setSpan(new RelativeSizeSpan(0.5f), 9, ssp.length(), 0);
        _binding.textView.setText(ssp);

        cycPerHour.setText("");
        cycPerMinute.setText("");
        totalObservationTime.setText("");
        button2.setEnabled(true);
        button3.setEnabled(false);
        button.setEnabled(false);
        lapsayisi = 0;
        avevalue.setText(departure);

        // Listeleri temizle
        ListElementsArrayList.clear();
        laps.clear();
        lapsval.clear();
        maxvalue.setText(departure);
        minvalue.setText(departure);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).drawerSwitchCmin.setEnabled(true);
            ((MainActivity) getActivity()).drawerSwitchSec.setEnabled(true);
            ((MainActivity) getActivity()).drawerSwitchDmin.setEnabled(true);
        }

        lapListAdapter.notifyDataSetChanged();
    }


    private void setWidgetsVisibility(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (button2 != null) button2.setVisibility(visibility);
        if (button3 != null) button3.setVisibility(visibility);
        if (button4 != null) button4.setVisibility(visibility);
        if (button != null) button.setVisibility(visibility);

    }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(getContext(), ChronometerService.class);
        serviceIntent.setAction(Constants.ACTION_START);

        // Zaman birimi bilgilerini servise gönder
        serviceIntent.putExtra(Constants.EXTRA_TIME_UNIT, unit);
        serviceIntent.putExtra(Constants.EXTRA_MODUL, modul);
        serviceIntent.putExtra(Constants.EXTRA_MILIS, milis);


        Log.d("TimerFragment", "🔥 Sending to service - Unit: " + unit + ", Modul: " + modul + ", Milis: " + milis);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
            Log.d("ServiceStart", "startForegroundService (Oreo+) çağrıldı."); // YENİ LOG
        } else {
            requireContext().startService(serviceIntent);
            Log.d("ServiceStart", "startService (Pre-Oreo) çağrıldı."); // YENİ LOG
        }
    }

    // Debug metodu
    private void debugTimeUnitInfo() {
        Log.d("TimeUnitDebug", "=== TIME UNIT INFO ===");
        Log.d("TimeUnitDebug", "Unit: " + unit);
        Log.d("TimeUnitDebug", "Modul: " + modul);
        Log.d("TimeUnitDebug", "Milis: " + milis);
        Log.d("TimeUnitDebug", "======================");

        if (getActivity() != null) {
            String info = "Sending to service:\n" +
                    "Unit: " + unit + "\n" +
                    "Modul: " + modul + "\n" +
                    "Milis: " + milis;
            Toast.makeText(getActivity(), info, Toast.LENGTH_LONG).show();
        }
    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(getContext(), ChronometerService.class);
        serviceIntent.setAction(Constants.ACTION_STOP);
        requireContext().startService(serviceIntent);
    }

    // DND Permission methods
    private void checkAndRequestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                showDndPermissionDialog();
            }
        }
    }

    // TimerFragment.java içinde yeni eklemeniz gereken metot
    private String formatTime(long elapsedMillis) {
// Zaman biriminin saniye (Constants.TIME_UNIT_SECONDS) olduğundan emin olun
        // Cmin ve Dmin için farklı formatlar kullanılıyorsa, o mantık korunmalıdır.
        //Timer sayacı uygun birime göre düzenliyor
        if (unit != null) {
            int hours, minutes, seconds, subUnit;
            String result;

            // Fragment'taki 'unit' değişkenini kullanırız.

            switch (unit) {
                case "Cmin.": // Santidakika (1 dk = 100 cmin)
                    long totalCentiminutes = elapsedMillis / 600; // 1 Cmin = 600ms
                    hours = (int) (totalCentiminutes / 6000); // 1 saat = 6000 Cmin
                    minutes = (int) ((totalCentiminutes % 6000) / 100);
                    subUnit = (int) (totalCentiminutes % 100); // Santidakika birimi
                    // Format: HH:MM:CMIN (Bu formatta nokta veya milisaniye ayırıcı kullanmayız)
                    result = String.format("%02d:%02d:%02d", hours, minutes, subUnit);
                    break;

                case "Dmh.": // Desimdakika (1 dk = 10 dmh)
                    long totalDeciminutes = elapsedMillis / 360; // 1 Dmh = 360ms
                    hours = (int) (totalDeciminutes / 10000); // 1 saat = 10000 Dmh
                    minutes = (int) ((totalDeciminutes % 10000) / 100);
                    subUnit = (int) (totalDeciminutes % 100); // Desimdakika'nın alt birimi
                    // Format: HH:DMH:CM
                    result = String.format("%02d:%02d:%02d", hours, minutes, subUnit);
                    break;

                case "Sec.": // Saniye (Standart)
                default:
                    hours = (int) (elapsedMillis / 3600000);
                    minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                    seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
                    subUnit = (int) (elapsedMillis % 1000); // Milisaniye
                    // Format: HH:MM:SS.MMM
                    result = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, subUnit);
                    break;
            }
            return result;
        } else {
            return "00:00:00.000";
        }
    }

    private void showDndPermissionDialog() {
        if (getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Do Not Distrub Notification Access");
        builder.setMessage("On the lock screen, you need to grant access to the \"Do Not Disturb\" settings to display the stopwatch.");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setPositiveButton("Go to Settings", (dialog, which) -> requestDndPermission());
        }
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(requireContext(), "Lock screen is out of service", Toast.LENGTH_SHORT).show();
        });

        builder.setCancelable(false);
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestDndPermission() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, Constants.REQUEST_DND_ACCESS);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Go manually to the Settings", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_DND_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
                    Toast.makeText(requireContext(), "Permission granted! The lock screen feature is active", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Permission denied. The lock screen feature is disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // TimerFragment.java'ya bu metodları ekleyin
    public void debugNotificationSettings() {
        if (getActivity() == null) return;

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== NOTIFICATION DEBUG INFO ===\n\n");

        if (notificationManager != null) {
            // Notification channel bilgisi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = notificationManager.getNotificationChannel(Constants.CHANNEL_ID);
                if (channel != null) {
                    debugInfo.append("Channel: ").append(channel.getId()).append("\n");
                    debugInfo.append("Name: ").append(channel.getName()).append("\n");
                    debugInfo.append("Importance: ").append(channel.getImportance()).append("\n");
                    debugInfo.append("Lockscreen Visibility: ").append(channel.getLockscreenVisibility()).append("\n");
                    debugInfo.append("Can Bypass DND: ").append(channel.canBypassDnd()).append("\n");
                    debugInfo.append("Can Show Badge: ").append(channel.canShowBadge()).append("\n");
                } else {
                    debugInfo.append("Channel NOT FOUND: ").append(Constants.CHANNEL_ID).append("\n");
                }
            }

            // Notification policy erişimi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                debugInfo.append("DND Access: ").append(notificationManager.isNotificationPolicyAccessGranted()).append("\n");
            }

            // Notificationlar etkin mi
            debugInfo.append("Notifications Enabled: ").append(notificationManager.areNotificationsEnabled()).append("\n");
        } else {
            debugInfo.append("NotificationManager is NULL\n");
        }

        debugInfo.append("\n=== DEVICE INFO ===\n");
        debugInfo.append("Android Version: ").append(Build.VERSION.SDK_INT).append("\n");
        debugInfo.append("Model: ").append(Build.MODEL).append("\n");
        debugInfo.append("Brand: ").append(Build.BRAND).append("\n");

        Log.d("NotificationDebug", debugInfo.toString());

        // Debug bilgisini Toast olarak göster
        if (getActivity() != null) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Notification Debug Info")
                    .setMessage(debugInfo.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // TimerFragment.java'de servisten gelen durum değişikliklerini dinle
    // TimerFragment.java (serviceStateReceiver metodu)
    private BroadcastReceiver serviceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Doğru eylem sabitini kullandığınızdan emin olun
            if (Constants.ACTION_TIME_UPDATE.equals(intent.getAction())) {
                // Constants.java'daki anahtar ile long değeri alın
                long elapsed = intent.getLongExtra(Constants.EXTRA_ELAPSED_TIME, 0);

                // --- KRİTİK EKSİK KISIM BAŞLANGICI ---
                // 1. Gelen zamanı formatlayın (Bu metot TimerFragment'ta zaten olmalı)
                String timeString = formatTime(elapsed);

                // 2. ViewModel'i güncelleyerek UI'daki TextView'i tetikleyin
                if (pageViewModel != null) {
                    pageViewModel.setTimerValue(timeString);
                }
                // --- KRİTİK EKSİK KISIM BİTİŞİ ---

                // UI güncelleme işlemleri
                updateButtonStates();
                // Bu onPause/onResume çağrıları muhtemelen hatalı; kaldırılmalıdır.
                // Sadece buton durumunu güncelleyin.
            /*
            if (isServicePaused){
                onPause();
            }else{
                onResume();
            }
            */
            }
        }
    };


    // Buton metnini güncelle
    private void updateButtonStates() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isServicePaused) {
                        button2.setText("RESUME");
                        button3.setEnabled(false); // Lap butonunu devre dışı bırak
                    } else {
                        button2.setText("STOP");
                        button3.setEnabled(true); // Lap butonunu aktif et
                    }
                }
            });

        }
    }

    // YENİ METOT: Servisi duraklatmak için kullanılır
    public void pause() {
        Auth = true; // Durumu "çalışmıyor" (duraklatıldı) olarak ayarla
        if (currentDateandTimeStart == null) {
            Log.w("TimerFragment", "Pause skipped: start time is null");
            return;
        }
        // Servise duraklatma eylemini bildirmek için LocalBroadcast gönder
        Intent pauseIntent = new Intent(Constants.ACTION_PAUSE);
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(pauseIntent);

        // Gözlem süresini hesaplamak için (mevcut kodda vardı)
        getStopTime();
    }

    // YENİ METOT: Servisi devam ettirmek için kullanılır
    public void resume() {
        Auth = false; // Durumu "çalışıyor" olarak ayarla

        // Servise devam etme eylemini bildirmek için LocalBroadcast gönder
        Intent resumeIntent = new Intent(Constants.ACTION_RESUME);
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(resumeIntent);

        // Not: getStartTime() burada çağrılmaz, çünkü gözlem süresi
        // ilk başta başladı. Servis kendi zamanını tutuyor.
    }

    // Fragment arka plana gittiğinde (başka bir aktiviteye geçiş veya HOME tuşu)
    @Override
    public void onPause() {
        super.onPause();
// Gecikmeli gönderimi iptal et
        uiHandler.removeCallbacks(this::requestCurrentStatus);

        if (isAdded()) {
            // 1. Alıcıları KAYITTAN SİL (Çok önemli, her zaman burada yapılmalı)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timeUpdateReceiver);
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(statusResponseReceiver);
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(adsRemovedReceiver);
        } //cut zaman güncelleme alıcınızın da burada kayıttan silindiğinden emin olun!
    }

    public String getDecimalFormatPattern() {
        // SharedPreferences'ı oku
        // Fragment'ın bir Context'e bağlı olup olmadığını kontrol et
        if (!isAdded()) {
            // Bağlı değilse varsayılan değeri döndür ve devam et
            return "#0.0";
        }
        // Eğer bağlıysa, requireContext()'i güvenle kullan
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Constants'tan tanımladığımız anahtar ve varsayılan değeri kullan
        int decimalCount = prefs.getInt(Constants.PREF_DECIMAL_PLACES, Constants.DEFAULT_DECIMAL_PLACES);
        // DecimalFormat desenini oluştur
        Log.d("TimerFragment", "Shared Prefdeki değer " + decimalCount);
        switch (decimalCount) {
            case 0:
                return "#0";   // Slider 0: 0 ondalık
            case 1:
                return "#0.0";  // Slider 1: 1 ondalık
            case 2:
                return "#0.00"; // Slider 2: 2 ondalık
            case 3:
                return "#0.000"; // Slider 3: 3 ondalık
            default:
                return "#0.0"; // Güvenlik
        }
    }

    public void deleteLap(int position, int lapNumber) {
        // Geçersiz bir pozisyon için işlem yapmayı engelle
        if (ListElementsArrayList == null || position < 0 || position >= ListElementsArrayList.size()) {
            Log.e("DeleteLap", "Geçersiz pozisyon: " + position);
            return;
        }

        // 1. Öğeyi tüm veri kaynaklarından SADECE 'position' kullanarak sil
        Log.d("DeleteLap", "Pozisyon " + position + "'daki tur siliniyor.");
        ListElementsArrayList.remove(position);
        lapsArray.remove(position);
        laps.remove(position);
        Collections.reverse(lapsval);
        lapsval.remove(position);
        Collections.reverse(lapsval);


        // 2. Toplam tur sayısını (lapsayisi) ListElementArraylist in boyut kadar olacak yeni durumda
        lapsayisi = ListElementsArrayList.size();

        // 3. Silme işleminden sonra RecyclerView'daki tur numaralarını yeniden düzenle
        reorderLapNumbers();

        // 4. İstatistikleri (min, max, ortalama) yeniden hesapla ve UI'ı güncelle
        updateStatisticsAndDisplay();

        // 5. RecyclerView'a değişiklikleri bildir
        // notifyDataSetChanged() burada en güvenli yöntemdir çünkü hem silme
        // hem de kalan tüm öğelerin içeriği (tur numaraları) değişti.
        if (lapListAdapter != null) {
            lapListAdapter.notifyDataSetChanged();
        }
        // *** YENİ EKLENEN SATIR ***
        // ViewModel'i güncelleyerek ChartFragment'ı tetikle
        pageViewModel.updateLapsForChart(lapsArray);
    }
        /**
         * Tur silindikten sonra kalan turların numaralarını (1'den başlayarak) yeniden düzenler.
         * RecyclerView'daki listenin ters sıralı olduğunu varsayar (yeni turlar üstte).
         */
        private void reorderLapNumbers() {
            // ListElementsArrayList, RecyclerView'da görünen ve ters sıralı olan listedir.
            // Listenin boyutu, kalan toplam tur sayısıdır (yeni lapsayisi).
            int currentLapCount = lapsayisi;
            for (int i = 0; i < ListElementsArrayList.size(); i++) {
                // En üstteki öğe (i=0) en son turdur ve en yüksek numaraya sahip olmalıdır.
                Lap lapItem = ListElementsArrayList.get(i);
                lapItem.lapsayisi = currentLapCount - i;

                // lapsArray'in de aynı sırada olduğunu varsayarak onu da güncelleyelim.
                // Eğer sıraları farklıysa, burada daha karmaşık bir eşleştirme gerekir.
                // Kodunuza göre sıraları aynı.
                if (i < lapsArray.size()) {
                    lapsArray.get(i).lapsayisi = currentLapCount - i;
                }
            }
        }

/**
 * İstatistikleri yeniden hesaplar ve UI'daki (min, max, avg) değerleri günceller.
 * updateStatistics() ve updateDisplay() metodlarını birleştirir.
 */
        private void updateStatisticsAndDisplay() {
            if (lapsval.isEmpty()) {
                // Hiç tur kalmadıysa, ekranı sıfırla
                max = 0; min = 0; ave = 0;
                lapnomin = 0; lapnomax = 0;

                maxvalue.setText(departure); // 'departure' sizin varsayılan string'iniz
                minvalue.setText(departure);
                avevalue.setText(departure);
                cycPerMinute.setText("");
                cycPerHour.setText("");
                return;
            }

            // İstatistikleri yeniden hesapla
            updateStatistics(); // Bu sizin mevcut metodunuz, min/max/ave değişkenlerini doldurur

            // Ekranı yeni istatistiklerle güncelle
            updateDisplay(); // Bu da sizin mevcut metodunuz, TextView'leri doldurur
        }
// TimerFragment.java'da Reset Fonksiyonu (Örneğin, button4 için)

    public void resetAll() {
        // 1. Durdurma Servisini Durdur
        reset(); // Bu metot zaten mevcut olmalı

        // 2. Tüm Veri Yapılarını Temizle (KRİTİK DÜZELTME)
        lapsArray.clear();
        ListElementsArrayList.clear();
        lapsval.clear();
        laps.clear(); // Varsayılan String listesini de temizle

        // Sayıcıları ve Durum Değişkenlerini Sıfırla
        lapsayisi = 0;
        lapnomax = 0;
        lapnomin = 0;
        lastKnownElapsedTime = 0L;
        Auth = true; // Durum: Durdu (Çalışmıyor)

        // 3. RecyclerView Adaptörünü Güncelle (KRİTİK DÜZELTME)
        if (lapListAdapter != null) {
            lapListAdapter.notifyDataSetChanged();
        }

        // 4. İstatistikleri ve UI'ı Güncelle (Min, Max, Avg, Toplam Gözlem Süresi)
        updateStatisticsAndDisplay(); // Bu metot boş listeyi kontrol edip '---' göstermeli

        // 5. Ana Zamanı ve Butonları Sıfırla
        resetTimerUI(); // pageViewModel.setTimerValue("00:00:00.000") çağrılmalı
        updateButtonStates(); // Butonları "START" durumuna geri döndür

        // 6. PageViewModel'i Temizle (GRAFİK İÇİN KRİTİK DÜZELTME)
        // ChartFragment'ın grafiği temizlemesini sağlamak için ViewModel'e boş bir liste gönderin.
        if (pageViewModel != null) {
            // ViewModel'in içindeki lap listesini boş bir liste ile güncelle.
            pageViewModel.updateLapsForChart(new ArrayList<>());
        }
    }
    // 🔥 YENİ: Kayıtlı zaman birimi ayarını yükler
    private void loadTimeUnitPreference() {
        Context context = getContext();

        if (context == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Kayıtlı birim yoksa varsayılanı kullan
        String savedUnit = prefs.getString(Constants.PREF_TIME_UNIT, Constants.DEFAULT_TIME_UNIT);
        Log.d("TimerFragment","loadTimeUnitPreference çalıştı. Unit --> "+ savedUnit);
        // Update the member variable
        this.unit = savedUnit;
        // ViewModel'i ve dolayısıyla Fragment'ın durumunu güncelle
        if (pageViewModel != null) {
            pageViewModel.setmTimeUnit(savedUnit);
        }
        // 🔥 KRITIK: Update UI immediately if binding is available
        if (_binding != null && _binding.unitValue != null) {
                _binding.unitValue.setText(savedUnit);
        }

        // Also update currentBinding for consistency
        currentBinding = _binding;


    }

    // 🔥 YENİ: BroadcastReceiver'ı zaman birimi güncellemeleri için kaydet
    private void registerTimeUnitUpdateReceiver() {
        if (getContext() == null) return;
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(timeUnitUpdateReceiver,
                new IntentFilter(Constants.ACTION_TIME_UNIT_UPDATE));
    }
    // 🔥 YENİ: Zaman birimi değişikliğini yakalayan alıcı
    private final BroadcastReceiver timeUnitUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_TIME_UNIT_UPDATE.equals(intent.getAction())) {
                String newUnit = intent.getStringExtra(Constants.EXTRA_TIME_UNIT);

                // Handle null case
                if (newUnit == null || newUnit.trim().isEmpty()) {
                    newUnit = "No Unit";
                }
                if (pageViewModel != null) {
                    // 1. ViewModel'i yeni birimle güncelle
                    pageViewModel.setmTimeUnit(newUnit);
                    Log.d("TimerFragment", "Ölçü birimi TimerFragment'a geldi: " + newUnit);
                }

                // 2. Unit değişkenini güncelle
                unit = newUnit;
// 3. 🔥 KRİTİK: unitValue TextView'ı yüklenen değerle güncelle (UI Katmanı)
                if (_binding != null && _binding.unitValue != null) {
                    _binding.unitValue.setText(newUnit);
                }

                // Also update currentBinding for consistency
                if (currentBinding != null && currentBinding.unitValue != null) {
                    currentBinding.unitValue.setText(newUnit);
                }

                // 4. UI'ı anında güncelle
                updateTimeDisplay(lastKnownElapsedTime);
            }
        }
    };
}
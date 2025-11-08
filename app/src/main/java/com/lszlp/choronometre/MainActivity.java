package com.lszlp.choronometre;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import com.google.android.gms.ads.AdView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.tasks.*;
import com.lszlp.choronometre.databinding.ActivityMainBinding;
import com.lszlp.choronometre.main.PageViewModel;
import com.lszlp.choronometre.main.SectionsPagerAdapter;
import com.google.android.material.slider.Slider; // Bu importu eklediğinizden emin olun!
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
//rate app teset internal



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CustomAlertDialogFragment.CustomDialogListener {

    private boolean keepSplashOnScreen = true;
    private enum ChronoState { STOPPED, RUNNING, PAUSED }
    private ChronoState currentState = ChronoState.STOPPED;
    public DrawerLayout drawer;
    public Boolean isResetDone;
    private ReviewInfo reviewInfo;
    private ReviewManager manager;
    NavigationView navigationView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    androidx.appcompat.widget.SwitchCompat drawerSwitchSec;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    androidx.appcompat.widget.SwitchCompat drawerSwitchCmin;
    androidx.appcompat.widget.SwitchCompat drawerSwitchDmin;
    androidx.appcompat.widget.SwitchCompat screenSaverSwitch;
   Slider drawerSlider;
    ViewPager viewPager;
    private AdView adView ;
    Button startButton;
    Button lapButton;
    Button resetButton;
    Button saveButton;
    private static final String TAG = "MainActivity";
    boolean auth;// lap için onay verilmesi lazım  auth = true ise çalışıyor demek
    private ActionBarDrawerToggle toggle;
    PageViewModel pageViewModel;

    private final int[] TAB_ICONS = {
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_stacked_line_chart_24,
            R.drawable.ic_baseline_save
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
// KRİTİK: Modern Uçtan Uca Desteği Etkinleştirme

        //WindowCompat.enableEdgeToEdge(getWindow());
        super.onCreate(savedInstanceState);

        // 2. KRİTİK: setContentView() burada olmalıdır.
        setContentView(R.layout.activity_main);

        // 3. ActionBar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 4. KRİTİK: Görünüm Hiyerarşisi Oluşturulduktan SONRA Insets'leri işle
        DrawerLayout rootLayout = findViewById(R.id.drawer_layout);
        if (rootLayout != null) {

            ViewGroupCompat.installCompatInsetsDispatch(rootLayout);
            // ... ViewCompat.setOnApplyWindowInsetsListener kodunuz buraya gelmeli ...
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                // ... insets işleme mantığı ...
                WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(v);
                if (windowInsets != null) {
                    androidx.core.graphics.Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    // Butonların kesilmesini önlemek için BOTTOM padding ekleme (Navigasyon Çubuğu yüksekliği)
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                }
                return insets;
            });
        }

        // 5. FLAG_FULLSCREEN Kullanımı
        // WindowCompat kullanıldığı için bu satır gereksiz hale gelebilir.
        // Eğer sadece durumu gizlemek istiyorsanız, WindowInsetsControllerCompat kullanın.
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
// Uygulama açılır açılmaz gerekli tüm izinleri kontrol et ve iste

        checkAndRequestAllPermissions();
        initializeApp();
        // KRİTİK EKLEME: BACK tuşuna basıldığında uygulamayı arka plana at./!\
        // KRİTİK EKLEME: BACK tuşuna basıldığında uygulamayı arka plana at.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Çekmecenin açık olup olmadığını kontrol et
                DrawerLayout drawer = findViewById(R.id.drawer_layout); // Drawer ID'nizi kullanın
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    // Uygulamayı HOME tuşu gibi arka plana atar (Activity'yi öldürmez)
                    moveTaskToBack(true);
                }
            }
        });
        //OnBoarding Kontrolü
        // 1. SharedPreferences'ı başlat
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 2. İlk açılış kontrolü
        boolean isFirstTime = prefs.getBoolean(Constants.PREF_FIRST_TIME_LAUNCH, true);

        if (isFirstTime) {
            // Eğer ilk kez açılıyorsa, Onboarding ekranını göster
            launchOnboardingScreen();
        }
    }
    private void initializeApp() {
        setupAppContent();
    }
    private void setupAppContent() {
        // Ana uygulama içeriğini hazırla

        new ThemeColors(this); // renk değiştirme sınıfı
        com.lszlp.choronometre.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activateReviewInfo();

        //ad mob banner test id :ca-app-pub-3940256099942544/9214589741
        //Ad mod banner ıd : ca-app-pub-2013051048838339/8612047524
        adView = binding.adView;
        // / Initialize the Google Mobile Ads SDK on the main thread.
        MobileAds.initialize(this, initializationStatus -> {
            // SDK is initialized, now load the ad.
            loadBannerAd();
        });
        adView = binding.adView;

        // Check for WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        if (tabs.getTabAt(0) != null) {
            tabs.getTabAt(0).setIcon(TAB_ICONS[0]);
        }
        if (tabs.getTabAt(1) != null) {
            tabs.getTabAt(1).setIcon(TAB_ICONS[1]);
        }
        if (tabs.getTabAt(2) != null) {
            tabs.getTabAt(2).setIcon(TAB_ICONS[2]);
        }

        viewPager.setOffscreenPageLimit(2);


        drawer = binding.drawerLayout;
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        //navigation menu aktivasyon
        navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(toggle);

        navigationView.getMenu().findItem(R.id.timeUnitSec)
                .setActionView(new androidx.appcompat.widget.SwitchCompat(this));
        navigationView.getMenu().findItem(R.id.timeUnitCmin)
                .setActionView(new androidx.appcompat.widget.SwitchCompat(this));
        navigationView.getMenu().findItem(R.id.timeUnitDmin)
                .setActionView(new androidx.appcompat.widget.SwitchCompat(this));
        navigationView.getMenu().findItem(R.id.screenSaver)
                .setActionView(new androidx.appcompat.widget.SwitchCompat(this));


        drawerSwitchSec = ((androidx.appcompat.widget.SwitchCompat) navigationView.getMenu().findItem(R.id.timeUnitSec).getActionView());
        drawerSwitchCmin = ((androidx.appcompat.widget.SwitchCompat) navigationView.getMenu().findItem(R.id.timeUnitCmin).getActionView());
        drawerSwitchDmin = ((androidx.appcompat.widget.SwitchCompat) navigationView.getMenu().findItem(R.id.timeUnitDmin).getActionView());
        screenSaverSwitch = ((androidx.appcompat.widget.SwitchCompat) navigationView.getMenu().findItem(R.id.screenSaver).getActionView());

        MenuItem precisionItem = navigationView.getMenu().findItem(R.id.precision);

        if (precisionItem != null) {
            // 1. Layout'u inflate et (Özel XML görünümünü oluştur)
            // Bu View, Slider'ı ve muhtemelen bir kapsayıcıyı içerir.
            LayoutInflater inflater = LayoutInflater.from(this);
            View sliderContainer = inflater.inflate(R.layout.precisionslider, navigationView, false);

            // 2. Slider'ı ActionView olarak ata
            precisionItem.setActionView(sliderContainer);

            // 3. Inflate edilen View içinden Slider bileşenini ID ile bul
            // Buradaki ID, drawer_slider_action_view.xml içindeki Slider'ın ID'si olmalıdır!
            drawerSlider = sliderContainer.findViewById(R.id.slider);

            if (drawerSlider != null) {
                initializeDrawerSlider(drawerSlider);
//                // Slider artık XML'deki görünümle oluştu ve sınıf değişkenine atandı.
//                // Başlatma ve dinleyici atama işlemleri
//                drawerSlider.addOnChangeListener(new Slider.OnChangeListener() {
//
//
//                    @Override
//                    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
//                        Log.d(TAG, "Slider value: " + value);
//
//                    }
//                });
            }
            else {
                Log.e(TAG, "HATA: Inflate edilen View içinde Slider bulunamadı!");
            }
        }
        //Mark:--> SWİÇLER
        drawerSwitchSec.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchSec.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        drawerSwitchCmin.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchCmin.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        drawerSwitchDmin.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchDmin.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        screenSaverSwitch.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        screenSaverSwitch.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));

       // menu item'a ulaşmak için menuıtem olarak çağırmalısın
        MenuItem scren = navigationView.getMenu().findItem(R.id.screenSaver);
        screenSaverSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı açık tutma
                scren.setTitle(getString(R.string.screenOff));


            }else{
                scren.setTitle(getString(R.string.screenOn));
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı kapatma
            }
        });

        drawerSwitchSec.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                drawerSwitchSec.setEnabled(false);
                drawerSwitchCmin.setChecked(false);
                drawerSwitchDmin.setChecked(false);
                if (viewPager.getAdapter() != null) {
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 60;
                    fragment.milis = 1000;

                    fragment.unit = "Sec.";
                    fragment.Timeunit = "Sec. - Second ";
                    if (fragment != null) {
                        // Fragment'ın unit değişkenini alıp, yeni metoda parametre olarak gönderin.
                        // unit değişkeni, TimerFragment'ta hala public veya erişilebilir olmalıdır.
                        fragment.setUnitDisplay(fragment.unit);
                    }



                }
                drawer.close();
            } else {
                drawerSwitchSec.setEnabled(true);

            }
        });
        drawerSwitchCmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                drawerSwitchCmin.setEnabled(false);
                drawerSwitchSec.setChecked(false);
                drawerSwitchDmin.setChecked(false);
                if (viewPager.getAdapter() != null) {
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 100;
                    fragment.milis = 600;

                    fragment.unit = "Cmin.";
                    fragment.Timeunit = "Cmin. - Hundredth of Minute ";
                    if (fragment != null) {
                        // Fragment'ın unit değişkenini alıp, yeni metoda parametre olarak gönderin.
                        // unit değişkeni, TimerFragment'ta hala public veya erişilebilir olmalıdır.
                        fragment.setUnitDisplay(fragment.unit);
                    }
                }
                drawer.close();
            } else {
                drawerSwitchCmin.setEnabled(true);
            }
        });
        drawerSwitchDmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                drawerSwitchDmin.setEnabled(false);
                drawerSwitchCmin.setChecked(false);
                drawerSwitchSec.setChecked(false);
                if (viewPager.getAdapter() != null) {
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 166;// çalışıyor
                    fragment.milis = 360;

                    fragment.unit = "Dmh.";
                    fragment.Timeunit = "Dmh. - 10Thounsandth of Minute ";
                    if (fragment != null) {
                        // Fragment'ın unit değişkenini alıp, yeni metoda parametre olarak gönderin.
                        // unit değişkeni, TimerFragment'ta hala public veya erişilebilir olmalıdır.
                        fragment.setUnitDisplay(fragment.unit);
                    }
                }
                drawer.close();
            } else {
                drawerSwitchDmin.setEnabled(true);
            }
        });

        /**** navigation sonu ***/

        startButton = binding.button2;
        lapButton = binding.button3;
        lapButton.setEnabled(false);
        resetButton = binding.button4;
        resetButton.setEnabled(false);
        saveButton = binding.button;
        saveButton.setEnabled(false);

        saveButton.setOnClickListener(view -> {
            if (viewPager.getAdapter() != null) {
                showSaveDialog();
            }
            /*
            if (viewPager.getAdapter() != null) {

            }*/
        });
        startButton.setOnClickListener(view -> {
            // fragmenti initial et.
            // sadece 0nci siradaki fragmeni çalıştır
            if (viewPager.getAdapter() != null) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                isResetDone = false; // Bu değişkenin kullanımda olup olmadığını kontrol edin, kaldırılabilir.

                switch (currentState) {
                    case STOPPED:
                        // Bu ilk "START" tıklamasıdır
                        if (fragment.modul == 0) {
                            drawer.open();
                            Toast.makeText(getApplicationContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
                        } else {
                            // --- DURUM DEĞİŞİMİ: STOPPED -> RUNNING ---
                            currentState = ChronoState.RUNNING;
                            auth = true; // 'auth' u diğer özellikler (örn. ses tuşu) için koru

                            // İSTEK: Switch'leri devre dışı bırak
                            drawerSwitchCmin.setEnabled(false);
                            drawerSwitchSec.setEnabled(false);
                            drawerSwitchDmin.setEnabled(false);
                            navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                            navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);
                            navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(false);
                            drawerSlider.setEnabled(false);

                            fragment.start(); // Servisi başlatır

                            // İSTEK: UI Güncellemesi
                            lapButton.setEnabled(true);
                            startButton.setText("PAUSE"); // Metni "PAUSE" yap
                            resetButton.setEnabled(false);
                            saveButton.setEnabled(false);
                        }
                        break;

                    case RUNNING:
                        // Bu "PAUSE" tıklamasıdır
                        // --- DURUM DEĞİŞİMİ: RUNNING -> PAUSED ---
                        currentState = ChronoState.PAUSED;
                        auth = false; // Durumu "durmuş" olarak ayarla

                        fragment.pause(); // Yeni pause metodunu çağır

                        // İSTEK: UI Güncellemesi
                        startButton.setText("RESUME"); // Metni "RESUME" yap
                        lapButton.setEnabled(false);
                        saveButton.setEnabled(true);
                        resetButton.setEnabled(true);

                        // Switch'ler devre dışı kalmaya devam eder
                        break;

                    case PAUSED:
                        // Bu "RESUME" tıklamasıdır
                        // --- DURUM DEĞİŞİMİ: PAUSED -> RUNNING ---
                        currentState = ChronoState.RUNNING;
                        auth = true; // Durumu "çalışıyor" olarak ayarla

                        fragment.resume(); // Yeni resume metodunu çağır

                        // İSTEK: UI Güncellemesi
                        startButton.setText("PAUSE"); // Metni "PAUSE" yap
                        lapButton.setEnabled(true);
                        saveButton.setEnabled(false);
                        resetButton.setEnabled(false);

                        // Switch'ler devre dışı kalmaya devam eder
                        break;
                }
            }
        });
        lapButton.setOnClickListener(view -> {
            // lap butonuna deneme amaçlı renk değiştirme sınıfı kodu çalıştırma eklendi


            if (viewPager.getAdapter() != null) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                fragment.takeLap();
            }

        });
        resetButton.setOnClickListener(view -> {
            if (viewPager.getAdapter() != null);
            showResetDialog();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        });
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        // Gerekli state'leri kaydet
        if (viewPager != null) {
            outState.putInt("currentTab", viewPager.getCurrentItem());
        }
        // "auth" yerine yeni durumu (state) kaydedin
        outState.putSerializable("currentState", currentState);
        // outState.putBoolean("isRunning", auth); // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // State'leri geri yükle
        if (viewPager != null) {
            int currentTab = savedInstanceState.getInt("currentTab", 0);
            viewPager.setCurrentItem(currentTab);
        }

        // auth = savedInstanceState.getBoolean("isRunning", false); // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN

        // Yeni durumu (state) geri yükle
        currentState = (ChronoState) savedInstanceState.getSerializable("currentState");
        if (currentState == null) {
            currentState = ChronoState.STOPPED;
        }

        // Geri yüklenen duruma göre UI'ı güncelle
        switch (currentState) {
            case STOPPED:
                auth = false;
                startButton.setText(R.string.start_text);
                lapButton.setEnabled(false);
                saveButton.setEnabled(false);
                resetButton.setEnabled(false);
                // Switch'leri etkinleştir
                drawerSwitchCmin.setEnabled(true);
                drawerSwitchSec.setEnabled(true);
                drawerSwitchDmin.setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(true);
                drawerSlider.setEnabled(true);
                break;
            case RUNNING:
                auth = true;
                startButton.setText("PAUSE");
                lapButton.setEnabled(true);
                saveButton.setEnabled(false);
                resetButton.setEnabled(false);
                // Switch'leri devre dışı bırak
                drawerSlider.setEnabled(false);
                drawerSwitchCmin.setEnabled(false);
                drawerSwitchSec.setEnabled(false);
                drawerSwitchDmin.setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(false);
                break;
            case PAUSED:
                auth = false;
                startButton.setText("RESUME");
                lapButton.setEnabled(false);
                saveButton.setEnabled(true);
                resetButton.setEnabled(true);
                // Switch'leri devre dışı bırak
                drawerSlider.setEnabled(false);
                drawerSwitchCmin.setEnabled(false);
                drawerSwitchSec.setEnabled(false);
                drawerSwitchDmin.setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);
                navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(false);
                break;
        }

        // Ekran açık/kapalı durumunu geri yükle
        boolean isScreenOn = savedInstanceState.getBoolean("isScreenOn", false);
        if (isScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            screenSaverSwitch.setChecked(true);
        }
    }




    @Override
    protected void onPause() {
        super.onPause();
        // Uygulama arka plana geçtiğinde servisin çalışmaya devam etmesi için
        // Burada özel bir şey yapmıyoruz, servis zaten foreground'da çalışıyor
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Uygulama ön plana geldiğinde servis durdurulmaz
        // Sadece UI güncellemesi yapılır
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Uygulama tamamen kapatıldığında servisi temizle
        stopService(new Intent(this, ChronometerService.class));
    }



    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Drawer toggle'ı yeniden sync et
        toggle.syncState();
    }



    /**
     * İzin listesini oluşturur ve gerekli olanları ister.
     */
    private void checkAndRequestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // 1. Depolama İzni (API 32 ve altı için)
        // Manifest'te maxSdkVersion=32 olduğu için burayı ona göre ayarlıyoruz.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) { // Android 12L / API 32
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // 2. Bildirim İzni (API 33 ve üstü için)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Diğer İzinler (FOREGROUND_SERVICE, WAKE_LOCK, vb. koruma seviyesi normal olduğu için Runtimeda istenmezler)
        // Internet, FOREGROUND_SERVICE gibi izinler "normal" koruma seviyesindedir ve kurulurken otomatik verilir.
        // Bu yüzden sadece kullanıcı izni gerektirenleri (Depolama ve Bildirim) istiyoruz.

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    Constants.REQUEST_ALL_PERMISSIONS);
        }
        // Eğer tüm izinler verilmişse, herhangi bir şey yapmaya gerek kalmaz.
    }

    /**
     * İzin isteği sonuçlarını ele alır.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.REQUEST_ALL_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                // Herhangi bir izin reddedildiyse kullanıcıyı bilgilendir
                Toast.makeText(this, "Some permission are missing and needed", Toast.LENGTH_LONG).show();

                // Özellikle Bildirim izni reddedildiyse, kullanıcıyı ayarlara yönlendir
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                    showNotificationSettingsDialog();
                }
            }
        }
    }
    /**
     * Bildirim izni reddedildiğinde, kullanıcıyı Ayarlar ekranına yönlendiren dialog gösterir.
     */
    private void showNotificationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Required")
                .setMessage("Notification permission is needed for the chronometer to run in the background and show notifications. You are being redirected to settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }





    /*
     VOLUME tuşlarını start /stop / lap özelliği koyma.
      */
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {

                    if (viewPager.getAdapter() != null) {
                        TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);

                        // --- YENİ STATE MACHINE MANTIĞI ---
                        switch (currentState) {
                            case STOPPED:
                                if (fragment.modul == 0) {
                                    drawer.open();
                                    Toast.makeText(getApplicationContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // STOPPED -> RUNNING
                                    currentState = ChronoState.RUNNING;
                                    auth = true;
                                    // Switch'leri devre dışı bırak
                                    drawerSwitchCmin.setEnabled(false);
                                    drawerSwitchSec.setEnabled(false);
                                    drawerSwitchDmin.setEnabled(false);
                                    navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                                    navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);
                                    navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(false);

                                    fragment.start();

                                    // UI Update
                                    lapButton.setEnabled(true);
                                    startButton.setText("PAUSE");
                                    resetButton.setEnabled(false);
                                    saveButton.setEnabled(false);
                                }
                                break;
                            case RUNNING:
                                // RUNNING -> PAUSED
                                currentState = ChronoState.PAUSED;
                                auth = false;
                                fragment.pause();
                                // UI Update
                                startButton.setText("RESUME");
                                lapButton.setEnabled(false);
                                saveButton.setEnabled(true);
                                resetButton.setEnabled(true);
                                break;
                            case PAUSED:
                                // PAUSED -> RUNNING
                                currentState = ChronoState.RUNNING;
                                auth = true;
                                fragment.resume();
                                // UI Update
                                startButton.setText("PAUSE");
                                lapButton.setEnabled(true);
                                saveButton.setEnabled(false);
                                resetButton.setEnabled(false);
                                break;
                        }
                        // --- ESKİ KODU SİLİN ---
                    }
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // Bu kısım doğru çalışır, 'auth' değişkeni
                // RUNNING durumunda 'true' olarak ayarlandığı için
                if (action == KeyEvent.ACTION_DOWN && auth) {

                    if (viewPager.getAdapter() != null) {
                        TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);

                        fragment.takeLap();
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, WebpagesActivities.class);
            String link = "https://www.agromtek.com/industrialchronometer/about.html";
            intent.putExtra("link", link);
            startActivity(intent);

        } else if (itemId == R.id.nav_policy) {
            Intent intent2 = new Intent(MainActivity.this, WebpagesActivities.class);
            String link2 = "https://www.agromtek.com/indchroprivacypol.html";
            intent2.putExtra("link", link2);
            startActivity(intent2);
            /**
             *
             * Precision Seçimi
             */
        } else if (itemId == R.id.precision) {

//            if (!auth) {
//
//                if (drawerSlider != null) {
//                    int selectedValue = (int) drawerSlider.getValue();
//                    // prefs.edit().putInt(Constants.EXTRA_DECIMAL_PLACES, selectedValue).apply();
//                    Log.d(TAG, "Ondalık Basamak Slider değeri kaydedildi: " + selectedValue);
//                    Log.d(TAG, "Slider value: " + drawerSlider.getValue());
//
//                }
//                if (viewPager.getAdapter() != null) {
//                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
//                    //fragment.save();
//                }


        } else if (itemId == R.id.nav_share) {
            if (!auth) {
                //play store adresini paylaşmak
                String subject = "Try Industrial Chronometer ⏱️";
                String sharedLink = "https://play.google.com/store/apps/details?id=com.lszlp.choronometre";
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharedLink);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                sendIntent.putExtra(Intent.EXTRA_TITLE, "Sending Industrial Chronometer");

                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        } else if (itemId == R.id.rateApp) {

            if (reviewInfo != null) {
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    Toast.makeText(this, "Review completed", Toast.LENGTH_LONG).show();
                    navigationView.getMenu().findItem(R.id.rateApp).setEnabled(false);
                });
            } else {
                Dialog dialog = new Dialog(MainActivity.this);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable
                            (new ColorDrawable
                                    (Color.parseColor
                                            ("#" + Integer.toHexString
                                                    (ContextCompat.getColor
                                                            (this, R.color.colorDisable)))));
                }
                dialog.setContentView(R.layout.dialog);
                dialog.show();

                RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                Button bt_sbmt = dialog.findViewById(R.id.bt_submit);
                ratingBar.setOnRatingBarChangeListener((ratingBar1, v, b) -> {
                });

                bt_sbmt.setOnClickListener(view -> {
                    startReviewFlow();

                    dialog.dismiss();
                });
            }


        }
        drawer.closeDrawers();
        return true;
    }
    void activateReviewInfo(){
        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> managerInfoTask = manager.requestReviewFlow();
        managerInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                reviewInfo = task.getResult();
            } else {
                // There was some problem, log or handle the error code.
                navigationView.getMenu().findItem(R.id.rateApp).setEnabled(false);
                navigationView.getMenu().findItem(R.id.rateApp).setTitle("Rated !");
            }
        });
    }
    void startReviewFlow(){
        if (reviewInfo != null){
            Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
            flow.addOnCompleteListener(task -> {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
                Toast.makeText(this,"Review completed",Toast.LENGTH_LONG).show();
                navigationView.getMenu().findItem(R.id.rateApp).setEnabled(false);
                navigationView.getMenu().findItem(R.id.rateApp).setTitle("Rated!");
            });
        }

    }
    private void loadBannerAd() {
        //ad mob banner test id :ca-app-pub-3940256099942544/9214589741
        //Ad mod banner ıd : ca-app-pub-2013051048838339/8612047524
        if (adView == null) {
            Log.e(TAG, "AdView is null — skipping ad load to prevent crash");
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.e(TAG, "Ad failed to load: " + adError.getMessage());
                Toast.makeText(MainActivity.this, "Ad failed to load", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return to the app after tapping on an ad.
            }
        });
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    private void showResetDialog() {
        // Düzeltildi: newInstance yerine newInstanceForReset kullanıldı
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForReset();
        drawer.setAlpha(0.3f);//setScrimColor(Color.argb(100, 0, 0, 0));

        dialog.show(getSupportFragmentManager(), "RESET_DIALOG_TAG");

    }

    private void showSaveDialog() {
        // Düzeltildi: newInstance yerine newInstanceForSave kullanıldı
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForSave();
        drawer.setAlpha(0.2f);
        dialog.show(getSupportFragmentManager(), "SAVE_DIALOG_TAG");
    }
// LISTENER METOTLARI

    @Override
    public void onResetConfirmed() {
        if (viewPager.getAdapter() != null) {
            // Fragment'ı doğru şekilde al
            TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);

            if (fragment != null && fragment.isAdded()) {
                // Yeni oluşturduğumuz kapsamlı sıfırlama metodunu çağır
                fragment.resetAll();

                // MainActivity'deki durumu da sıfırla
                currentState = ChronoState.STOPPED;


                // TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                // ChartFragment chartFragment = (ChartFragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
                resetButton.setEnabled(false); //dataları sildikten sonra butonu kapat
                startButton.setText(R.string.start_text);
                startButton.setEnabled(true);// start tuşu açılıyor
                lapButton.setEnabled(false);// lap tuşu kapanıyor
                saveButton.setEnabled(false); //save butonu kapat
                // fragment.reset();
                // chartFragment.ClearChart();
                Toast.makeText(this, "All data has been deleted.", Toast.LENGTH_SHORT).show();

                // --- YENİ EKLENEN KISIM ---
                // Durum makinesini sıfırla
                currentState = ChronoState.STOPPED;
                auth = false;
                drawer.setAlpha(1f);
                // İSTEK: Switch'leri tekrar etkinleştir
                drawerSwitchCmin.setEnabled(true);
                drawerSwitchSec.setEnabled(true);
                drawerSwitchDmin.setEnabled(true);
                drawerSlider.setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(true);
                navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(true);
                // --- YENİ EKLENEN KISIM SONU ---
            }
        }
    }
    @Override
    public void onSaveConfirmed(String fileName) {
        TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        // Düzeltildi: fileName değişkeni save() metoduna parametre olarak eklendi
        fragment.save(fileName);


    }

    @Override
    public void onCancelled() {
        drawer.setAlpha(1f);
        //Toast.makeText(this, "İşlem iptal edildi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteConfirmed(int position, String fileName) {

    }

    @Override
    public void onNoteSaved(int position, int lapNumber, String noteText) {
        drawer.setAlpha(1.0f);
        if (viewPager != null && viewPager.getAdapter() != null) {
            // TimerFragment'ı bul ve notu güncellemesi için metodunu çağır
            TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
            if (fragment != null && fragment.isAdded()) {
                fragment.updateNoteForLap(position, noteText);

            }
        }
    }

    @Override
    public void onDeleteLap(int position, int lapNumber) {
        Log.d("ModulCheck", "Long Click: " + position + " Lap Number : "+lapNumber);
        if (viewPager != null && viewPager.getAdapter() != null) {
            TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
            if (fragment != null && fragment.isAdded()) {
                fragment.deleteLap(position, lapNumber);
            }
        }
    }


    private void initializeDrawerSlider(Slider slider) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // 1. Kaydedilmiş değeri yükle (Varsayılan: 0)
        int savedDecimalPlaces = prefs.getInt(Constants.PREF_DECIMAL_PLACES, Constants.DEFAULT_DECIMAL_PLACES);

        // 2. Slider aralığını ayarla (0'dan 2'ye, 1'er artacak)
//        slider.setValueFrom(0f);
////        slider.setValueTo(3f); // 0, 1, 2 değerleri için
////        slider.setStepSize(1f);

        // 3. Kaydedilmiş değeri Slider'a ata
        slider.setValue((float) savedDecimalPlaces);

        // 4. Dinleyiciyi ata (Değer değiştiğinde Shared Preferences'a kaydet)
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider s, float value, boolean fromUser) {
                int selectedValue = (int) value;
   // Yeni değeri SharedPreferences'a kaydet
                prefs.edit().putInt(Constants.PREF_DECIMAL_PLACES, selectedValue).apply();

                Log.d("MainActivity", "Ondalık Basamak Slider değeri kaydedildi: " + selectedValue);

                // KRİTİK: TimerFragment'ın yeni ayarı okumasını sağlamak için
                // Servisi veya Fragment'ı yeniden başlatmak/güncellemek gerekebilir.
                // En basit çözüm, Fragment'ın onResume() veya onStart() metotlarında SharedPref'i okumasıdır.
                // Bu, Adım 3'te yapılacaktır.
                // KRİTİK: Precision ayarı değiştiğinde TimerFragment'a hemen haber ver (YENİ EKLEME)
                Intent precisionUpdateIntent = new Intent(Constants.ACTION_PRECISION_UPDATE);
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(precisionUpdateIntent);

            }
        });
    }

    private void launchOnboardingScreen() {
        // SharedPreferences'a bir daha göstermemek üzere kaydet
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Onboarding i test etmek için aşağıdki false ->true yap
        prefs.edit().putBoolean(Constants.PREF_FIRST_TIME_LAUNCH, false).apply();

        // Onboarding Activity'yi başlat
        Intent intent = new Intent(MainActivity.this, OnBoardingActivity.class);
         startActivity(intent);
        // NOTE: Onboarding Activity bittiğinde MainActivity'nin devam etmesi için burada finish() çağrılmaz.
        // Kullanıcı Onboarding'i tamamladığında, OnboardingActivity'nin kendisi finish() yapmalıdır.
    }
}
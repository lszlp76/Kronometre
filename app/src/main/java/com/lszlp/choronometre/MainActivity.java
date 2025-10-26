package com.lszlp.choronometre;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import com.google.android.gms.tasks.*;
import com.lszlp.choronometre.databinding.ActivityMainBinding;
import com.lszlp.choronometre.main.SectionsPagerAdapter;

//rate app teset internal

/**
 * TODO
 * ekran koruyucu kapatma
 * menu ekranı
 * paylaşım
 * about sayfası
 * <p>
 * <p>
 * salise ekleme
 **/

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CustomAlertDialogFragment.CustomDialogListener {

    private boolean keepSplashOnScreen = true;




    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        // Gerekli state'leri kaydet
        if (viewPager != null) {
            outState.putInt("currentTab", viewPager.getCurrentItem());
        }
        outState.putBoolean("isRunning", auth);

    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // State'leri geri yükle
        if (viewPager != null) {
            int currentTab = savedInstanceState.getInt("currentTab", 0);
            viewPager.setCurrentItem(currentTab);
        }
        auth = savedInstanceState.getBoolean("isRunning", false);
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
        stopService(new Intent(this, ChronometerService_.class));
    }



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
    ViewPager viewPager;
    private AdView adView ;
    Button startButton, lapButton, resetButton, saveButton;
    private static final String TAG = "MainActivity";
    boolean auth;// lap için onay verilmesi lazım  auth = true ise çalışıyor demek
    private ActionBarDrawerToggle toggle;

    private final int[] TAB_ICONS = {
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_stacked_line_chart_24,
            R.drawable.ic_baseline_save
    };


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Drawer toggle'ı yeniden sync et
        toggle.syncState();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Splash screen'i yükle
       // SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        // ActionBar'ı gizle
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Splash screen özelleştirme
//        splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
//            @Override
//            public boolean shouldKeepOnScreen() {
//                return keepSplashOnScreen;
//            }
//        });
        // Uygulama hazır olduğunda splash screen'i kapat
        initializeApp();
    }

    private void initializeApp() {
        setupAppContent();
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                keepSplashOnScreen = false;
//                // Ana uygulama içeriğini yükle
//                setupAppContent();
//            }
//        }, 2000); // 2 saniye
    }


    private void setupAppContent() {
        // Ana uygulama içeriğini hazırla

        new ThemeColors(this); // renk değiştirme sınıfı
        com.lszlp.choronometre.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activateReviewInfo();

        //ad mob banner test id :ca-app-pub-3940256099942544/9214589741
        //Ad mod banner ıd : ca-app-pub-2013051048838339/8612047524

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
//menu item'a ulaşmak için menuıtem olarak çağırmalısın
        MenuItem scren = navigationView.getMenu().findItem(R.id.screenSaver);
        screenSaverSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){

                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı açık tutma
                scren.setTitle(getString(R.string.screenOff));
            }else{
                screenSaverSwitch.setChecked(false);
                scren.setTitle(getString(R.string.screenOn));
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı kapatma
            }
        });
        //Mark:--> SWİÇLER
        drawerSwitchSec.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchSec.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        drawerSwitchCmin.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchCmin.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        drawerSwitchDmin.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        drawerSwitchDmin.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
        screenSaverSwitch.setThumbTintList(getResources().getColorStateList(R.color.switch_thumb_selector, null));
        screenSaverSwitch.setTrackTintList(getResources().getColorStateList(R.color.switch_track_selector, null));
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
                isResetDone = false;
                if (!startButton.getText().equals("STOP")) {
                    if (fragment.modul == 0) {
                        drawer.open();
                        Toast.makeText(getApplicationContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
                    } else {
                        auth = true;
                        drawerSwitchCmin.setEnabled(false);
                        drawerSwitchSec.setEnabled(false);
                        drawerSwitchDmin.setEnabled(false);
                        navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                        navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);
                        navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(false);

                        fragment.start();
                        lapButton.setEnabled(true);
                        startButton.setText(R.string.stop);
                        resetButton.setEnabled(false);
                        saveButton.setEnabled(false);
                    }

                } else {
                    //stop yapılması
                    auth = false;
                    if (isResetDone) {
                        drawerSwitchCmin.setEnabled(true);
                        drawerSwitchSec.setEnabled(true);
                        drawerSwitchDmin.setEnabled(true);

                    }

                    navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(true);
                    navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(true);
                    navigationView.getMenu().findItem(R.id.timeUnitDmin).setEnabled(true);

                    fragment.stop();
                    startButton.setText(R.string.start);
                    lapButton.setEnabled(false);
                    saveButton.setEnabled(true);// save butonu açık
                    resetButton.setEnabled(true);// reset butonu açık
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
        /*resetButton.setOnClickListener(view -> {
            if (viewPager.getAdapter() != null) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                ChartFragment chartFragment = (ChartFragment) viewPager.getAdapter().instantiateItem(viewPager, 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogCustom));
                builder.setTitle("Delete All Datas");
                builder.setMessage("Are you sure to delete all datas ?");
                builder.setNegativeButton("No", (dialogInterface, i) -> System.out.println("No ya basıldı"));
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    resetButton.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
                    startButton.setText(R.string.start);
                    startButton.setEnabled(true);// start tuşu açılıyor
                    lapButton.setEnabled(false);// lap tuşu kapanıyor
                    saveButton.setEnabled(false); //save butonu kapat

                    fragment.reset();
                    chartFragment.ClearChart();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                // Get the positive button and set its text color
                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (positiveButton != null) {
                    positiveButton.setTextColor(Color.parseColor("#FFFFFFFF"));
                    positiveButton.setTextSize(20);

                }
                Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (negativeButton != null) {
                    negativeButton.setTextColor(Color.parseColor("#FFFFFFFF"));
                    negativeButton.setTextSize(20);

                }
            }
        });*/

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

                        if (!startButton.getText().equals("STOP")) {

                            if (fragment.modul == 0) {
                                drawer.open();
                                Toast.makeText(getApplicationContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
                            } else {
                                auth = true;
                                fragment.start();
                                lapButton.setEnabled(true);
                                startButton.setText(R.string.start);
                                resetButton.setEnabled(false);
                                saveButton.setEnabled(false);
                            }

                        } else {
                            //stop yapılması
                            auth = false;
                            fragment.stop();
                            startButton.setText(R.string.start);
                            lapButton.setEnabled(false);
                            saveButton.setEnabled(true);// save butonu açık
                            resetButton.setEnabled(true);// reset butonu açık
                        }
                    }
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
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
        } else if (itemId == R.id.nav_save) {
            if (!auth) {
                if (viewPager.getAdapter() != null) {
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    //fragment.save();
                }

            }
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
        dialog.show(getSupportFragmentManager(), "RESET_DIALOG_TAG");
    }

    private void showSaveDialog() {
        // Düzeltildi: newInstance yerine newInstanceForSave kullanıldı
        CustomAlertDialogFragment dialog = CustomAlertDialogFragment.newInstanceForSave();
        dialog.show(getSupportFragmentManager(), "SAVE_DIALOG_TAG");
    }
// LISTENER METOTLARI

    @Override
    public void onResetConfirmed() {
        TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        ChartFragment chartFragment = (ChartFragment) viewPager.getAdapter().instantiateItem(viewPager, 1);
        resetButton.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
        startButton.setText(R.string.start);
        startButton.setEnabled(true);// start tuşu açılıyor
        lapButton.setEnabled(false);// lap tuşu kapanıyor
        saveButton.setEnabled(false); //save butonu kapat
        fragment.reset();
        chartFragment.ClearChart();
        Toast.makeText(this, "All data has been deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveConfirmed(String fileName) {
        TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
        // Düzeltildi: fileName değişkeni save() metoduna parametre olarak eklendi
        fragment.save(fileName);
    }

    @Override
    public void onCancelled() {
        //Toast.makeText(this, "İşlem iptal edildi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteConfirmed(int position, String fileName) {

    }

    @Override
    public void onNoteSaved(int position, int lapNumber, String noteText) {
        if (viewPager != null && viewPager.getAdapter() != null) {
            // TimerFragment'ı bul ve notu güncellemesi için metodunu çağır
            TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
            if (fragment != null && fragment.isAdded()) {
                fragment.updateNoteForLap(position, noteText);
            }
        }
    }




}

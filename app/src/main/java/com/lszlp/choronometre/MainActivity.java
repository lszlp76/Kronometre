package com.lszlp.choronometre;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.splashscreen.SplashScreen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import com.google.android.gms.tasks.*;
import com.lszlp.choronometre.databinding.ActivityMainBinding;
import com.lszlp.choronometre.main.SectionsPagerAdapter;

import java.io.Console;
import java.util.Random;

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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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



    public DrawerLayout drawer;
    public Boolean isResetDone;
    private ReviewInfo reviewInfo;
    private ReviewManager manager;
    NavigationView navigationView;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch drawerSwitchSec;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch drawerSwitchCmin;
    Switch drawerSwitchDmin;
    Switch screenSaverSwitch;
    ViewPager viewPager;
    Switch switchSec, switchCmin, switchDmin;
    AppBarLayout appBarLayout;
    private AdView adView ;
    Button startButton, lapButton, resetButton, saveButton;
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    boolean auth;// lap için onay verilmesi lazım  auth = true ise çalışıyor demek
    private ActionBarDrawerToggle toggle;

    private Toolbar toolbar;
    private int[] TAB_ICONS = {
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_stacked_line_chart_24,
            R.drawable.ic_baseline_save
    };




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

       // SplashScreen.installSplashScreen(this) ; //splash screen i çağırır

        super.onCreate(savedInstanceState);
        new ThemeColors(this); // renk değiştirme sınıfı




        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activateReviewInfo();

        //ad mob banner test id :ca-app-pub-3940256099942544/9214589741
        //Ad mod banner ıd : ca-app-pub-2013051048838339/8612047524

       // / Initialize the Google Mobile Ads SDK on the main thread.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // SDK is initialized, now load the ad.
                loadBannerAd();
            }
        });

        // MainActivity.java onCreate'de
//        Button btnOpenDebug = findViewById(R.id.btnOpenDebug);
//        btnOpenDebug.setOnClickListener(v -> {
//            Intent debugIntent = new Intent(MainActivity.this, TestNotificationActivity.class);
//            startActivity(debugIntent);
//        });
         adView = binding.adView;


//        new Thread(
//                () -> {
//                    // Initialize the Google Mobile Ads SDK on a background thread.
//                    MobileAds.initialize(this, initializationStatus -> {});
//                })
//                .start();
//
//    adView = binding.adView;
//       AdRequest adRequest = new AdRequest.Builder()
//            .build();
//    adView.loadAd(adRequest);

//Önce kullanıcının yazma izni olup olmadığını kontrol ediyoruz


        // Check for WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(TAB_ICONS[0]);
        tabs.getTabAt(1).setIcon(TAB_ICONS[1]);
        tabs.getTabAt(2).setIcon(TAB_ICONS[2]);

        viewPager.setOffscreenPageLimit(2);
        

        // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.view_pager,TimerFragment.class,null);

       // toolbar.setBackgroundColor(Color.parseColor("#80000000")) ;

        drawer = binding.drawerLayout;// findViewById(R.id.drawer_layout);
        toolbar = binding.toolbar;// findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //navigation menu aktivasyon
        navigationView = binding.navView;// findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
                 toggle.setDrawerIndicatorEnabled(true);
                drawer.addDrawerListener(toggle);
/**
 * drawer open ve close ile açaabilrsin.
 */
        // Now, syncState() should be called in onPostCreate()
        //toggle.syncState(); // Remove this line from onCreate()


        ;
/**
 * NavigationView üzerine swicth bağlamak için aşağıdaki
 * komut kümesini eklemelisin
 */

        navigationView.getMenu().findItem(R.id.timeUnitSec)
                .setActionView(new Switch(this));
        navigationView.getMenu().findItem(R.id.timeUnitCmin)
                .setActionView(new Switch(this));
        navigationView.getMenu().findItem(R.id.timeUnitDmin)
                .setActionView(new Switch(this));
        navigationView.getMenu().findItem(R.id.screenSaver)
                .setActionView(new Switch(this));


        drawerSwitchSec = ((Switch) navigationView.getMenu().findItem(R.id.timeUnitSec).getActionView());
        drawerSwitchCmin = ((Switch) navigationView.getMenu().findItem(R.id.timeUnitCmin).getActionView());
        drawerSwitchDmin = ((Switch) navigationView.getMenu().findItem(R.id.timeUnitDmin).getActionView());
        screenSaverSwitch = ((Switch) navigationView.getMenu().findItem(R.id.screenSaver).getActionView());
//menu item'a ulaşmak için menuıtem olarak çağırmalısın
        MenuItem scren = navigationView.getMenu().findItem(R.id.screenSaver);
        screenSaverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı açık tutma
                    scren.setTitle(getString(R.string.screenOff));
                }else{
                    screenSaverSwitch.setChecked(false);
                    scren.setTitle(getString(R.string.screenOn));
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// ekranı kapatma
                }
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
        drawerSwitchSec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawerSwitchSec.setEnabled(false);
                    drawerSwitchCmin.setChecked(false);
                    drawerSwitchDmin.setChecked(false);
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 60;
                    fragment.milis = 1000;

                    fragment.unit = "Sec.";
                    fragment.Timeunit = "Sec. - Second ";
                    fragment.binding.unitValue.setText(fragment.unit);
                    drawer.close();
                } else {
                   // drawerSwitchCmin.setChecked(true);
                    //drawerSwitchDmin.setChecked(true);
                    drawerSwitchSec.setEnabled(true);

                }
            }
        });
        drawerSwitchCmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawerSwitchCmin.setEnabled(false);
                    drawerSwitchSec.setChecked(false);
                    drawerSwitchDmin.setChecked(false);
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 100;
                    fragment.milis = 600;

                    fragment.unit = "Cmin.";
                    fragment.Timeunit = "Cmin. - Hundredth of Minute ";
                    fragment.binding.unitValue.setText(fragment.unit);
                    drawer.close();
                } else {
                    //drawerSwitchSec.setChecked(true);
                    //drawerSwitchDmin.setChecked(true);
                    drawerSwitchCmin.setEnabled(true);
                }
            }
        });
        drawerSwitchDmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawerSwitchDmin.setEnabled(false);
                    drawerSwitchCmin.setChecked(false);
                    drawerSwitchSec.setChecked(false);
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 166;// çalışıyor
                    fragment.milis = 360;

                    fragment.unit = "Dmh.";
                    fragment.Timeunit = "Dmh. - 10Thounsandth of Minute ";
                    fragment.binding.unitValue.setText(fragment.unit);
                    drawer.close();
                } else {
                   // drawerSwitchCmin.setChecked(true);
                   // drawerSwitchSec.setChecked(true);
                    drawerSwitchDmin.setEnabled(true);
                }
            }
        });

        /**** navigation sonu ***/

        // startButton = findViewById(R.id.button2);
        startButton = binding.button2;
        lapButton = binding.button3;//findViewById(R.id.button3);
        lapButton.setEnabled(false);
        resetButton = binding.button4;//findVi
        // ewById(R.id.button4);
        resetButton.setEnabled(false);
        saveButton = binding.button;//findViewById(R.id.button);
        saveButton.setEnabled(false);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                fragment.save();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ;
                // fragmenti initial et.
                // sadece 0nci siradaki fragmeni çalıştır
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                isResetDone = false;
                if (startButton.getText() != "STOP") {
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
                        startButton.setText("STOP");
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
                    startButton.setText("START");
                    lapButton.setEnabled(false);
                    saveButton.setEnabled(true);// save butonu açık
                    resetButton.setEnabled(true);// reset butonu açık
                }
            }
        });
        lapButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             // lap butonuna deneme amaçlı renk değiştirme sınıfı kodu çalıştırma eklendi
                                             int red= new Random().nextInt(255);
                                             int green= new Random().nextInt(255);
                                             int blue= new Random().nextInt(255);


                                             TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                                             fragment.takeLap();
                                           //  ThemeColors.setNewThemeColor(MainActivity.this, red, green, blue);

                                         }
                                     }
        );
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                ChartFragment chartFragment = (ChartFragment) viewPager.getAdapter().instantiateItem(viewPager, 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogCustom));
                builder.setTitle("Delete All Datas");
                builder.setMessage("Are you sure to delete all datas ?");
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("No ya basıldı");
                    }

                } );
               builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resetButton.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
                        startButton.setText("START");
                        startButton.setEnabled(true);// start tuşu açılıyor
                        lapButton.setEnabled(false);// lap tuşu kapanıyor
                        saveButton.setEnabled(false); //save butonu kapat

                        fragment.reset();
                        chartFragment.ClearChart();
                    }
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
        });
    }
//** ADMOB FUNCS***
private AdSize getAdSize() {
    // Determine the screen width (less decorations) to use for the ad width.
    Display display = getWindowManager().getDefaultDisplay();
    DisplayMetrics outMetrics = new DisplayMetrics();
    display.getMetrics(outMetrics);

    float density = outMetrics.density;

    float adWidthPixels = adView.getWidth();

    // If the ad hasn't been laid out, default to the full screen width.
    if (adWidthPixels == 0) {
        adWidthPixels = outMetrics.widthPixels;
    }

    int adWidth = (int) (adWidthPixels / density);
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
}
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);

                    if (startButton.getText() != "STOP") {

                        if (fragment.modul == 0) {
                            drawer.open();
                            Toast.makeText(getApplicationContext(), "Choose time unit!", Toast.LENGTH_SHORT).show();
                        } else {
                            auth = true;
                            fragment.start();
                            lapButton.setEnabled(true);
                            startButton.setText("STOP");
                            resetButton.setEnabled(false);
                            saveButton.setEnabled(false);
                        }

                    } else {
                        //stop yapılması
                        auth = false;
                        fragment.stop();
                        startButton.setText("START");
                        lapButton.setEnabled(false);
                        saveButton.setEnabled(true);// save butonu açık
                        resetButton.setEnabled(true);// reset butonu açık
                    }
                }

                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN && auth) {

                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);

                    fragment.takeLap();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }

    }


    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case (R.id.nav_about):
                Intent intent = new Intent(MainActivity.this, WebpagesActivities.class);
                String link = "https://www.agromtek.com/industrialchronometer/about.html";
                intent.putExtra("link", link);
                startActivity(intent);

                break;
            case (R.id.nav_policy):
                Intent intent2 = new Intent(MainActivity.this, WebpagesActivities.class);
                String link2 = "https://www.agromtek.com/indchroprivacypol.html";
                intent2.putExtra("link", link2);
                startActivity(intent2);
                break;
            case (R.id.nav_save):
                if (!auth) {
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.save();

                }
                break;
            case (R.id.nav_share):
                if (!auth) {
                    //en son tab sayfasını açmak
                   /* viewPager.setCurrentItem(viewPager.getCurrentItem() + 2, true);
                    */
                    //play store adresini paylaşmak
                    String subject = "Try Industrial Chronometer ⏱️";
                    String sharedLink = "https://play.google.com/store/apps/details?id=com.lszlp.choronometre";
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,  sharedLink);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject );
                    sendIntent.putExtra(Intent.EXTRA_TITLE, "Sending Industrial Chronometer");

                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                }
                break;

            case (R.id.rateApp):
                if (reviewInfo != null){
                    Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                    flow.addOnCompleteListener(task -> {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        Toast.makeText(this,"Review completed",Toast.LENGTH_LONG).show();
                        navigationView.getMenu().findItem(R.id.rateApp).setEnabled(false);
                    });
                }else
                {
                    Dialog dialog = new Dialog(MainActivity.this);

                    dialog.getWindow().setBackgroundDrawable
                            (new ColorDrawable
                                    (Color.parseColor
                                            ("#"+Integer.toHexString
                                                    (ContextCompat.getColor
                                                            (this,R.color.colorDisable)))));
                    dialog.setContentView(R.layout.dialog);
                    dialog.show();

                    RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                    TextView tvRating = dialog.findViewById(R.id.tv_rating);
                    Button bt_sbmt = dialog.findViewById(R.id.bt_submit);
                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                                               @Override
                                                               public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                                                                   //tvRating.setText(String.format("(%s)",v));
                                                               }
                                                           }

                    );

                    bt_sbmt.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            //String sRating = String.valueOf(ratingBar.getRating());
                            //gidecek değer sRaitng
                            startReviewFlow();

                            dialog.dismiss();
                        }
                    });
                    //https://icons8.com/icons/set/toggle-off-on
                }


                break;
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
//Toast.makeText(this,"Review failed to start",Toast.LENGTH_LONG).show();
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
       // String adUnitId = "ca-app-pub-2013051048838339/8612047524"; // Replace with your actual ad unit ID

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
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
    // MainActivity veya TimerFragment'ta
    private void showDndGuide() {
        new AlertDialog.Builder(this)
                .setTitle("Rahatsız Etmeyin İzni Nasıl Verilir?")
                .setMessage("1. 'Ayarlara Git' butonuna tıklayın\n" +
                        "2. 'Gelişmiş' veya 'Diğer ayarlar'a gidin\n" +
                        "3. 'Rahatsız Etmeyin' bölümünü bulun\n" +
                        "4. 'Chronometre' uygulamasına izin verin\n" +
                        "5. Uygulamaya geri dönün")
                .setPositiveButton("Anladım", null)
                .show();
    }
}

//https://www.youtube.com/watch?v=bKJeDD-tP_Y
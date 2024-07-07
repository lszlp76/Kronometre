package com.lszlp.choronometre;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    public DrawerLayout drawer;
    public Boolean isResetDone;
    private ReviewInfo reviewInfo;
    private ReviewManager manager;
    NavigationView navigationView;
    Switch drawerSwitchSec;
    Switch drawerSwitchCmin;
    ViewPager viewPager;
    Switch switchSec, switchCmin;
    AppBarLayout appBarLayout;
    Button startButton, lapButton, resetButton, saveButton;
    boolean auth;// lap için onay verilmesi lazım  auth = true ise çalışıyor demek
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private ActivityMainBinding binding;
    private int[] TAB_ICONS = {
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_stacked_line_chart_24,
            R.drawable.ic_baseline_save
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
       /*
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_main);

        }
        *
        */
        System.out.println("deneme");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activateReviewInfo();
//Önce kullanıcının yazma izni olup olmadığını kontrol ediyoruz


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
        }
        //Typeface tf = getResources().getFont(R.font.digital7);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        toolbar = binding.toolbar;// findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = binding.drawerLayout;// findViewById(R.id.drawer_layout);

        //navigation menu aktivasyon
        navigationView = binding.navView;// findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(toggle);
/**
 * drawer open ve close ile açaabilrsin.
 */

        toggle.syncState();
        ;
/**
 * NavigationView üzerine swicth bağlamak için aşağıdaki
 * komut kümesini eklemelisin
 */
        navigationView.getMenu().findItem(R.id.timeUnitSec)
                .setActionView(new Switch(this));
        navigationView.getMenu().findItem(R.id.timeUnitCmin)
                .setActionView(new Switch(this));

//
       drawerSwitchSec = ((Switch) navigationView.getMenu().findItem(R.id.timeUnitSec).getActionView());
       drawerSwitchCmin = ((Switch) navigationView.getMenu().findItem(R.id.timeUnitCmin).getActionView());

        drawerSwitchSec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawerSwitchCmin.setChecked(false);
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 60;
                    fragment.milis = 1000;

                    fragment.unit = "Sec.";
                    fragment.Timeunit = "Sec. - Second ";
                    fragment.binding.unitValue.setText(fragment.unit);
                    drawer.close();
                } else {
                    drawerSwitchCmin.setChecked(true);

                }
            }
        });
        drawerSwitchCmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    drawerSwitchSec.setChecked(false);
                    TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                    fragment.modul = 100;
                    fragment.milis = 600;

                    fragment.unit = "Cmin.";
                    fragment.Timeunit = "Cmin. - Hundredth of Minute ";
                    fragment.binding.unitValue.setText(fragment.unit);
                    drawer.close();
                } else {
                    drawerSwitchSec.setChecked(true);
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
                    }
                    else {
                        auth = true;
                        drawerSwitchCmin.setEnabled(false);
                        drawerSwitchSec.setEnabled(false);
                        navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(false);
                        navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(false);

                        fragment.start();
                        lapButton.setEnabled(true);
                        startButton.setText("STOP");
                        resetButton.setEnabled(false);
                        saveButton.setEnabled(false);
                    }

                } else  {
                    //stop yapılması
                    auth = false;
                    if (isResetDone){
                        drawerSwitchCmin.setEnabled(true);
                        drawerSwitchSec.setEnabled(true);

                    }

                    navigationView.getMenu().findItem(R.id.timeUnitSec).setEnabled(true);
                    navigationView.getMenu().findItem(R.id.timeUnitCmin).setEnabled(true);

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
                                             TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                                             fragment.takeLap();
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
                });
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


                builder.show();

            }
        });

       /* FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


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
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 2, true);
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
}
//rate app teset internal

//https://www.youtube.com/watch?v=bKJeDD-tP_Y
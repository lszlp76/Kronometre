package com.lszlp.choronometre;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.lszlp.choronometre.databinding.ActivityMainBinding;
import com.lszlp.choronometre.main.SectionsPagerAdapter;

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
    ViewPager viewPager;
    AppBarLayout appBarLayout;
    Button startButton, lapButton, resetButton, saveButton;
    boolean auth;// lap için onay verilmesi lazım  auth = true ise çalışıyor demek
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private ActivityMainBinding binding;
    private int[] TAB_ICONS = {
            R.drawable.ic_baseline_timer_24,
            R.drawable.ic_baseline_stacked_line_chart_24,
            R.drawable.ic_baseline_save
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//Önce kullanıcının yazma izni olup olmadığını kontrol ediyoruz


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
        }
        Typeface tf = getResources().getFont(R.font.digital7);
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
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        //navigation menu aktivasyon
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /*if (savedInstanceState == null){
            viewPager.setCurrentItem(0);
            navigationView.setCheckedItem(R.id.nav_view);

        }*/
        /**** navigation sonu ***/

       // startButton = findViewById(R.id.button2);
        startButton= binding.button2;
        lapButton = findViewById(R.id.button3);
        lapButton.setEnabled(false);
        resetButton = findViewById(R.id.button4);
        resetButton.setEnabled(false);
        saveButton = findViewById(R.id.button);
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

                if (startButton.getText() != "STOP") {
                    if (fragment.modul == 0) {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.nav_about):
                Intent intent = new Intent(MainActivity.this,WebpagesActivities.class);
                String link = "about";
                intent.putExtra("link",link);
                startActivity(intent);

                break;
            case (R.id.nav_policy):
                Intent intent2 = new Intent(MainActivity.this,WebpagesActivities.class);
                String link2 = "https://www.agromtek.com/indchroprivacypol.html";
                intent2.putExtra("link",link2);
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


        }
        drawer.closeDrawers();
        return true;
    }
}
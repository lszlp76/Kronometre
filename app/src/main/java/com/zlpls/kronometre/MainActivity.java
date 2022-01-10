package com.zlpls.kronometre;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.zlpls.kronometre.databinding.ActivityMainBinding;
import com.zlpls.kronometre.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    Button startButton, lapButton,resetButton,saveButton ;
    boolean auth ;// lap için onay verilmesi lazım
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//Önce kullanıcının yazma izni olup olmadığını kontrol ediyoruz
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);


        // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.view_pager,TimerFragment.class,null);

        startButton = findViewById(R.id.button2);
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
                            auth= true;
                            fragment.start();
                            lapButton.setEnabled(true);
                            startButton.setText("STOP");
                            resetButton.setEnabled(false);
                            saveButton.setEnabled(false);
                        }

                    } else {
                        //stop yapılması
                        auth=false;
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
}
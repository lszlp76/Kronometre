package com.zlpls.kronometre;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.zlpls.kronometre.databinding.ActivityMainBinding;
import com.zlpls.kronometre.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

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
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        ;
        // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.view_pager,TimerFragment.class,null);

        Button startButton = findViewById(R.id.button2);
        Button lapButton = findViewById(R.id.button3);
        lapButton.setEnabled(false);
        Button resetButton = findViewById(R.id.button4);
        resetButton.setEnabled(false);
        Button saveButton = findViewById(R.id.button);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("HEnüz yok save");
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimerFragment fragment = (TimerFragment) viewPager.getAdapter().instantiateItem(viewPager, 0);
                ;
                // fragmenti initial et.
                // sadece 0nci siradaki fragmeni çalıştır

                if (startButton.getText() != "STOP") {
                    if (fragment.modul == 0) {
                        Toast.makeText(getApplicationContext(), "Choose your chrono type!", Toast.LENGTH_SHORT).show();
                    } else {
                        fragment.start();
                        lapButton.setEnabled(true);
                        startButton.setText("STOP");
                    }

                } else {
                    //stop yapılması
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
                ChartFragment chartFragment = (ChartFragment)viewPager.getAdapter().instantiateItem(viewPager,1);

                resetButton.setEnabled(false); //dataları sildikten sonra butonu kapat v1.nci releasedeki hatadan dolayı
                startButton.setText("START");
                startButton.setEnabled(true);// start tuşu açılıyor
                lapButton.setEnabled(false);// lap tuşu kapanıyor
                saveButton.setEnabled(false); //save butonu kapat
                fragment.reset();
                chartFragment.ClearChart();
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

    public void deneme() {

    }
}
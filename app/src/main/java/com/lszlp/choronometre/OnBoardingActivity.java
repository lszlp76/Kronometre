// OnboardingActivity.java (Yeni dosya)
package com.lszlp.choronometre;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

// ViewPager ve Tab/Indicator kütüphanelerini projenize eklediğinizden emin olun!

public class OnBoardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Button btnSkip;
    private Button btnNext;

    // Onboarding sayfa dizinleri
    private int[] layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding); // Yeni layout'u şişir

        viewPager = findViewById(R.id.view_pager);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // Tanıtım slaytlarının layout ID'lerini tanımlayın
        layouts = new int[]{
                R.layout.intro_slide_1, // Örn: Başlık ve Açıklama
                R.layout.intro_slide_2, // Örn: Lap Özelliği
                R.layout.intro_slide_3, // Örn: Hassasiyet Ayarı
                R.layout.intro_slide_4 // Örn: sesli not Ayarı
        };

        // ViewPager Adapter'ı ayarla (Aşağıda tanımlanacak)
        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(v -> launchHomeScreen());
        btnNext.setOnClickListener(v -> {
            // Bir sonraki slayta geç
            int current = getItem(+1);
            if (current < layouts.length) {
                viewPager.setCurrentItem(current);
            } else {
                // Son slaytta "Bitti" butonu
                launchHomeScreen();
            }
        });
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        // MainActivity zaten yığında (stack) olduğu için sadece bu Activity'yi kapat
        finish();
    }

    // ViewPager Change Listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            // Sadece son sayfada "Başla" butonu görünecek, diğerlerinde "İleri"
            if (position == layouts.length - 1) {
                btnNext.setText(getString(R.string.got_it)); // Başla veya Anladım
                btnSkip.setVisibility(View.GONE);
            } else {
                btnNext.setText(getString(R.string.next)); // İleri
                btnSkip.setVisibility(View.GONE);
            }
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}
        @Override
        public void onPageScrollStateChanged(int arg0) {}
    };

    // İÇ İÇE (NESTED) Adapter Sınıfı
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
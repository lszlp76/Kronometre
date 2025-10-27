package com.lszlp.choronometre.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup; // Yeni Ekle
import com.lszlp.choronometre.ChartFragment;
import com.lszlp.choronometre.FileList;
import com.lszlp.choronometre.R;
import com.lszlp.choronometre.TimerFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2,R.string.tab_text_3};
    private final Context mContext;
    // 1. Oluşturulan Fragment'ları saklamak için bir dizi/harita kullanın
    private final Fragment[] registeredFragments = new Fragment[3];
    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;

    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return new TimerFragment();

            case 1:
                return new ChartFragment();

            case 2:
                return new FileList();

            default:
                return null;

        }

    }
    // 2. Fragment Manager tarafından oluşturulan View'ı kaydet
    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Üst sınıfın orijinal Fragment'ı oluşturma/geri yükleme işlemini yapmasını sağlayın
        Fragment fragment = (Fragment) super.instantiateItem(container, position);

        // Oluşturulan/Geri yüklenen Fragment'ı kaydedin
        registeredFragments[position] = fragment;
        return fragment;
    }
    // 3. Fragment Manager tarafından silinen View'ı listeden kaldırın
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // Silinen öğenin kaydını kaldırın
        registeredFragments[position] = null;
        super.destroyItem(container, position, object);
    }
    // 4. Fragment'a güvenli erişim için yeni metot (KRİTİK)
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments[position];
    }
    // SectionsPagerAdapter.java'da
    public Fragment getFragment(int position) {
        // Adapter'ın getItem(position) metodundaki fragment döndürme mantığını kullanın
        switch (position) {
            case 0:
                return new TimerFragment(); // VEYA zaten oluşturulmuş bir referansı döndürün
            case 1:
                return new ChartFragment();

            case 2:
                return new FileList();

            default:
                return null;
        }
    }
/*
 @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }
 */



    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}
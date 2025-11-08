package com.lszlp.choronometre;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import java.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date; // <-- Bu satır 'new Date()' hatasını çözer
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.lszlp.choronometre.main.PageViewModel;

import java.util.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;



public class ChartFragment extends Fragment {


    LineChart lineChart;
    ArrayList<Entry> lineEntry, lapValue;
   // TextView chartTimer;
    PageViewModel pageViewModel;
    Typeface tf;
    Float textSize;
    String timeUnit;

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ImageView takePhoto = view.findViewById(R.id.takePhoto);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             saveChartToGallery();
                 }
              }
        );

        return view;

    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        //grafik için değişkenlerin oluşturulması
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            tf = getResources().getFont(R.font.digital7);
//        }
//        lineChart = view.findViewById(R.id.chart);
//        lineChart.setNoDataTextTypeface(tf);
//        lineChart.setNoDataTextColor(R.color.chartColor);
//
//        lineEntry = new ArrayList<>();
//        lapValue = new ArrayList<>();
//      //  chartTimer = view.findViewById(R.id.chartTimer);
//        /*tAvgValue = new ArrayList<>();
//        tMinValue = new ArrayList<>();
//        tMaxValue = new ArrayList<>();*/
//
//           pageViewModel.getTimerValue().observe(requireActivity(), new Observer<String>() {
//                @Override
//                public void onChanged(String s) {
//
//                    ;
//                 // displayFormattedTime(chartTimer,s);
//                   // chartTimer.setText(s);
//                  //  System.out.println("Chart timer value :" + s );
//                }
//            });;
//
//
//
//
//        pageViewModel.getIndex().observe(requireActivity(), new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer integer) {
//                /*
//                ama her seferinde lap tuşuna basınca lineENTRY dizisine eklemesi lazım
//                */
//                ;
//
//                DrawChart();
//            }
//        });
//        // --- YENİ EKLENEN KISIM ---
//        // Tur verisi değiştiğinde (silindiğinde) tetiklenecek gözlemciyi ayarlayın.
//        pageViewModel.getOnLapDataChanged().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean hasChanged) {
//                // Sinyal geldiğinde...
//                if (hasChanged != null && hasChanged) {
//                    Log.d("ChartFragment", "Tur verisi değişti, grafik yeniden çiziliyor.");
//
//                    // Grafiği yeniden çizecek olan metodu burada çağırın.
//                    // Bu metodun, güncel veri listesini (örneğin lapsval) alması gerekir.
//                    // Bu veriyi ViewModel üzerinden veya başka bir paylaşılan kaynaktan alabilirsiniz.
//                    // Örneğin, veriyi doğrudan alan bir metodunuz varsa:
//                  redrawChart();
//                }
//            }
//        });
//
//        //grafik için değişkenlerin oluşturulması
//        //1-Grafik tipini tanıt
//        //lineChart.invalidate();
//        //2- *** X değerleri***/
//        //3 ***Y değerleri ***/
//
//        //4
//        //lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
//
//        //5
//        //6
//
//
//    }
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Grafik bileşenini ve temel ayarları yap
    lineChart = view.findViewById(R.id.chart);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        tf = getResources().getFont(R.font.digital7);
        lineChart.setNoDataTextTypeface(tf);
    }
    lineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.chartColor));
    lineChart.setNoDataText("Henüz tur verisi yok.");

    // ViewModel'den gelen ve tur listesini temsil eden LiveData'yı gözlemle.
    // Bu, hem yeni tur eklendiğinde hem de bir tur silindiğinde grafiğin güncellenmesini sağlar.
    pageViewModel.getLapsForChart().observe(getViewLifecycleOwner(), new Observer<ArrayList<Lap>>() {
        @Override
        public void onChanged(ArrayList<Lap> laps) {
            // LiveData her değiştiğinde (ekleme/silme), grafiği bu yeni listeyle çiz.
            if (laps != null) {
                Log.d("ChartFragment", "LiveData değişti. " + laps.size() + " adet tur ile grafik çiziliyor.");
                drawChartWithLaps(laps);
            }
        }
    });
}
    /**
     * Verilen tur listesine (laps) göre grafiği temizler ve yeniden çizer.
     * Bu metod, hem yeni tur eklendiğinde hem de bir tur silindiğinde çağrılır.
     * @param laps Grafik için kullanılacak güncel Lap listesi.
     */
    private void drawChartWithLaps(ArrayList<Lap> laps) {
        // Eğer liste boşsa, grafiği temizle ve işlemi bitir.
        // KRİTİK KONTROL: Eğer liste boşsa, grafiği temizle ve çık.
        if (laps == null || laps.isEmpty()) {
            lineChart.setData(null); // Grafik verisini temizle
            lineChart.clear();       // Grafiği görsel olarak sıfırla
            lineChart.invalidate();  // UI'ı güncelle
            lineChart.setNoDataText("Henüz tur verisi yok."); // Varsayılan mesajı göster
            return;
        }

        // 1. Grafik için 'Entry' listesini oluştur
        // Gelen 'laps' listesi ViewModel tarafından zaten doğru sırada (1, 2, 3...) gönderiliyor.
        ArrayList<Entry> entries = new ArrayList<>();
        for (Lap lap : laps) {
            try {
                // "12.34 sn" gibi bir string'den sadece sayısal değeri al
                String numericString = lap.unit.replaceAll("[^\\d.]", "");
                float value = Float.parseFloat(numericString);
                entries.add(new Entry(lap.lapsayisi, value));
            } catch (NumberFormatException e) {
                Log.e("ChartFragment", "Tur verisi ('" + lap.unit + "') parse edilirken hata oluştu.", e);
            }
        }

        // 2. LineDataSet oluştur ve biçimlendir
        LineDataSet dataSet = new LineDataSet(entries, "Cycle Time"); // Grafik etiketi
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4.5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Çizgiyi daha yumuşak yapar
        dataSet.setValueTypeface(tf); // Değerler için font

        // 3. LineData oluştur ve grafiğe ata
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 4. Grafik UI Ayarlarını Yapılandır
        setupChartUI();

        // 5. Ortalama, Min, Max çizgilerini (LimitLine) hesapla ve ekle
        addLimitLines();

        // 6. Grafiği ekranda yenile
        lineChart.invalidate();
        Log.d("ChartFragment", "Grafik başarıyla yeniden çizildi.");
    }
    /**
     * Grafiğin eksenleri, açıklaması gibi genel UI ayarlarını yapar.
     */
    private void setupChartUI() {
        // Açıklamayı (description) kaldır
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        // Dokunma, sürükleme, yakınlaştırma ayarları
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // X Ekseni Ayarları
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X ekseni altta olsun
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f); // Adımlar arası en az 1 olsun (1, 2, 3...)
        xAxis.setDrawGridLines(false); // Arka plan grid çizgilerini kapat
        xAxis.setTypeface(tf);

        // Sol Y Ekseni Ayarları
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f); // Minimum değer 0'dan başlasın
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.GRAY);
        leftAxis.setTypeface(tf);

        // Sağ Y Ekseni'ni kapat
        lineChart.getAxisRight().setEnabled(false);

        // Legend (Veri setlerinin isimleri) ayarları
        lineChart.getLegend().setEnabled(false); // "Cycle Time" yazısını gizle
    }


    /**
     * ViewModel'den Min, Max, Avg değerlerini alıp grafiğe LimitLine olarak ekler.
     */
    private void addLimitLines() {
        YAxis leftAxis = lineChart.getAxisLeft();
        // Önceki LimitLine'ları temizle
        leftAxis.removeAllLimitLines();

        Float tmax = pageViewModel.getMaxTimeValue().getValue();
        Float tmin = pageViewModel.getMinTimeValue().getValue();
        Float tave = pageViewModel.getAvgTimeValue().getValue();
        timeUnit = pageViewModel.getTimeUnit(); // Zaman birimi "sn" vb.

        if (tmax != null) {
            LimitLine maxLine = new LimitLine(tmax, "Max: " + String.format(Locale.US, "%.2f", tmax) + " " + timeUnit);
            maxLine.setLineColor(Color.RED);
            maxLine.setLineWidth(1.5f);
            maxLine.setTextColor(Color.WHITE);
            maxLine.setTextSize(10f);
            maxLine.setTypeface(tf);
            leftAxis.addLimitLine(maxLine);
        }

        if (tmin != null) {
            LimitLine minLine = new LimitLine(tmin, "Min: " + String.format(Locale.US, "%.2f", tmin) + " " + timeUnit);
            minLine.setLineColor(Color.GREEN);
            minLine.setLineWidth(1.5f);
            minLine.setTextColor(Color.WHITE);
            minLine.setTextSize(10f);
            minLine.setTypeface(tf);
            leftAxis.addLimitLine(minLine);
        }

        if (tave != null) {
            LimitLine avgLine = new LimitLine(tave, "Avg: " + String.format(Locale.US, "%.2f", tave) + " " + timeUnit);
            avgLine.setLineColor(Color.YELLOW);
            avgLine.setLineWidth(1.5f);
          //  avgLine.setTextStyle(LimitLine.LimitLabelPosition.LEFT_TOP);
            avgLine.setTextColor(Color.WHITE);
            avgLine.setTextSize(10f);
            avgLine.setTypeface(tf);
            leftAxis.addLimitLine(avgLine);
        }
    }


    /**
     * Grafiği tamamen temizler ve başlangıç durumuna getirir.
     */
    public void clearChart() {
        lineChart.clear(); // Tüm veriyi, setleri ve limit line'ları siler
        lineChart.invalidate(); // Grafiğin temizlendiğini ekranda gösterir
        Log.d("ChartFragment", "Grafik temizlendi.");
    }
    // ChartFragment.java içine

    /**
     * Grafiği, TimerFragment'taki güncel lapsArray verisiyle yeniden çizer.
     * Bu metot, bir tur silindikten sonra çağrılmak üzere tasarlanmıştır.
     */
//    private void redrawChart() {
//        // 1. Mevcut grafik verilerini temizle
//        // lineChart.clear() kullanmak eski LimitLine'ları da sileceği için
//        // sadece data'yı temizlemek daha iyi bir yaklaşım olabilir.
//        lineChart.getData().clearValues(); // Sadece veri setlerini temizler
//        lineChart.invalidate(); // Grafiğin boşaldığını ekranda göster
//
//        // 2. TimerFragment'taki güncel tur listesini al
//        // lapsArray statik olduğu için doğrudan erişebiliriz.
//        ArrayList<Lap> currentLaps = TimerFragment.lapsArray;
//
//        // Eğer silme sonrası hiç tur kalmadıysa, işlemi bitir.
//        if (currentLaps == null || currentLaps.isEmpty()) {
//            Log.d("redrawChart", "Silme sonrası hiç tur kalmadı, grafik temizlendi.");
//            return;
//        }
//
//        // 3. Güncel tur listesini grafik için 'Entry' formatına dönüştür
//        ArrayList<Entry> newEntries = new ArrayList<>();
//        // lapsArray'in ters sıralı olduğunu unutma! (En son tur en başta)
//        // Grafiğin doğru (1, 2, 3...) sırada çizilmesi için listeyi tersten okumalıyız.
//        for (int i = currentLaps.size() - 1; i >= 0; i--) {
//            Lap lap = currentLaps.get(i);
//            try {
//                // LapsArray'deki 'unit' alanı bir string ("12.34 sn" gibi).
//                // Grafik için sadece sayısal kısmı almalıyız.
//                String numericString = lap.unit.replaceAll("[^\\d.,]", "").replace(',', '.');
//                float value = Float.parseFloat(numericString);
//                int lapNumber = lap.lapsayisi;
//                newEntries.add(new Entry(lapNumber, value));
//            } catch (NumberFormatException e) {
//                Log.e("redrawChart", "Tur verisi parse edilirken hata: " + lap.unit, e);
//            }
//        }
//
//        // 4. Yeni veri seti (LineDataSet) oluştur ve biçimlendir
//        LineDataSet dataSet = new LineDataSet(newEntries, "Cycle Time"); // Etiket
//        dataSet.setColor(Color.parseColor("#FF5722")); // Çizgi rengi (örnek)
//        dataSet.setCircleColor(Color.parseColor("#FF5722")); // Noktaların rengi
//        dataSet.setLineWidth(2f);
//        dataSet.setCircleRadius(4f);
//        dataSet.setValueTextSize(10f);
//        dataSet.setValueTextColor(Color.WHITE);
//        // Diğer biçimlendirme ayarlarını buraya ekleyebilirsiniz...
//
//        // 5. Yeni LineData oluştur ve grafiğe ata
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//
//        // 6. LimitLine'ları ve diğer UI elemanlarını yeniden ayarla
//        // Ortalama, min, max gibi çizgiler de yeniden hesaplanmalı ve çizilmeli.
//        // Bunun için DrawChart metodundaki LimitLine mantığını buraya taşıyabiliriz.
//        // VEYA daha iyisi: DrawChart metodunu bu yeni veriyle çağırabiliriz.
//        // Ancak DrawChart'ın kendisi de veri hazırladığı için çakışma olabilir.
//        // Bu yüzden temel UI ayarlarını burada yapalım:
//
//        // X ve Y eksenlerini tekrar yapılandır
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X ekseni altta olsun
//        xAxis.setTextColor(Color.WHITE);
//        xAxis.setGranularity(1f); // Adım aralığı 1 olsun (1, 2, 3...)
//
//        YAxis leftAxis = lineChart.getAxisLeft();
//        leftAxis.setTextColor(Color.WHITE);
//
//        lineChart.getAxisRight().setEnabled(false); // Sağ Y eksenini kapat
//
//        // 7. Grafiği ekranda yenile
//        lineChart.invalidate();
//
//        Log.d("redrawChart", newEntries.size() + " adet güncel veri ile grafik yeniden çizildi.");
//    }

    /**
     * Verilen zaman dizesini HH:mm:ss.SS formatında biçimlendirir,
     * son 4 rakamı (salise) daha küçük ve farklı renkte yapar.
     *
     * @param textView Zamanı gösterecek olan TextView.
     * @param timeString Tam zaman dizesi (Örn: "00:01:09.948").
     */
    private void displayFormattedTime(TextView textView, String timeString) {
        if (timeString == null || timeString.length() < 5) {
            // Hata durumunu veya boş durumu yönetin
            textView.setText(timeString);
            return;
        }

        // Son 4 karakter (örneğin .948'den sonraki 948) veya son 5 karakter (.948)
        // Eğer formatınız tam olarak "HH:mm:ss.SS" ise, nokta dahil son 4 karakteri alalım.
        // Eğer süre "00:01:09.948" ise:
        // 00:01:09. -> 9 karakter (Ana Süre)
        // .948 -> 4 karakter (Salise)

        int totalLength = timeString.length();
        // Son 4 rakamın başlangıç indeksi (Nokta dahil 4, sadece rakamlar için 3 veya 4)
        // Varsayım: Format "XX:XX:XX.XXX" (12 karakter) veya "XX:XX:XX.XX" (11 karakter)
        // Eğer "00:01:09.948" (12 karakter) ise, son 4 karakter ".948"
        int startIndexOfMillis = totalLength - 4;

        // Eğer zaman formatınızın milisaniye kısmı 3 haneyse (Örn: .948), totalLength - 4 doğru olur.
        // Eğer zaman formatınızın milisaniye kısmı 2 haneyse (Örn: .94), totalLength - 3 doğru olur.

        // Varsayılan olarak milisaniyeyi ve önceki noktayı kapsayan bir aralık alalım.
        if (timeString.contains(".")) {
            startIndexOfMillis = timeString.lastIndexOf(".");
        } else {
            // Eğer formatta nokta yoksa, sadece son 4 rakamı alalım (örneğin hhmmss948 -> son 4: s948)
            startIndexOfMillis = totalLength - 4;
        }


        SpannableString spannableString = new SpannableString(timeString);

        // 1. Yazı Boyutu Ayarı
        // Küçük boyutun kaç piksel (PX) olacağını belirleyin.
        // SP değil, PX kullanmalısınız. SP'den PX'e çevirmeliyiz.

        // Örnek: Orijinal boyutun %60'ı kadar küçültülmüş bir boyut ayarlayalım.
        // Varsayalım ki ana TextView boyutu 60sp.
        float originalTextSizeSp = 30; // XML'deki boyutunuz
        int originalTextSizePx = (int) (originalTextSizeSp * getResources().getDisplayMetrics().scaledDensity);
        int newSmallTextSizePx = (int) (originalTextSizePx * 0.70); // %70'ı kadar küçült

        // AbsoluteSizeSpan(int size, boolean dip) -> dip=false olduğunda size PX cinsinden kabul edilir.
        spannableString.setSpan(
                new AbsoluteSizeSpan(newSmallTextSizePx, false),
                startIndexOfMillis,
                totalLength,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // 2. Renk Ayarı (İsteğe Bağlı ama Okunurluk için Gerekli)
        // Farklı bir renk vermek isterseniz:
        int smallTextColor = ContextCompat.getColor(getContext(), R.color.colorPrimary); // R.color.red yerine istediğiniz rengi kullanın

        spannableString.setSpan(
                new ForegroundColorSpan(smallTextColor),
                startIndexOfMillis,
                totalLength,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // TextView'a uygulanır
        textView.setText(spannableString);
    }
    public void ClearChart() {

        lineEntry.clear();
       /* tAvgValue.clear();
        tMinValue.clear();
        tMaxValue.clear();*/
        lapValue.clear();
        lineChart.invalidate();
        lineChart.clear();
      //  chartTimer.setText("00:00:00");

    }

    private void DrawChart() {
        ;
/*
https://stackoverflow.com/questions/40999699/i-am-trying-to-make-values-of-x-axis-show-on-the-bottom-of-the-graph-not-the-top

 */

        System.out.println("kronos:  **" + pageViewModel.getTimerValue().getValue());
        DecimalFormat dec = new DecimalFormat("#0.00");
        Integer i = pageViewModel.getIndex().getValue();
        Float j = pageViewModel.getTimeValue().getValue();
        Float tmax = pageViewModel.getMaxTimeValue().getValue();
        Float tmin = pageViewModel.getMinTimeValue().getValue();
        Float tave = pageViewModel.getAvgTimeValue().getValue();
        timeUnit = pageViewModel.getTimeUnit();
        System.out.println("zaman değeri -->" + pageViewModel.getTimeValue().getValue());
        System.out.println("lap değeri-->" + pageViewModel.getIndex().getValue());

        //lineEntry.add(new Entry(i, j));// burası aslında drawchart içinde olmalı.
        lapValue.add(new Entry(i, j)); //cycle time value
        textSize = 20f;
        //limitline koyunca gerek kalmadı alttakilere
     /*   tMaxValue.add(new Entry(i, tmax));
//sabit değer göstermek iiçin
        for (int y = 0; y < i; y++) {
            tMaxValue.get(y).setY(tmax);
        }
        ;

        tMinValue.add(new Entry(i, tmin));
        for (int y = 0; y < i; y++) {
            tMinValue.get(y).setY(tmin);

        }

        tAvgValue.add(new Entry(i, tave));
        for (int y = 0; y < i; y++) {
            tAvgValue.get(y).setY(tave);

        }*/
        ;

        /*T avg***/

        /*
        MPAndroidChart librarysi yükledikten sonra

        Grafik yapmak önce gösterilecek değerleri DataSet haline getirmek gerekiyor. Her bir
         seri için tek tek dataset leri oluşturmak lazım.
         oluşturulan DataSet içinde çizilecek grafiğin özellikleri belirtilir. renk,çizgi kalınlığı vbg.

         Dataset ile işlem bittiğinde bu setleri iLineDataSet şeklinde arraylist e tanıtırsın
         List<ILineDataSet> iLinedata = new ArrayList<ILineDataSet>();
         sonra bu arrayliste herbir dataseti eklersin.
         iLineData.add( DatasetXX)

         arraylist oluşturulduktan sonra LineData objesini yaratırsın. bir önceki arraylist bu objenin
         veri tipi olmalı.
         LineData linedata = new LineData(iLinedata);
        sonrasında bu objeyi grafik objesine set edersin.
        lineChart.setData(linedata);


         */
       /* LineDataSet tAvgValueDataSet = new LineDataSet(tAvgValue, "Avg.Cyc.Time Value");
        tAvgValueDataSet.setColor(Color.MAGENTA);
        tAvgValueDataSet.setLineWidth(2f);
        tAvgValueDataSet.setDrawCircles(false);//daireleri çizme
        tAvgValueDataSet.setDrawValues(false);//değerleri gösterme
        tAvgValueDataSet.setValueTextSize(12);*/

        /** LAPS ***/
        LineDataSet lapValueDataSet = new LineDataSet(lapValue, "Cyc.Time Value");
        lapValueDataSet.setColor(Color.WHITE);

        lapValueDataSet.setValueTextSize(textSize);
        lapValueDataSet.setValueTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary )); // rakamların rengini çıkartır dark/light
        lapValueDataSet.setCircleColor(Color.GREEN);
        lapValueDataSet.setCircleRadius(5);
        lapValueDataSet.setValueTypeface(tf);
        lapValueDataSet.setLineWidth(3f);
        lapValueDataSet.enableDashedLine(3, 3, 3);
        lapValueDataSet.isDashedLineEnabled();
        /* *//*T max **//*
        LineDataSet tMaxValueDataSet = new LineDataSet(tMaxValue, "Max.Cyc.Time Value");
        tMaxValueDataSet.setColor(Color.RED);
        tMaxValueDataSet.setDrawCircles(false);//daireleri çizme
        tMaxValueDataSet.setDrawValues(false);//değerleri gösterme
        tMaxValueDataSet.setLineWidth(2f);
        tMaxValueDataSet.setValueTextSize(12);
        *//**T MİN **//*

        LineDataSet tMinValueDataSet = new LineDataSet(tMinValue, "Min.Cyc.Time Value");
        tMinValueDataSet.setColor(Color.RED);
        tMinValueDataSet.setDrawCircles(false);//daireleri çizme
        tMinValueDataSet.setDrawValues(false);//değerleri gösterme
        tMinValueDataSet.setValueTextSize(12);
        tMinValueDataSet.setLineWidth(2f);*/

        LimitLine tmaxLimit = new LimitLine(tmax, "Maximum Cycle Time: " + dec.format(tmax) + " "+timeUnit);
        tmaxLimit.setLineWidth(4f);
        tmaxLimit.setTextColor(getResources().getColor(R.color.colorPrimary));
        tmaxLimit.enableDashedLine(10f, 10f, 0f);
        tmaxLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        tmaxLimit.setTextSize(textSize);
        tmaxLimit.setTypeface(tf);

        LimitLine tminLimit = new LimitLine(tmin, "Minimum Cycle Time: " + dec.format(tmin) + " "+timeUnit);
        tminLimit.setLineWidth(4f);
        tminLimit.setTextColor(getResources().getColor(R.color.colorPrimary));
        tminLimit.enableDashedLine(10f, 10f, 0f);
        tminLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        tminLimit.setTextSize(textSize);
        tminLimit.setTypeface(tf);

        LimitLine taveLimit = new LimitLine(tave, "Mean Cycle Time: " + dec.format(tave) + " "+timeUnit);
        taveLimit.setLineWidth(4f);
        taveLimit.setTextColor(getResources().getColor(R.color.colorPrimary));
        taveLimit.setLineColor(Color.MAGENTA);
        taveLimit.enableDashedLine(10f, 10f, 0f);
        taveLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        taveLimit.setTextSize(textSize);
       ;
        taveLimit.setTypeface(tf);

        //sağ y ekseni kapama
        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setEnabled(false);
        //sol y ekseni

        YAxis yAxis = lineChart.getAxisLeft();
        // yAxis.setLabelCount(4,true);
        yAxis.setTypeface(tf);
        yAxis.removeAllLimitLines();
        yAxis.addLimitLine(tmaxLimit);
        yAxis.addLimitLine(tminLimit);
        yAxis.addLimitLine(taveLimit);
        yAxis.setDrawLimitLinesBehindData(true);
        /*
        yAxis.setMaxWidth(3);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setValueFormatter(new DefaultAxisValueFormatter(3));
        yAxis.setTextColor(Color.RED);
        yAxis.setTextSize(12f); //not in your original but added
        yAxis.setGridColor(Color.argb(102,255,255,255));
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        yAxis.setAxisMinimum(0); //not in your original but added */


        /***X eksenini alta alma**/
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);// ölçeği 1 adım olarak ayarlama

        List<ILineDataSet> iLinedata = new ArrayList<ILineDataSet>();

        //iLinedata.add(tAvgValueDataSet);
        iLinedata.add(lapValueDataSet);
        // iLinedata.add(tMaxValueDataSet);
        // iLinedata.add(tMinValueDataSet);
        //ekran genişliğini almak
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        // System.out.println("X ölçüsü-->" + width);


        LineData linedata = new LineData(iLinedata);
        lineChart.setData(linedata);
        lineChart.invalidate(); //refreshing the line chart


      // lineChart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //lineChart.animateXY(500, 500);


         Description des = lineChart.getDescription();
        des.setTypeface(tf);
        des.setText("Cycle Time Chart ");
        des.setTextAlign(Paint.Align.CENTER);
        des.setPosition(width / 2, 100);//yukarıdan width alıyor
        des.setTextSize(25);
        des.setTextColor(Color.BLUE);
        des.setEnabled(false);
        ;



    }

    public void saveChartToGallery() {
        // Android 10 (API 29) ve öncesi için depolama izni kontrolü KRİTİKTİR.
        // Android 11 (API 30) ve sonrası için MikePhil kütüphanesi MediaStore kullanır ve genelde
        // ek bir runtime iznine gerek kalmaz, ancak bu kontrolü yapmak güvenlidir.

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // İzin verilmediyse, izin iste
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_STORAGE_PERMISSION);
            return; // İzin isteyip geri dön
        }

        // --- İzin Verilmiş veya Android 11+ ise Kayıt İşlemine Geç ---
        performSaveChart();
    }

    /**
     * İzin verilme sonucunu ele alır.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi, şimdi kaydetme işlemini başlat
                performSaveChart();
            } else {
                // İzin reddedildi, kullanıcıya bilgi ver
                Toast.makeText(getContext(), "Storage permission is required to save the chart.", Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     * Gerçek kaydetme mantığını içerir. İzin kontrolünden sonra çağrılır.
     */
    private void performSaveChart() {
        if (lineChart == null) {
            Toast.makeText(getContext(), "The chart component is not yet ready for use.", Toast.LENGTH_SHORT).show();
            return;
        }
// Kontrol 2: Grafikte gerçekten veri var mı? (Çizim yapılabilir mi?)
        if (lineChart.getData() == null || lineChart.getData().getEntryCount() == 0) {
            Toast.makeText(getContext(), "No data found to be saved in the chart", Toast.LENGTH_SHORT).show();
            return;
        }
        // MikePhil Chart Kaydetme Kodu
        String albumName = "IndustrialChronometer";

        // Hata düzeltmesi: Doğru importlar ile bu çalışacaktır.
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "CHART_" + timestamp;

        try {
            // saveToGallery, PNG formatında kaydeder.
            boolean success = lineChart.saveToGallery(fileName, albumName, "", Bitmap.CompressFormat.PNG, 90);

            if (success) {
                Toast.makeText(getContext(),
                        "Chart saved successfully:.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(),
                        "An error occurred while saving the chart.",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Save operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

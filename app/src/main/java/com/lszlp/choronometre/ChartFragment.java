package com.lszlp.choronometre;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class ChartFragment extends Fragment {

    LineChart lineChart;
    ArrayList<Entry> lineEntry, lapValue;
    TextView chartTimer;
    PageViewModel pageViewModel;
    Typeface tf;
    Float textSize;

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

        return inflater.inflate(R.layout.fragment_chart, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //grafik için değişkenlerin oluşturulması
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tf = getResources().getFont(R.font.digital7);
        }
        lineChart = view.findViewById(R.id.chart);
        lineChart.setNoDataTextTypeface(tf);
        lineChart.setNoDataTextColor(R.color.chartColor);
        lineEntry = new ArrayList<>();
        lapValue = new ArrayList<>();
        chartTimer = view.findViewById(R.id.chartTimer);
        /*tAvgValue = new ArrayList<>();
        tMinValue = new ArrayList<>();
        tMaxValue = new ArrayList<>();*/

           pageViewModel.getTimerValue().observe(requireActivity(), new Observer<String>() {
                @Override
                public void onChanged(String s) {

                    ;
                  displayFormattedTime(chartTimer,s);
                   // chartTimer.setText(s);
                    System.out.println("Chart timer value :" + s );
                }
            });;




        pageViewModel.getIndex().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                /*
                ama her seferinde lap tuşuna basınca lineENTRY dizisine eklemesi lazım
                */
                ;

                DrawChart();
            }
        });

        //grafik için değişkenlerin oluşturulması
        //1-Grafik tipini tanıt
        //lineChart.invalidate();
        //2- *** X değerleri***/
        //3 ***Y değerleri ***/

        //4
        //lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        //5
        //6


    }
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
        chartTimer.setText("00:00:00");

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

        System.out.println("zaman değeri -->" + pageViewModel.getTimeValue().getValue());
        System.out.println("lap değeri-->" + pageViewModel.getIndex().getValue());

        //lineEntry.add(new Entry(i, j));// burası aslında drawchart içinde olmalı.
        lapValue.add(new Entry(i, j)); //cycle time value
        textSize = 16f;
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

        LimitLine tmaxLimit = new LimitLine(tmax, "Maximum Cycle Time: " + dec.format(tmax) + " cyc/unit ");
        tmaxLimit.setLineWidth(4f);
        tmaxLimit.setTextColor(getResources().getColor(R.color.colorPrimary));
        tmaxLimit.enableDashedLine(10f, 10f, 0f);
        tmaxLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        tmaxLimit.setTextSize(textSize);
        tmaxLimit.setTypeface(tf);

        LimitLine tminLimit = new LimitLine(tmin, "Minimum Cycle Time: " + dec.format(tmin) + " cyc/unit ");
        tminLimit.setLineWidth(4f);
        tminLimit.setTextColor(getResources().getColor(R.color.colorPrimary));
        tminLimit.enableDashedLine(10f, 10f, 0f);
        tminLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        tminLimit.setTextSize(textSize);
        tminLimit.setTypeface(tf);

        LimitLine taveLimit = new LimitLine(tave, "Mean Cycle Time: " + dec.format(tave) + " cyc/unit ");
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
}

package com.lszlp.choronometre;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lszlp.choronometre.main.PageViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class ChartFragment extends Fragment {

    // CombinedChart, LineChart'ın tüm özelliklerini ve daha fazlasını içerir.
    CombinedChart combinedChart;
    PageViewModel pageViewModel;
    Typeface tf;
    String timeUnit;
    Float tmax,tmin, tave;
    ArrayList<Float> avgTimes = new ArrayList<>();

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ImageView takePhoto = view.findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(v -> saveChartToGallery());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Grafik bileşenini ve temel ayarları yap
        // XML layout'unuzda CombinedChart'ın ID'sinin "chart" olduğunu varsayıyoruz.
        combinedChart = view.findViewById(R.id.combinedchart);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tf = getResources().getFont(R.font.robotoflex);

        }
        setupChartUI(); // Grafik UI ayarlarını başlangıçta yap

        // 2. ViewModel'den gelen tur listesini gözlemle
        pageViewModel.getLapsForChart().observe(getViewLifecycleOwner(), new Observer<ArrayList<Lap>>() {
            @Override
            public void onChanged(ArrayList<Lap> laps) {
                // LiveData her değiştiğinde (ekleme/silme), grafiği bu yeni listeyle çiz.
                Log.d("ChartFragment", "LiveData değişti. Gelen tur sayısı: " + (laps != null ? laps.size() : "null"));
                drawCombinedChart(laps);
            }
        });
        pageViewModel.getAvgTimeValue().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float newAvgTime) {

             //   avgTimes.add(newAvgTime);
             //   combinedChart.invalidate();

            }
        });
    }

    /**
     * Verilen tur listesine göre Birleşik Grafiği (CombinedChart) çizer.
     * @param laps Grafik için kullanılacak güncel Lap listesi.
     */
    private void drawCombinedChart(ArrayList<Lap> laps) {
        // Güvenlik kontrolü: Liste null veya boş ise grafiği temizle ve çık.
        if (laps == null || laps.isEmpty()) {
            clearChart();
            return;
        }

        // Tüm grafik verilerini tutacak olan ana veri nesnesi
        CombinedData combinedData = new CombinedData();
        // Min, Max, Ortalama çizgilerini ekle
        addLimitLines();

        // Bar grafiği verilerini oluşturup ana veriye ekle
        combinedData.setData(generateBarChartData(laps));
        // Ortalama çizgisi verisini oluşturup ana veriye ekle
        combinedData.setData(generateAverageLineChartData(laps));

        // Veriyi grafiğe ata
        combinedChart.setData(combinedData);



        // Grafiği yenile
        combinedChart.invalidate();
        Log.d("ChartFragment", "CombinedChart başarıyla çizildi.");
    }

    /**
     * Tur zamanlarını gösteren Çubuk Grafiği (BarData) verisini oluşturur.
     */
    private BarData generateBarChartData(ArrayList<Lap> laps) {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        // avgTimes listesini her yeni çizimde sıfırla
        avgTimes.clear();

        for (int i = 0; i < laps.size(); i++) {
            Lap lap = laps.get(i);
            try {
                String numericString = lap.unit.replaceAll("[^\\d.]", "");
                float value = Float.parseFloat(numericString);
                barEntries.add(new BarEntry(lap.lapsayisi, value));

                // Ortalamayı dinamik olarak hesapla ve avgTimes listesine ekle
                float currentSum = 0;
                for (int j = 0; j <= i; j++) {
                    String currentNumericString = laps.get(j).unit.replaceAll("[^\\d.]", "");
                    currentSum += Float.parseFloat(currentNumericString);
                }
                avgTimes.add(currentSum / (i + 1));

            } catch (NumberFormatException e) {
                Log.e("ChartFragment", "Tur verisi ('" + lap.unit + "') parse edilirken hata.", e);
            }
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Cycle Time");
        // --- Stil Ayarları ---
        barDataSet.setColor(Color.parseColor("#2243ff"));
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(16f);
        barDataSet.setDrawValues(true);
        barDataSet.setDrawIcons(false);
        barDataSet.setValueTypeface(tf);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        return new BarData(barDataSet);


    }

    /**
     * (İSTEĞE BAĞLI) Ortalamadan sapmayı gösteren Çizgi Grafiği (LineData) verisini oluşturur.
     */
    private LineData generateAverageLineChartData(ArrayList<Lap> laps) {
        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (int i = 0; i < avgTimes.size(); i++) {
            lineEntries.add(new Entry(i + 1, avgTimes.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Average Cycle Time");
        // --- Stil Ayarları ---
        lineDataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        lineDataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        lineDataSet.setLineWidth(3.5f);
        lineDataSet.setCircleRadius(4.5f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(13f);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setValueTypeface(tf);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setDrawValues(false); // Ortalama çizgisinin üzerindeki değerleri gizle

        return new LineData(lineDataSet);
    }

    /**
     * Grafiğin eksenleri, açıklaması gibi genel UI ayarlarını yapar.
     */
    private void setupChartUI() {

        combinedChart.setNoDataText("No Data to Show");

        combinedChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.white));
        combinedChart.setNoDataTextTypeface(tf);

        // Çizim sırasını belirle (Önce Bar, üzerine Çizgi)
        combinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        Description description = new Description();
        description.setText("");
        combinedChart.setDescription(description);

        combinedChart.setTouchEnabled(true);
        combinedChart.setDragEnabled(true);
        combinedChart.setScaleEnabled(true);
        combinedChart.setPinchZoom(true);
        combinedChart.setDrawGridBackground(false);

        // X Ekseni Ayarları
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f); // Tur numaraları (1, 2, 3...)
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(tf);
        xAxis.setTextSize(13f);
        xAxis.setAxisMinimum(0.5f); // Grafiğin başlangıcında boşluk bırakır

        // Sol Y Ekseni Ayarları
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.GRAY);
        leftAxis.setTextSize(13f);
        leftAxis.setTypeface(tf);

        // Sağ Y Ekseni'ni kapat
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getLegend().setEnabled(true);
        combinedChart.getLegend().setTextSize(18f);
        combinedChart.getLegend().setTypeface(tf);
        combinedChart.getLegend().setTextColor(Color.WHITE);

    }

    /**
     * ViewModel'den Min, Max, Avg değerlerini alıp grafiğe LimitLine olarak ekler.
     */
    private void addLimitLines()
                 {
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.removeAllLimitLines();

       tmax = pageViewModel.getMaxTimeValue().getValue();
        tmin = pageViewModel.getMinTimeValue().getValue();
         tave = pageViewModel.getAvgTimeValue().getValue();

        timeUnit = pageViewModel.getTimeUnit();

        if (tmax != null) {
            LimitLine maxLine = new LimitLine(tmax, "Max: " + String.format(Locale.US, "%.2f", tmax) + " " + timeUnit);
            maxLine.setLineColor(Color.RED);
            maxLine.setLineWidth(1.5f);
            maxLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            maxLine.setTextColor(Color.WHITE);
            maxLine.setTextSize(13f);
            maxLine.setTypeface(tf);
            leftAxis.addLimitLine(maxLine);
        }
        // ... (tmin ve tave için LimitLine kodları buraya eklenecek, önceki kodla aynı)
        if (tmin != null) {
            LimitLine minLine = new LimitLine(tmin, "Min: " + String.format(Locale.US, "%.2f", tmin) + " " + timeUnit);
            minLine.setLineColor(Color.GREEN);
            minLine.setLineWidth(1.5f);
            minLine.setTextColor(Color.WHITE);
            minLine.setTextSize(13f);
            minLine.setTypeface(tf);
            minLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            leftAxis.addLimitLine(minLine);
        }

        if (tave != null) {
            LimitLine avgLine = new LimitLine(tave, "Avg: " + String.format(Locale.US, "%.2f", tave) + " " + timeUnit);
            avgLine.setLineColor(Color.YELLOW);
            avgLine.setLineWidth(1.5f);
            avgLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            avgLine.setTextColor(Color.WHITE);
            avgLine.setTextSize(13f);
            avgLine.setTypeface(tf);
            leftAxis.addLimitLine(avgLine);
        }
    }

    /**
     * Grafiği tamamen temizler ve başlangıç durumuna getirir.
     */
    public void clearChart() {
        combinedChart.clear();
        avgTimes.clear(); // avgTimes listesini de temizle
        combinedChart.invalidate();
        Log.d("ChartFragment", "Grafik temizlendi.");
    }

    /**
     * Grafiğin mevcut görünümünü galeriye kaydeder.
     * (Bu metodun içeriğinin çalıştığını varsayıyoruz, değişiklik yapılmasına gerek yok.)
     */
    private void saveChartToGallery() {
        // ... Mevcut saveChartToGallery kodunuz ...
        // Örnek:
        if (combinedChart.saveToGallery("Kronometre_Chart_" + System.currentTimeMillis(), 100)) {
            Toast.makeText(getContext(), "Chart's screenchot registered in your gallery.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Chart's can not be registered in your gallery.", Toast.LENGTH_SHORT).show();
        }
    }
}